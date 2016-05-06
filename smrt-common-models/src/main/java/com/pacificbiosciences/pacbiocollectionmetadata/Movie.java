//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.04.06 at 11:45:18 AM PDT 
//


package com.pacificbiosciences.pacbiocollectionmetadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="WhenStarted" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="DurationInSec" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Number" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "whenStarted",
    "durationInSec",
    "number"
})
@XmlRootElement(name = "Movie")
public class Movie {

    @XmlElement(name = "WhenStarted", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar whenStarted;
    @XmlElement(name = "DurationInSec", defaultValue = "0")
    protected int durationInSec;
    @XmlElement(name = "Number", defaultValue = "0")
    protected int number;

    /**
     * Gets the value of the whenStarted property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getWhenStarted() {
        return whenStarted;
    }

    /**
     * Sets the value of the whenStarted property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setWhenStarted(XMLGregorianCalendar value) {
        this.whenStarted = value;
    }

    /**
     * Gets the value of the durationInSec property.
     * 
     */
    public int getDurationInSec() {
        return durationInSec;
    }

    /**
     * Sets the value of the durationInSec property.
     * 
     */
    public void setDurationInSec(int value) {
        this.durationInSec = value;
    }

    /**
     * Gets the value of the number property.
     * 
     */
    public int getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     */
    public void setNumber(int value) {
        this.number = value;
    }

}