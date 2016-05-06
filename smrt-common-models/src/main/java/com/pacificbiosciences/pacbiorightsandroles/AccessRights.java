//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.04.06 at 11:45:18 AM PDT 
//


package com.pacificbiosciences.pacbiorightsandroles;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioRightsAndRoles.xsd}AccessRight" maxOccurs="unbounded"/>
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
    "accessRight"
})
@XmlRootElement(name = "AccessRights")
public class AccessRights {

    @XmlElement(name = "AccessRight", required = true)
    protected List<AccessRight> accessRight;

    /**
     * Define the functions that a role is capable of accessing
     * 
     * The name attribute should be used to define the Access Right's name.  The Component Name is the right's parent, in the hierarchy of functional access.
     * 
     * The ResourceId, in this case, e.g. svc://admin, should support a wildcard specification, such that an entire hierarchy can be disabled via http://*<!---->/Analysis/*
     * 
     * The AccessDisabled attribute may be used to allow/restrict (default) functionality.
     * Gets the value of the accessRight property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accessRight property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccessRight().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AccessRight }
     * 
     * 
     */
    public List<AccessRight> getAccessRight() {
        if (accessRight == null) {
            accessRight = new ArrayList<AccessRight>();
        }
        return this.accessRight;
    }

}