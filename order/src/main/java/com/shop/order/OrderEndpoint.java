package com.shop.order;

import com.shop.delivery.CancelDeliveryRequest;
import com.shop.delivery.DeliveryInfo;
import com.shop.delivery.DeliveryRequest;
import com.shop.delivery.DeliveryStatusRequest;
import com.shop.order.type.OrderInfo;
import com.shop.order.type.OrderRequest;
import com.shop.payment.PaymentService;
import com.shop.payment.type.PaymentError;
import com.shop.payment.type.PaymentInfo;
import com.shop.payment.type.PaymentRequest;
import com.shop.sale.SaleService;
import com.shop.sale.type.SaleError;
import com.shop.sale.type.SaleInfo;
import com.shop.sale.type.SaleRequest;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.client.SoapFaultClientException;

import java.util.UUID;

import static org.apache.camel.model.rest.RestParamType.body;

@Component
public class OrderEndpoint extends RouteBuilder {

    private final SaleService saleService;
    private final PaymentService paymentService;
    private final OrderService orderService;

    private final String kafkaHost;
    private final String kafkaPort;
    private final String deliveryPort;
    private final String deliveryHost;

    private final static String ORDER_ID = "orderId";
    private final static String TOPIC_ORDER_REQUEST = "OrderReqTopic";
    private final static String TOPIC_ORDER_INFO = "OrderInfoTopic";
    private final static String TOPIC_ORDER_READY = "OrderReadyTopic";
    private final static String TOPIC_ORDER_CANCEL = "CancelOrderTopic";

    public OrderEndpoint(SaleService saleService,
                         PaymentService paymentService,
                         OrderService orderService,
                         @Value("${kafka.host:localhost}") String kafkaHost,
                         @Value("${kafka.port:9093}") String kafkaPort,
                         @Value("${delivery.host:localhost}") String deliveryHost,
                         @Value("${delivery.port:9901}") String deliveryPort) {
        this.saleService = saleService;
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.kafkaHost = kafkaHost;
        this.kafkaPort = kafkaPort;
        this.deliveryPort = deliveryPort;
        this.deliveryHost = deliveryHost;
    }

    @Override
    public void configure() throws Exception {

        saleErrorHandler();
        gateway();
        delivery();
        sale();
        order();
        payment();
        status();

    }

    private void gateway() {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .contextPath("/api")
                // turn on swagger api-doc
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Online shop API")
                .apiProperty("api.version", "1.0.0");

        rest("/order")
                .description("Order Service")
                .consumes("application/json")
                .produces("application/json")
                .post("/place")
                .description("Place order")
                .type(OrderRequest.class)
                .outType(OrderInfo.class)
                .param().name("body").type(body)
                .description("The order").endParam()
                .responseMessage().code(200)
                .message("Order successfully placed")
                .endResponseMessage()
                .to("direct:placeOrder")

                .get("/status/{id}")
                .param().type(RestParamType.path).name("id").description("Order id").endParam()
                .description("Order Status")
                .outType(OrderInfo.class)
                .responseMessage().code(200).endResponseMessage()
                .to("direct:status")
        ;

        from("direct:placeOrder")
                .routeId("placeOrder")
                .log("placeOrder fired")
                .process((exchange) -> {
                    String orderId = UUID.randomUUID().toString();
                    exchange.getMessage().setHeader(ORDER_ID, orderId);
                    exchange.setProperty(ORDER_ID, orderId);
                    this.orderService.create(orderId, exchange.getMessage().getBody(OrderRequest.class));
                })
                .to("direct:OrderRequest")
                .to("direct:OrderFinalizer")
        ;

        // ???????
        from("direct:OrderFinalizer")
                .routeId("OrderFinalizer")
                .log("OrderFinalizer fired")
                .process((exchange) -> {
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    exchange.getMessage().setBody(this.orderService.get(orderId));
                });

        from("direct:OrderRequest")
                .routeId("OrderRequest")
                .log("brokerTopic fired")
                .marshal()
                .json()
                .to(this.kafkaTopicName(TOPIC_ORDER_REQUEST));
    }

    private void delivery() {
        final JaxbDataFormat jaxbDelivery = new JaxbDataFormat(com.shop.delivery.DeliveryInfo.class.getPackage().getName());
        from(this.kafkaTopicName(TOPIC_ORDER_REQUEST))
                .routeId("delivery")
                .log("Delivery order")
//                .saga().propagation(SagaPropagation.REQUIRED)
//                .compensation("direct:HandleFailedDelivery")
//                .option(ORDER_ID, simple("${exchangeProperty.orderId}"))
                .unmarshal().json(JsonLibrary.Jackson, OrderRequest.class)
                .process((exchange) -> {
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    DeliveryInfo startDeliveryInfo = new DeliveryInfo();
                    startDeliveryInfo.setId(-1);
                    startDeliveryInfo.setStatus("STARTED");
                    startDeliveryInfo.setCost(null);
                    this.orderService.update(orderId, startDeliveryInfo);

                    DeliveryRequest request = exchange.getMessage().getBody(OrderRequest.class).getDeliveryRequest();
                    exchange.getMessage().setBody(request);
                })
                .marshal(jaxbDelivery)
                .onException(SoapFaultClientException.class)
                    .handled(true)
                    .log("Delivery error")
                    .to("direct:HandleFailedDelivery")
                .end()
                .to(this.deliveryEndpoint(""))
                .log("Delivery returned")
                .to("stream:out")
                .unmarshal(jaxbDelivery)
                .process((exchange) -> {
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    DeliveryInfo deliveryInfo = exchange.getMessage().getBody(DeliveryInfo.class);
                    OrderInfo oi = new OrderInfo(orderId);
                    oi.setDeliveryInfo(deliveryInfo);
                    exchange.getMessage().setBody(oi);
                    exchange.getMessage().setHeader("shouldBeCanceled", false);

                    // delivery canceled due to invalid sale
                    OrderInfo cachedInfo = this.orderService.get(orderId);
                    if(cachedInfo.getDeliveryInfo().getStatus().equals("STARTED") && cachedInfo.getStatus().equals("CANCELED")) {
                        OrderInfo orderInfo = this.orderService.update(orderId, deliveryInfo);
                        exchange.getMessage().setHeader("shouldBeCanceled", true);
                        exchange.getMessage().setBody(orderInfo);
                    }
                })
                .marshal().json()
                .log("Delivery finalized")
                .to("stream:out")
                .setHeader("serviceType", constant("delivery"))
                .choice()
                .when(header("shouldBeCanceled").isEqualTo(false))
                .to(this.kafkaTopicName(TOPIC_ORDER_INFO))
                .otherwise()
                .log("Delivery should be canceled - calling compensation")
                .unmarshal().json(JsonLibrary.Jackson, OrderInfo.class)
                .to("direct:CancelDeliveryRequest")
                .endChoice();

        from(this.kafkaTopicName(TOPIC_ORDER_CANCEL))
                .routeId("cancelDelivery")
                .log("Cancel Delivery fired")
                .unmarshal().json(JsonLibrary.Jackson, OrderInfo.class)
                .process(exchange -> {
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    OrderInfo orderInfo = this.orderService.get(orderId);
                    boolean isDeliveryFailed = orderInfo.getDeliveryInfo().getStatus().equals("FAILED");
                    if(isDeliveryFailed) {
                        orderInfo.setStatus("CANCELED");
                    }
                    boolean isDeliveryScheduled = orderInfo.getDeliveryInfo().getStatus().equals("SCHEDULED");
                    exchange.getMessage().setHeader("shouldMakeCancelRequest", isDeliveryScheduled);
                    exchange.getMessage().setBody(orderInfo);
                })
//                .marshal().json()
                .choice()
                .when(header("shouldMakeCancelRequest").isEqualTo(true))
                .log("Delivery already scheduled - canceling")
                .to("direct:CancelDeliveryRequest")
                .otherwise()
                .log("Cancel delivery skipped")
                .endChoice();

        from("direct:CancelDeliveryRequest")
                .routeId("CancelDeliveryRequest")
                .log("CancelDeliveryRequest fired")
                .process((exchange) -> {
                    DeliveryInfo deliveryInfo = exchange.getMessage().getBody(OrderInfo.class).getDeliveryInfo();
                    CancelDeliveryRequest request = new CancelDeliveryRequest();
                    request.setId(deliveryInfo.getId());
                    exchange.getMessage().setBody(request);
                })
                .marshal(jaxbDelivery)
                .to(this.deliveryEndpoint("cancel"))
                .log("Delivery canceled")
                .to("stream:out")
                .unmarshal(jaxbDelivery)
                .process((exchange) -> {
                    DeliveryInfo cancelInfo = exchange.getMessage().getBody(DeliveryInfo.class);
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    this.orderService.get(orderId).setStatus("CANCELED");
                    OrderInfo oi = this.orderService.update(orderId, cancelInfo);
                    exchange.getMessage().setBody(oi);
                })
                .marshal().json().to("stream:out")
                ;

        from("direct:HandleFailedDelivery")
                .routeId("HandleFailedDelivery")
                .log("HandleFailedDelivery fired")
                .process(exchange -> {
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    OrderInfo failedDeliveryInfo = new OrderInfo(orderId);
                    failedDeliveryInfo.setStatus("CANCELED");
                    DeliveryInfo deliveryInfo = new DeliveryInfo();
                    deliveryInfo.setId(-1);
                    deliveryInfo.setStatus("FAILED");
                    deliveryInfo.setCost(null);
                    failedDeliveryInfo.setDeliveryInfo(deliveryInfo);
                    exchange.getMessage().setBody(failedDeliveryInfo);
                    this.orderService.get(orderId).setStatus("CANCELED");
                    this.orderService.update(orderId, deliveryInfo);
                })
                .marshal().json()
                .to(this.kafkaTopicName(TOPIC_ORDER_CANCEL))
                ;
    }

    private void sale() {
        from(this.kafkaTopicName(TOPIC_ORDER_REQUEST))
                .routeId("sale")
                .log("Make sale")
                .unmarshal().json(JsonLibrary.Jackson, OrderRequest.class)
                .process((exchange) -> {
                    SaleRequest saleRequest = exchange.getMessage().getBody(OrderRequest.class).getSaleRequest();
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    SaleInfo saleInfo = this.saleService.create(saleRequest, orderId);
                    OrderInfo oi = new OrderInfo(orderId);
                    oi.setSaleInfo(saleInfo);
                    exchange.getMessage().setBody(oi);
                })
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("sale"))
                .to(this.kafkaTopicName(TOPIC_ORDER_INFO));

        from(this.kafkaTopicName(TOPIC_ORDER_CANCEL))
                .routeId("cancelSale")
                .log("Cancel sale fired")
                .unmarshal().json(JsonLibrary.Jackson, OrderInfo.class)
                .process(exchange -> {
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    OrderInfo orderInfo = this.orderService.get(orderId);
                    if (!orderInfo.getSaleInfo().getStatus().equals("FAILED")) {
                        SaleInfo cancelInfo = this.saleService.cancel(orderId);
                        this.orderService.update(orderId, cancelInfo);
                    }
                })
        ;

    }

    private void order() {
        from(this.kafkaTopicName(TOPIC_ORDER_INFO))
                .routeId("orderInfo")
                .log("Merge OrderInfo")
                .unmarshal().json(JsonLibrary.Jackson, OrderInfo.class)
                .process((exchange) -> {
                    OrderInfo orderInfo = exchange.getMessage().getBody(OrderInfo.class);
                    if (orderInfo.getDeliveryInfo() != null) {
                        this.orderService.update(orderInfo.getId(), orderInfo.getDeliveryInfo());
                    }
                    if (orderInfo.getSaleInfo() != null) {
                        this.orderService.update(orderInfo.getId(), orderInfo.getSaleInfo());
                    }
                    orderInfo = this.orderService.get(orderInfo.getId());
                    boolean isReady = orderInfo.getDeliveryInfo() != null &&
                            orderInfo.getDeliveryInfo().getCost() != null &&
                            orderInfo.getSaleInfo() != null &&
                            orderInfo.getSaleInfo().getCost() != null;
                    exchange.getMessage().setHeader("isReady", isReady);
                    exchange.getMessage().setBody(orderInfo);
                })
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("merge"))
                .choice()
                .when(header("isReady").isEqualTo(true)).to(this.kafkaTopicName(TOPIC_ORDER_READY))
                .endChoice();

    }

    private void payment() {
        from(this.kafkaTopicName(TOPIC_ORDER_READY))
                .routeId("finalizePayment")
                .log("Finalize Payment")
                .unmarshal().json(JsonLibrary.Jackson, OrderInfo.class)
                .process((exchange) -> {
                    OrderInfo orderInfo = exchange.getMessage().getBody(OrderInfo.class);
                    PaymentRequest paymentRequest = this.orderService.getRequest(orderInfo.getId()).getPaymentRequest();
                    PaymentInfo paymentInfo = this.paymentService.create(paymentRequest, orderInfo.getSaleInfo(), orderInfo.getDeliveryInfo(), orderInfo.getId());
                    this.orderService.update(orderInfo.getId(), paymentInfo);
                    orderInfo.setPaymentInfo(paymentInfo);
                    exchange.getMessage().setBody(orderInfo);
                }).marshal().json()
                .setHeader("serviceType", constant("finalize"))
                .to("stream:out")
                .unmarshal().json(JsonLibrary.Jackson, OrderInfo.class)
        ;

    }

    private void status() {
        final JaxbDataFormat jaxbDelivery = new JaxbDataFormat(com.shop.delivery.DeliveryInfo.class.getPackage().getName());
        from("direct:status")
                .routeId("getStatus")
                .log("GetStatus fired")
                .process((exchange) -> {
                    String orderId = exchange.getIn().getHeader("id", String.class);
                    this.orderService.update(orderId, this.saleService.info(orderId));
                    try{
                        this.orderService.update(orderId, this.paymentService.info(orderId));
                    } catch (PaymentError pe) {
                        this.orderService.update(orderId, new PaymentInfo(orderId, "UNAVAILABLE", null));
                    }

                    DeliveryInfo deliveryInfo = this.orderService.get(orderId).getDeliveryInfo();
                    DeliveryStatusRequest request = new DeliveryStatusRequest();
                    request.setId(deliveryInfo.getId());
                    exchange.getMessage().setBody(request);
                })
                .marshal(jaxbDelivery)

                .onException(SoapFaultClientException.class)
                .handled(true)
                .log("Delivery status error")
                .process((exchange) -> {
                    DeliveryInfo deliveryInfo = new DeliveryInfo();
                    deliveryInfo.setId(-1);
                    deliveryInfo.setStatus("UNAVAILABLE");
                    deliveryInfo.setCost(null);
                    String orderId = exchange.getMessage().getHeader("id", String.class);
                    OrderInfo oi = this.orderService.update(orderId, deliveryInfo);
                    exchange.getMessage().setBody(oi);
                })
                .marshal().json()
                .log("Status finalized with error")
                .to("stream:out")
                .unmarshal().json(JsonLibrary.Jackson, OrderInfo.class)
                .end()

                .to(this.deliveryEndpoint("status"))
                .log("Delivery status returned")
                .to("stream:out")
                .unmarshal(jaxbDelivery)
                .process((exchange) -> {
                    DeliveryInfo deliveryInfo = exchange.getMessage().getBody(DeliveryInfo.class);
                    String orderId = exchange.getMessage().getHeader("id", String.class);
                    OrderInfo oi = this.orderService.update(orderId, deliveryInfo);
                    exchange.getMessage().setBody(oi);
                })
                .marshal().json()
                .log("Status finalized")
                .to("stream:out")
                .unmarshal().json(JsonLibrary.Jackson, OrderInfo.class)
        ;
    }

    private String kafkaTopicName(String topicName) {
        return kafkaTopicName(topicName, this.kafkaHost, this.kafkaPort);
    }

    private String kafkaTopicName(String topicName, String host, String port) {
        return String.format("kafka:%s?brokers=%s:%s", topicName, host, port);
    }

    private String deliveryEndpoint(String host, String port, String action) {
        return String.format("spring-ws:http://%s:%s/soap-api/service/delivery/%s", host, port, action);
    }

    private String deliveryEndpoint(String action) {
        return this.deliveryEndpoint(this.deliveryHost, this.deliveryPort, action);
    }

    private void saleErrorHandler() {
        onException(SaleError.class)
                .log("sale error handler fired")
                .process((exchange) -> {
                    String orderId = exchange.getMessage().getHeader(ORDER_ID, String.class);
                    SaleInfo saleInfo = new SaleInfo(orderId, "FAILED", null);
//                    Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    this.orderService.get(orderId).setStatus("CANCELED");
                    OrderInfo orderInfo = this.orderService.update(orderId, saleInfo);
                    exchange.getMessage().setBody(orderInfo);
                })
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("saleHandler"))
                .log("sale error passed")
                .to(this.kafkaTopicName(TOPIC_ORDER_CANCEL))
                .handled(true)
        ;
    }
}
