/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.pacbio.common.models.reports;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class ReportAttribute extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ReportAttribute\",\"namespace\":\"com.pacbio.common.models.reports\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"value\",\"type\":[\"string\",\"int\",\"float\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence id;
  @Deprecated public java.lang.CharSequence name;
  @Deprecated public java.lang.Object value;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public ReportAttribute() {}

  /**
   * All-args constructor.
   */
  public ReportAttribute(java.lang.CharSequence id, java.lang.CharSequence name, java.lang.Object value) {
    this.id = id;
    this.name = name;
    this.value = value;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return name;
    case 2: return value;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.CharSequence)value$; break;
    case 1: name = (java.lang.CharSequence)value$; break;
    case 2: value = (java.lang.Object)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   */
  public java.lang.CharSequence getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.CharSequence value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'value' field.
   */
  public java.lang.Object getValue() {
    return value;
  }

  /**
   * Sets the value of the 'value' field.
   * @param value the value to set.
   */
  public void setValue(java.lang.Object value) {
    this.value = value;
  }

  /** Creates a new ReportAttribute RecordBuilder */
  public static com.pacbio.common.models.reports.ReportAttribute.Builder newBuilder() {
    return new com.pacbio.common.models.reports.ReportAttribute.Builder();
  }
  
  /** Creates a new ReportAttribute RecordBuilder by copying an existing Builder */
  public static com.pacbio.common.models.reports.ReportAttribute.Builder newBuilder(com.pacbio.common.models.reports.ReportAttribute.Builder other) {
    return new com.pacbio.common.models.reports.ReportAttribute.Builder(other);
  }
  
  /** Creates a new ReportAttribute RecordBuilder by copying an existing ReportAttribute instance */
  public static com.pacbio.common.models.reports.ReportAttribute.Builder newBuilder(com.pacbio.common.models.reports.ReportAttribute other) {
    return new com.pacbio.common.models.reports.ReportAttribute.Builder(other);
  }
  
  /**
   * RecordBuilder for ReportAttribute instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ReportAttribute>
    implements org.apache.avro.data.RecordBuilder<ReportAttribute> {

    private java.lang.CharSequence id;
    private java.lang.CharSequence name;
    private java.lang.Object value;

    /** Creates a new Builder */
    private Builder() {
      super(com.pacbio.common.models.reports.ReportAttribute.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.pacbio.common.models.reports.ReportAttribute.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.name)) {
        this.name = data().deepCopy(fields()[1].schema(), other.name);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.value)) {
        this.value = data().deepCopy(fields()[2].schema(), other.value);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing ReportAttribute instance */
    private Builder(com.pacbio.common.models.reports.ReportAttribute other) {
            super(com.pacbio.common.models.reports.ReportAttribute.SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.name)) {
        this.name = data().deepCopy(fields()[1].schema(), other.name);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.value)) {
        this.value = data().deepCopy(fields()[2].schema(), other.value);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'id' field */
    public java.lang.CharSequence getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public com.pacbio.common.models.reports.ReportAttribute.Builder setId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'id' field has been set */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'id' field */
    public com.pacbio.common.models.reports.ReportAttribute.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public com.pacbio.common.models.reports.ReportAttribute.Builder setName(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.name = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'name' field has been set */
    public boolean hasName() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'name' field */
    public com.pacbio.common.models.reports.ReportAttribute.Builder clearName() {
      name = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'value' field */
    public java.lang.Object getValue() {
      return value;
    }
    
    /** Sets the value of the 'value' field */
    public com.pacbio.common.models.reports.ReportAttribute.Builder setValue(java.lang.Object value) {
      validate(fields()[2], value);
      this.value = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'value' field has been set */
    public boolean hasValue() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'value' field */
    public com.pacbio.common.models.reports.ReportAttribute.Builder clearValue() {
      value = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public ReportAttribute build() {
      try {
        ReportAttribute record = new ReportAttribute();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.name = fieldSetFlags()[1] ? this.name : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.value = fieldSetFlags()[2] ? this.value : (java.lang.Object) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}