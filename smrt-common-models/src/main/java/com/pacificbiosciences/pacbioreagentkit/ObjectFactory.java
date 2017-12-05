//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.12.04 at 02:50:32 PM PST 
//


package com.pacificbiosciences.pacbioreagentkit;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.pacificbiosciences.pacbioreagentkit package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Reagent_QNAME = new QName("http://pacificbiosciences.com/PacBioReagentKit.xsd", "Reagent");
    private final static QName _ReagentPlateRow_QNAME = new QName("http://pacificbiosciences.com/PacBioReagentKit.xsd", "ReagentPlateRow");
    private final static QName _ReagentKit_QNAME = new QName("http://pacificbiosciences.com/PacBioReagentKit.xsd", "ReagentKit");
    private final static QName _ReagentTube_QNAME = new QName("http://pacificbiosciences.com/PacBioReagentKit.xsd", "ReagentTube");
    private final static QName _ReagentKitTypeAutomations_QNAME = new QName("http://pacificbiosciences.com/PacBioReagentKit.xsd", "Automations");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.pacificbiosciences.pacbioreagentkit
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReagentKitType }
     * 
     */
    public ReagentKitType createReagentKitType() {
        return new ReagentKitType();
    }

    /**
     * Create an instance of {@link SupplyKitSequencing }
     * 
     */
    public SupplyKitSequencing createSupplyKitSequencing() {
        return new SupplyKitSequencing();
    }

    /**
     * Create an instance of {@link ReagentTubeType }
     * 
     */
    public ReagentTubeType createReagentTubeType() {
        return new ReagentTubeType();
    }

    /**
     * Create an instance of {@link PacBioReagentKit }
     * 
     */
    public PacBioReagentKit createPacBioReagentKit() {
        return new PacBioReagentKit();
    }

    /**
     * Create an instance of {@link ReagentType }
     * 
     */
    public ReagentType createReagentType() {
        return new ReagentType();
    }

    /**
     * Create an instance of {@link ReagentPlateRowType }
     * 
     */
    public ReagentPlateRowType createReagentPlateRowType() {
        return new ReagentPlateRowType();
    }

    /**
     * Create an instance of {@link ReagentKitType.Reagents }
     * 
     */
    public ReagentKitType.Reagents createReagentKitTypeReagents() {
        return new ReagentKitType.Reagents();
    }

    /**
     * Create an instance of {@link ReagentKitType.ReagentTubes }
     * 
     */
    public ReagentKitType.ReagentTubes createReagentKitTypeReagentTubes() {
        return new ReagentKitType.ReagentTubes();
    }

    /**
     * Create an instance of {@link ReagentKitType.ReagentPlateRows }
     * 
     */
    public ReagentKitType.ReagentPlateRows createReagentKitTypeReagentPlateRows() {
        return new ReagentKitType.ReagentPlateRows();
    }

    /**
     * Create an instance of {@link ReagentKitType.Automations }
     * 
     */
    public ReagentKitType.Automations createReagentKitTypeAutomations() {
        return new ReagentKitType.Automations();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReagentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pacificbiosciences.com/PacBioReagentKit.xsd", name = "Reagent")
    public JAXBElement<ReagentType> createReagent(ReagentType value) {
        return new JAXBElement<ReagentType>(_Reagent_QNAME, ReagentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReagentPlateRowType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pacificbiosciences.com/PacBioReagentKit.xsd", name = "ReagentPlateRow")
    public JAXBElement<ReagentPlateRowType> createReagentPlateRow(ReagentPlateRowType value) {
        return new JAXBElement<ReagentPlateRowType>(_ReagentPlateRow_QNAME, ReagentPlateRowType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReagentKitType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pacificbiosciences.com/PacBioReagentKit.xsd", name = "ReagentKit")
    public JAXBElement<ReagentKitType> createReagentKit(ReagentKitType value) {
        return new JAXBElement<ReagentKitType>(_ReagentKit_QNAME, ReagentKitType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReagentTubeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pacificbiosciences.com/PacBioReagentKit.xsd", name = "ReagentTube")
    public JAXBElement<ReagentTubeType> createReagentTube(ReagentTubeType value) {
        return new JAXBElement<ReagentTubeType>(_ReagentTube_QNAME, ReagentTubeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReagentKitType.Automations }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pacificbiosciences.com/PacBioReagentKit.xsd", name = "Automations", scope = ReagentKitType.class)
    public JAXBElement<ReagentKitType.Automations> createReagentKitTypeAutomations(ReagentKitType.Automations value) {
        return new JAXBElement<ReagentKitType.Automations>(_ReagentKitTypeAutomations_QNAME, ReagentKitType.Automations.class, ReagentKitType.class, value);
    }

}
