//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.06 at 03:16:33 PM PST 
//


package com.pacificbiosciences.pacbiobasedatamodel;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A container for a set of analogs
 * 
 * <p>Java class for SequencingChemistry complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SequencingChemistry">
 *   &lt;complexContent>
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}DataEntityType">
 *       &lt;sequence>
 *         &lt;element name="DyeSet">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}BaseEntityType">
 *                 &lt;sequence>
 *                   &lt;element name="Analogs">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Analog" type="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}AnalogType" maxOccurs="4"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/extension>
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
@XmlType(name = "SequencingChemistry", propOrder = {
    "dyeSet"
})
public class SequencingChemistry
    extends DataEntityType
{

    @XmlElement(name = "DyeSet", required = true)
    protected SequencingChemistry.DyeSet dyeSet;

    /**
     * Gets the value of the dyeSet property.
     * 
     * @return
     *     possible object is
     *     {@link SequencingChemistry.DyeSet }
     *     
     */
    public SequencingChemistry.DyeSet getDyeSet() {
        return dyeSet;
    }

    /**
     * Sets the value of the dyeSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link SequencingChemistry.DyeSet }
     *     
     */
    public void setDyeSet(SequencingChemistry.DyeSet value) {
        this.dyeSet = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}BaseEntityType">
     *       &lt;sequence>
     *         &lt;element name="Analogs">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="Analog" type="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}AnalogType" maxOccurs="4"/>
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
    @XmlType(name = "", propOrder = {
        "analogs"
    })
    public static class DyeSet
        extends BaseEntityType
    {

        @XmlElement(name = "Analogs", required = true)
        protected SequencingChemistry.DyeSet.Analogs analogs;

        /**
         * Gets the value of the analogs property.
         * 
         * @return
         *     possible object is
         *     {@link SequencingChemistry.DyeSet.Analogs }
         *     
         */
        public SequencingChemistry.DyeSet.Analogs getAnalogs() {
            return analogs;
        }

        /**
         * Sets the value of the analogs property.
         * 
         * @param value
         *     allowed object is
         *     {@link SequencingChemistry.DyeSet.Analogs }
         *     
         */
        public void setAnalogs(SequencingChemistry.DyeSet.Analogs value) {
            this.analogs = value;
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
         *         &lt;element name="Analog" type="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}AnalogType" maxOccurs="4"/>
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
            "analog"
        })
        public static class Analogs {

            @XmlElement(name = "Analog", required = true)
            protected List<AnalogType> analog;

            /**
             * Gets the value of the analog property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the analog property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAnalog().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link AnalogType }
             * 
             * 
             */
            public List<AnalogType> getAnalog() {
                if (analog == null) {
                    analog = new ArrayList<AnalogType>();
                }
                return this.analog;
            }

        }

    }

}
