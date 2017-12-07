//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: XXX
//


package com.pacificbiosciences.pacbiorightsandroles;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com.pacificbiosciences.pacbiobasedatamodel.StrictEntityType;


/**
 * <p>Java class for AccessRightType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AccessRightType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}StrictEntityType">
 *       &lt;attribute name="InternalResourceAddress" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="50"/>
 *             &lt;minLength value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="ComponentName">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="255"/>
 *             &lt;minLength value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="AccessDisabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="Operation" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="RequiresAudit" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="RequiresESig" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccessRightType")
@XmlSeeAlso({
    AccessRight.class
})
public class AccessRightType
    extends StrictEntityType
{

    @XmlAttribute(name = "InternalResourceAddress", required = true)
    protected String internalResourceAddress;
    @XmlAttribute(name = "ComponentName")
    protected String componentName;
    @XmlAttribute(name = "AccessDisabled")
    protected Boolean accessDisabled;
    @XmlAttribute(name = "Operation")
    protected String operation;
    @XmlAttribute(name = "RequiresAudit", required = true)
    protected boolean requiresAudit;
    @XmlAttribute(name = "RequiresESig", required = true)
    protected boolean requiresESig;

    /**
     * Gets the value of the internalResourceAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInternalResourceAddress() {
        return internalResourceAddress;
    }

    /**
     * Sets the value of the internalResourceAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInternalResourceAddress(String value) {
        this.internalResourceAddress = value;
    }

    /**
     * Gets the value of the componentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Sets the value of the componentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentName(String value) {
        this.componentName = value;
    }

    /**
     * Gets the value of the accessDisabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAccessDisabled() {
        if (accessDisabled == null) {
            return false;
        } else {
            return accessDisabled;
        }
    }

    /**
     * Sets the value of the accessDisabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAccessDisabled(Boolean value) {
        this.accessDisabled = value;
    }

    /**
     * Gets the value of the operation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the value of the operation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperation(String value) {
        this.operation = value;
    }

    /**
     * Gets the value of the requiresAudit property.
     * 
     */
    public boolean isRequiresAudit() {
        return requiresAudit;
    }

    /**
     * Sets the value of the requiresAudit property.
     * 
     */
    public void setRequiresAudit(boolean value) {
        this.requiresAudit = value;
    }

    /**
     * Gets the value of the requiresESig property.
     * 
     */
    public boolean isRequiresESig() {
        return requiresESig;
    }

    /**
     * Sets the value of the requiresESig property.
     * 
     */
    public void setRequiresESig(boolean value) {
        this.requiresESig = value;
    }

}
