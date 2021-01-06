
package com.shop.delivery;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com.shop.delivery.types.DeliveryInfo;
import com.shop.delivery.types.Fault;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.shop.delivery package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DeliveryResponse_QNAME = new QName("http://www.delivery.shop.com", "deliveryResponse");
    private final static QName _DeliveryStatusResponse_QNAME = new QName("http://www.delivery.shop.com", "deliveryStatusResponse");
    private final static QName _CancelDeliveryResponse_QNAME = new QName("http://www.delivery.shop.com", "cancelDeliveryResponse");
    private final static QName _DeliveryError_QNAME = new QName("http://www.delivery.shop.com", "deliveryError");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.shop.delivery
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeliveryRequest }
     * 
     */
    public DeliveryRequest createDeliveryRequest() {
        return new DeliveryRequest();
    }

    /**
     * Create an instance of {@link DeliveryStatusRequest }
     * 
     */
    public DeliveryStatusRequest createDeliveryStatusRequest() {
        return new DeliveryStatusRequest();
    }

    /**
     * Create an instance of {@link CancelDeliveryRequest }
     * 
     */
    public CancelDeliveryRequest createCancelDeliveryRequest() {
        return new CancelDeliveryRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeliveryInfo }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DeliveryInfo }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.delivery.shop.com", name = "deliveryResponse")
    public JAXBElement<DeliveryInfo> createDeliveryResponse(DeliveryInfo value) {
        return new JAXBElement<DeliveryInfo>(_DeliveryResponse_QNAME, DeliveryInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeliveryInfo }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DeliveryInfo }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.delivery.shop.com", name = "deliveryStatusResponse")
    public JAXBElement<DeliveryInfo> createDeliveryStatusResponse(DeliveryInfo value) {
        return new JAXBElement<DeliveryInfo>(_DeliveryStatusResponse_QNAME, DeliveryInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeliveryInfo }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DeliveryInfo }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.delivery.shop.com", name = "cancelDeliveryResponse")
    public JAXBElement<DeliveryInfo> createCancelDeliveryResponse(DeliveryInfo value) {
        return new JAXBElement<DeliveryInfo>(_CancelDeliveryResponse_QNAME, DeliveryInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Fault }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Fault }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.delivery.shop.com", name = "deliveryError")
    public JAXBElement<Fault> createDeliveryError(Fault value) {
        return new JAXBElement<Fault>(_DeliveryError_QNAME, Fault.class, null, value);
    }

}
