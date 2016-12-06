//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.06 at 03:16:33 PM PST 
//


package com.pacificbiosciences.pacbiorightsandroles;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.pacificbiosciences.pacbiobasedatamodel.StrictEntityType;


/**
 * <p>Java class for UserIdentityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserIdentityType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}StrictEntityType">
 *       &lt;sequence>
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioRightsAndRoles.xsd}Person"/>
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioRightsAndRoles.xsd}UserPassword"/>
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioRightsAndRoles.xsd}ProjectReferences" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="UserName" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="50"/>
 *             &lt;minLength value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="TokenId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="UseLDAPUri" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="Email">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="96"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="MessageAddress">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;maxLength value="255"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="AccessGrantDate" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}dateTime">
 *             &lt;minInclusive value="1000-01-01T00:00:00"/>
 *             &lt;maxInclusive value="9999-12-31T23:59:59"/>
 *             &lt;pattern value="\p{Nd}{4}-\p{Nd}{2}-\p{Nd}{2}T\p{Nd}{2}:\p{Nd}{2}:\p{Nd}{2}"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="RoleReference" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserIdentityType", propOrder = {
    "person",
    "userPassword",
    "projectReferences"
})
public class UserIdentityType
    extends StrictEntityType
{

    @XmlElement(name = "Person", required = true)
    protected Person person;
    @XmlElement(name = "UserPassword", required = true)
    protected UserPassword userPassword;
    @XmlElement(name = "ProjectReferences")
    protected ProjectReferences projectReferences;
    @XmlAttribute(name = "UserName", required = true)
    protected String userName;
    @XmlAttribute(name = "TokenId")
    protected String tokenId;
    @XmlAttribute(name = "UseLDAPUri")
    protected Boolean useLDAPUri;
    @XmlAttribute(name = "Email")
    protected String email;
    @XmlAttribute(name = "MessageAddress")
    protected String messageAddress;
    @XmlAttribute(name = "AccessGrantDate", required = true)
    protected XMLGregorianCalendar accessGrantDate;
    @XmlAttribute(name = "RoleReference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object roleReference;

    /**
     * An entity to store persons' names and contact info, to associate to users
     * 
     * @return
     *     possible object is
     *     {@link Person }
     *     
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Sets the value of the person property.
     * 
     * @param value
     *     allowed object is
     *     {@link Person }
     *     
     */
    public void setPerson(Person value) {
        this.person = value;
    }

    /**
     * An entity storing hashed password and salt - cached here in case LDAP is not availabel to authenticate, or the user does not exist in LDAP (i.e. adhoc user).
     * 
     * @return
     *     possible object is
     *     {@link UserPassword }
     *     
     */
    public UserPassword getUserPassword() {
        return userPassword;
    }

    /**
     * Sets the value of the userPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserPassword }
     *     
     */
    public void setUserPassword(UserPassword value) {
        this.userPassword = value;
    }

    /**
     * List of projects that the user has access to
     * 
     * @return
     *     possible object is
     *     {@link ProjectReferences }
     *     
     */
    public ProjectReferences getProjectReferences() {
        return projectReferences;
    }

    /**
     * Sets the value of the projectReferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProjectReferences }
     *     
     */
    public void setProjectReferences(ProjectReferences value) {
        this.projectReferences = value;
    }

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the tokenId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * Sets the value of the tokenId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTokenId(String value) {
        this.tokenId = value;
    }

    /**
     * Gets the value of the useLDAPUri property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUseLDAPUri() {
        return useLDAPUri;
    }

    /**
     * Sets the value of the useLDAPUri property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseLDAPUri(Boolean value) {
        this.useLDAPUri = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the messageAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageAddress() {
        return messageAddress;
    }

    /**
     * Sets the value of the messageAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageAddress(String value) {
        this.messageAddress = value;
    }

    /**
     * Gets the value of the accessGrantDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAccessGrantDate() {
        return accessGrantDate;
    }

    /**
     * Sets the value of the accessGrantDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAccessGrantDate(XMLGregorianCalendar value) {
        this.accessGrantDate = value;
    }

    /**
     * Gets the value of the roleReference property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getRoleReference() {
        return roleReference;
    }

    /**
     * Sets the value of the roleReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setRoleReference(Object value) {
        this.roleReference = value;
    }

}
