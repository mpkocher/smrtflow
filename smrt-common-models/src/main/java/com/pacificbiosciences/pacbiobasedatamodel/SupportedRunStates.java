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
 * <p>Java class for SupportedRunStates.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SupportedRunStates">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Ready"/>
 *     &lt;enumeration value="Idle"/>
 *     &lt;enumeration value="System Test"/>
 *     &lt;enumeration value="Starting"/>
 *     &lt;enumeration value="Running"/>
 *     &lt;enumeration value="Aborting"/>
 *     &lt;enumeration value="Aborted"/>
 *     &lt;enumeration value="Terminated"/>
 *     &lt;enumeration value="Completing"/>
 *     &lt;enumeration value="Complete"/>
 *     &lt;enumeration value="Paused"/>
 *     &lt;enumeration value="Unknown"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SupportedRunStates")
@XmlEnum
public enum SupportedRunStates {

    @XmlEnumValue("Ready")
    READY("Ready"),
    @XmlEnumValue("Idle")
    IDLE("Idle"),
    @XmlEnumValue("System Test")
    SYSTEM_TEST("System Test"),
    @XmlEnumValue("Starting")
    STARTING("Starting"),
    @XmlEnumValue("Running")
    RUNNING("Running"),
    @XmlEnumValue("Aborting")
    ABORTING("Aborting"),
    @XmlEnumValue("Aborted")
    ABORTED("Aborted"),
    @XmlEnumValue("Terminated")
    TERMINATED("Terminated"),
    @XmlEnumValue("Completing")
    COMPLETING("Completing"),
    @XmlEnumValue("Complete")
    COMPLETE("Complete"),
    @XmlEnumValue("Paused")
    PAUSED("Paused"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown");
    private final String value;

    SupportedRunStates(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SupportedRunStates fromValue(String v) {
        for (SupportedRunStates c: SupportedRunStates.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}