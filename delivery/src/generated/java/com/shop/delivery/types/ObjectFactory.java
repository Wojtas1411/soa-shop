
package com.shop.delivery.types;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.shop.delivery.types package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.shop.delivery.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Delivery }
     * 
     */
    public Delivery createDelivery() {
        return new Delivery();
    }

    /**
     * Create an instance of {@link PaymentCard }
     * 
     */
    public PaymentCard createPaymentCard() {
        return new PaymentCard();
    }

    /**
     * Create an instance of {@link Person }
     * 
     */
    public Person createPerson() {
        return new Person();
    }

    /**
     * Create an instance of {@link Address }
     * 
     */
    public Address createAddress() {
        return new Address();
    }

    /**
     * Create an instance of {@link DeliveryInfo }
     * 
     */
    public DeliveryInfo createDeliveryInfo() {
        return new DeliveryInfo();
    }

    /**
     * Create an instance of {@link Fault }
     * 
     */
    public Fault createFault() {
        return new Fault();
    }

}
