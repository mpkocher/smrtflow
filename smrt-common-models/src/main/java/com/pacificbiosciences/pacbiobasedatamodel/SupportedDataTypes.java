//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.04.06 at 11:45:18 AM PDT 
//


package com.pacificbiosciences.pacbiobasedatamodel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SupportedDataTypes.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SupportedDataTypes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Int16"/>
 *     &lt;enumeration value="Int32"/>
 *     &lt;enumeration value="Int64"/>
 *     &lt;enumeration value="UInt16"/>
 *     &lt;enumeration value="UInt32"/>
 *     &lt;enumeration value="UInt64"/>
 *     &lt;enumeration value="Single"/>
 *     &lt;enumeration value="Double"/>
 *     &lt;enumeration value="String"/>
 *     &lt;enumeration value="DateTime"/>
 *     &lt;enumeration value="Int16_1D"/>
 *     &lt;enumeration value="Int32_1D"/>
 *     &lt;enumeration value="Int64_1D"/>
 *     &lt;enumeration value="UInt16_1D"/>
 *     &lt;enumeration value="UInt32_1D"/>
 *     &lt;enumeration value="UInt64_1D"/>
 *     &lt;enumeration value="Single_1D"/>
 *     &lt;enumeration value="Double_1D"/>
 *     &lt;enumeration value="String_1D"/>
 *     &lt;enumeration value="DateTime_1D"/>
 *     &lt;enumeration value="Int16_2D"/>
 *     &lt;enumeration value="Int32_2D"/>
 *     &lt;enumeration value="Int64_2D"/>
 *     &lt;enumeration value="UInt16_2D"/>
 *     &lt;enumeration value="UInt32_2D"/>
 *     &lt;enumeration value="UInt64_2D"/>
 *     &lt;enumeration value="Single_2D"/>
 *     &lt;enumeration value="Double_2D"/>
 *     &lt;enumeration value="String_2D"/>
 *     &lt;enumeration value="DateTime_2D"/>
 *     &lt;enumeration value="XML"/>
 *     &lt;enumeration value="JSON"/>
 *     &lt;enumeration value="Object"/>
 *     &lt;enumeration value="Other"/>
 *     &lt;enumeration value="Unknown"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SupportedDataTypes")
@XmlEnum
public enum SupportedDataTypes {

    @XmlEnumValue("Int16")
    INT_16("Int16"),
    @XmlEnumValue("Int32")
    INT_32("Int32"),
    @XmlEnumValue("Int64")
    INT_64("Int64"),
    @XmlEnumValue("UInt16")
    U_INT_16("UInt16"),
    @XmlEnumValue("UInt32")
    U_INT_32("UInt32"),
    @XmlEnumValue("UInt64")
    U_INT_64("UInt64"),
    @XmlEnumValue("Single")
    SINGLE("Single"),
    @XmlEnumValue("Double")
    DOUBLE("Double"),
    @XmlEnumValue("String")
    STRING("String"),
    @XmlEnumValue("DateTime")
    DATE_TIME("DateTime"),
    @XmlEnumValue("Int16_1D")
    INT_16_1_D("Int16_1D"),
    @XmlEnumValue("Int32_1D")
    INT_32_1_D("Int32_1D"),
    @XmlEnumValue("Int64_1D")
    INT_64_1_D("Int64_1D"),
    @XmlEnumValue("UInt16_1D")
    U_INT_16_1_D("UInt16_1D"),
    @XmlEnumValue("UInt32_1D")
    U_INT_32_1_D("UInt32_1D"),
    @XmlEnumValue("UInt64_1D")
    U_INT_64_1_D("UInt64_1D"),
    @XmlEnumValue("Single_1D")
    SINGLE_1_D("Single_1D"),
    @XmlEnumValue("Double_1D")
    DOUBLE_1_D("Double_1D"),
    @XmlEnumValue("String_1D")
    STRING_1_D("String_1D"),
    @XmlEnumValue("DateTime_1D")
    DATE_TIME_1_D("DateTime_1D"),
    @XmlEnumValue("Int16_2D")
    INT_16_2_D("Int16_2D"),
    @XmlEnumValue("Int32_2D")
    INT_32_2_D("Int32_2D"),
    @XmlEnumValue("Int64_2D")
    INT_64_2_D("Int64_2D"),
    @XmlEnumValue("UInt16_2D")
    U_INT_16_2_D("UInt16_2D"),
    @XmlEnumValue("UInt32_2D")
    U_INT_32_2_D("UInt32_2D"),
    @XmlEnumValue("UInt64_2D")
    U_INT_64_2_D("UInt64_2D"),
    @XmlEnumValue("Single_2D")
    SINGLE_2_D("Single_2D"),
    @XmlEnumValue("Double_2D")
    DOUBLE_2_D("Double_2D"),
    @XmlEnumValue("String_2D")
    STRING_2_D("String_2D"),
    @XmlEnumValue("DateTime_2D")
    DATE_TIME_2_D("DateTime_2D"),
    XML("XML"),
    JSON("JSON"),
    @XmlEnumValue("Object")
    OBJECT("Object"),
    @XmlEnumValue("Other")
    OTHER("Other"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown");
    private final String value;

    SupportedDataTypes(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SupportedDataTypes fromValue(String v) {
        for (SupportedDataTypes c: SupportedDataTypes.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}