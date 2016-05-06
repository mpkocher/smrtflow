//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.01 at 12:18:28 AM PDT 
//


package com.pacbio.secondary.analysis.legacy;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.pacbio.secondary.analysis.legacy package. 
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

    private final static QName _ReferenceInfo_QNAME = new QName("", "reference_info");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.pacbio.secondary.analysis.legacy
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReferenceInfoType }
     * 
     */
    public ReferenceInfoType createReferenceInfoType() {
        return new ReferenceInfoType();
    }

    /**
     * Create an instance of {@link OrganismType }
     * 
     */
    public OrganismType createOrganismType() {
        return new OrganismType();
    }

    /**
     * Create an instance of {@link DigestType }
     * 
     */
    public DigestType createDigestType() {
        return new DigestType();
    }

    /**
     * Create an instance of {@link ReferenceType }
     * 
     */
    public ReferenceType createReferenceType() {
        return new ReferenceType();
    }

    /**
     * Create an instance of {@link ContigsType }
     * 
     */
    public ContigsType createContigsType() {
        return new ContigsType();
    }

    /**
     * Create an instance of {@link IndexFileType }
     * 
     */
    public IndexFileType createIndexFileType() {
        return new IndexFileType();
    }

    /**
     * Create an instance of {@link ContigType }
     * 
     */
    public ContigType createContigType() {
        return new ContigType();
    }

    /**
     * Create an instance of {@link FileType }
     * 
     */
    public FileType createFileType() {
        return new FileType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "reference_info")
    public JAXBElement<ReferenceInfoType> createReferenceInfo(ReferenceInfoType value) {
        return new JAXBElement<ReferenceInfoType>(_ReferenceInfo_QNAME, ReferenceInfoType.class, null, value);
    }

}