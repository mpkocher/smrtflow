//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: XXX
//


package com.pacificbiosciences.pacbioreagentkit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.pacificbiosciences.pacbiobasedatamodel.BaseEntityType;


/**
 * <p>Java class for ReagentPlateRowType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReagentPlateRowType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}BaseEntityType">
 *       &lt;attribute name="PlateRow" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="InitialUse" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReagentPlateRowType")
public class ReagentPlateRowType
    extends BaseEntityType
{

    @XmlAttribute(name = "PlateRow", required = true)
    protected String plateRow;
    @XmlAttribute(name = "InitialUse")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar initialUse;

    /**
     * Gets the value of the plateRow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlateRow() {
        return plateRow;
    }

    /**
     * Sets the value of the plateRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlateRow(String value) {
        this.plateRow = value;
    }

    /**
     * Gets the value of the initialUse property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getInitialUse() {
        return initialUse;
    }

    /**
     * Sets the value of the initialUse property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setInitialUse(XMLGregorianCalendar value) {
        this.initialUse = value;
    }

}
