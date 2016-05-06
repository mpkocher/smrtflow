/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.pacbio.common.models.contracts;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class PacBioOptions extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"PacBioOptions\",\"namespace\":\"com.pacbio.common.models.contracts\",\"fields\":[{\"name\":\"pb_option\",\"type\":{\"type\":\"record\",\"name\":\"pb_option\",\"fields\":[{\"name\":\"default\",\"type\":[\"int\",\"string\",\"boolean\",\"float\"]},{\"name\":\"option_id\",\"type\":\"string\"},{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"description\",\"type\":\"string\"}]}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public com.pacbio.common.models.contracts.pb_option pb_option;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public PacBioOptions() {}

  /**
   * All-args constructor.
   */
  public PacBioOptions(com.pacbio.common.models.contracts.pb_option pb_option) {
    this.pb_option = pb_option;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return pb_option;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: pb_option = (com.pacbio.common.models.contracts.pb_option)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'pb_option' field.
   */
  public com.pacbio.common.models.contracts.pb_option getPbOption() {
    return pb_option;
  }

  /**
   * Sets the value of the 'pb_option' field.
   * @param value the value to set.
   */
  public void setPbOption(com.pacbio.common.models.contracts.pb_option value) {
    this.pb_option = value;
  }

  /** Creates a new PacBioOptions RecordBuilder */
  public static com.pacbio.common.models.contracts.PacBioOptions.Builder newBuilder() {
    return new com.pacbio.common.models.contracts.PacBioOptions.Builder();
  }
  
  /** Creates a new PacBioOptions RecordBuilder by copying an existing Builder */
  public static com.pacbio.common.models.contracts.PacBioOptions.Builder newBuilder(com.pacbio.common.models.contracts.PacBioOptions.Builder other) {
    return new com.pacbio.common.models.contracts.PacBioOptions.Builder(other);
  }
  
  /** Creates a new PacBioOptions RecordBuilder by copying an existing PacBioOptions instance */
  public static com.pacbio.common.models.contracts.PacBioOptions.Builder newBuilder(com.pacbio.common.models.contracts.PacBioOptions other) {
    return new com.pacbio.common.models.contracts.PacBioOptions.Builder(other);
  }
  
  /**
   * RecordBuilder for PacBioOptions instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<PacBioOptions>
    implements org.apache.avro.data.RecordBuilder<PacBioOptions> {

    private com.pacbio.common.models.contracts.pb_option pb_option;

    /** Creates a new Builder */
    private Builder() {
      super(com.pacbio.common.models.contracts.PacBioOptions.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.pacbio.common.models.contracts.PacBioOptions.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.pb_option)) {
        this.pb_option = data().deepCopy(fields()[0].schema(), other.pb_option);
        fieldSetFlags()[0] = true;
      }
    }
    
    /** Creates a Builder by copying an existing PacBioOptions instance */
    private Builder(com.pacbio.common.models.contracts.PacBioOptions other) {
            super(com.pacbio.common.models.contracts.PacBioOptions.SCHEMA$);
      if (isValidValue(fields()[0], other.pb_option)) {
        this.pb_option = data().deepCopy(fields()[0].schema(), other.pb_option);
        fieldSetFlags()[0] = true;
      }
    }

    /** Gets the value of the 'pb_option' field */
    public com.pacbio.common.models.contracts.pb_option getPbOption() {
      return pb_option;
    }
    
    /** Sets the value of the 'pb_option' field */
    public com.pacbio.common.models.contracts.PacBioOptions.Builder setPbOption(com.pacbio.common.models.contracts.pb_option value) {
      validate(fields()[0], value);
      this.pb_option = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'pb_option' field has been set */
    public boolean hasPbOption() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'pb_option' field */
    public com.pacbio.common.models.contracts.PacBioOptions.Builder clearPbOption() {
      pb_option = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public PacBioOptions build() {
      try {
        PacBioOptions record = new PacBioOptions();
        record.pb_option = fieldSetFlags()[0] ? this.pb_option : (com.pacbio.common.models.contracts.pb_option) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}