
package com.shop.delivery.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Delivery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Delivery"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="recipient" type="{http://www.delivery.shop.com/types}Person"/&gt;
 *         &lt;element name="address" type="{http://www.delivery.shop.com/types}Address"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Delivery", propOrder = {
    "recipient",
    "address"
})
public class Delivery {

    @XmlElement(required = true)
    protected Person recipient;
    @XmlElement(required = true)
    protected Address address;

    /**
     * Gets the value of the recipient property.
     * 
     * @return
     *     possible object is
     *     {@link Person }
     *     
     */
    public Person getRecipient() {
        return recipient;
    }

    /**
     * Sets the value of the recipient property.
     * 
     * @param value
     *     allowed object is
     *     {@link Person }
     *     
     */
    public void setRecipient(Person value) {
        this.recipient = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setAddress(Address value) {
        this.address = value;
    }

}
