//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: XXX
//


package com.pacificbiosciences.pacbiobasedatamodel;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Discrete distribution class
 * 
 * <p>Java class for StatsDiscreteDistType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatsDiscreteDistType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}BaseEntityType">
 *       &lt;sequence>
 *         &lt;element name="NumBins" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="BinCounts">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="BinCount" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="MetricDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="BinLabels">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="BinLabel" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatsDiscreteDistType", propOrder = {
    "numBins",
    "binCounts",
    "metricDescription",
    "binLabels"
})
public class StatsDiscreteDistType
    extends BaseEntityType
{

    @XmlElement(name = "NumBins")
    protected int numBins;
    @XmlElement(name = "BinCounts", required = true)
    protected StatsDiscreteDistType.BinCounts binCounts;
    @XmlElement(name = "MetricDescription", required = true)
    protected String metricDescription;
    @XmlElement(name = "BinLabels", required = true)
    protected StatsDiscreteDistType.BinLabels binLabels;

    /**
     * Gets the value of the numBins property.
     * 
     */
    public int getNumBins() {
        return numBins;
    }

    /**
     * Sets the value of the numBins property.
     * 
     */
    public void setNumBins(int value) {
        this.numBins = value;
    }

    /**
     * Gets the value of the binCounts property.
     * 
     * @return
     *     possible object is
     *     {@link StatsDiscreteDistType.BinCounts }
     *     
     */
    public StatsDiscreteDistType.BinCounts getBinCounts() {
        return binCounts;
    }

    /**
     * Sets the value of the binCounts property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatsDiscreteDistType.BinCounts }
     *     
     */
    public void setBinCounts(StatsDiscreteDistType.BinCounts value) {
        this.binCounts = value;
    }

    /**
     * Gets the value of the metricDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetricDescription() {
        return metricDescription;
    }

    /**
     * Sets the value of the metricDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetricDescription(String value) {
        this.metricDescription = value;
    }

    /**
     * Gets the value of the binLabels property.
     * 
     * @return
     *     possible object is
     *     {@link StatsDiscreteDistType.BinLabels }
     *     
     */
    public StatsDiscreteDistType.BinLabels getBinLabels() {
        return binLabels;
    }

    /**
     * Sets the value of the binLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatsDiscreteDistType.BinLabels }
     *     
     */
    public void setBinLabels(StatsDiscreteDistType.BinLabels value) {
        this.binLabels = value;
    }


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
     *         &lt;element name="BinCount" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
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
        "binCount"
    })
    public static class BinCounts {

        @XmlElement(name = "BinCount", type = Integer.class)
        protected List<Integer> binCount;

        /**
         * Gets the value of the binCount property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the binCount property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getBinCount().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Integer }
         * 
         * 
         */
        public List<Integer> getBinCount() {
            if (binCount == null) {
                binCount = new ArrayList<Integer>();
            }
            return this.binCount;
        }

    }


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
     *         &lt;element name="BinLabel" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
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
        "binLabel"
    })
    public static class BinLabels {

        @XmlElement(name = "BinLabel", required = true)
        protected List<String> binLabel;

        /**
         * Gets the value of the binLabel property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the binLabel property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getBinLabel().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getBinLabel() {
            if (binLabel == null) {
                binLabel = new ArrayList<String>();
            }
            return this.binLabel;
        }

    }

}
