//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: XXX
//


package com.pacificbiosciences.pacbiodatasets;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Type for a Contig DataSet.
 * 
 * <p>Java class for ContigSetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContigSetType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioDatasets.xsd}DataSetType">
 *       &lt;sequence>
 *         &lt;element name="DataSetMetadata" type="{http://pacificbiosciences.com/PacBioDatasets.xsd}ContigSetMetadataType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContigSetType", propOrder = {
    "dataSetMetadata"
})
@XmlSeeAlso({
    GmapReferenceSet.class,
    ContigSet.class,
    ReferenceSet.class
})
public class ContigSetType
    extends DataSetType
{

    @XmlElement(name = "DataSetMetadata", required = true)
    protected ContigSetMetadataType dataSetMetadata;

    /**
     * Gets the value of the dataSetMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link ContigSetMetadataType }
     *     
     */
    public ContigSetMetadataType getDataSetMetadata() {
        return dataSetMetadata;
    }

    /**
     * Sets the value of the dataSetMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContigSetMetadataType }
     *     
     */
    public void setDataSetMetadata(ContigSetMetadataType value) {
        this.dataSetMetadata = value;
    }

}
