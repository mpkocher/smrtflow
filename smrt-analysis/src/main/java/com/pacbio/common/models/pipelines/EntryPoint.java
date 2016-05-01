/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.pacbio.common.models.pipelines;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class EntryPoint extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"EntryPoint\",\"namespace\":\"com.pacbio.common.models.pipelines\",\"fields\":[{\"name\":\"entryId\",\"type\":\"string\",\"doc\":\"Entry Id\"},{\"name\":\"fileTypeId\",\"type\":\"string\",\"doc\":\"PacBio File Type identifier\"},{\"name\":\"name\",\"type\":\"string\",\"doc\":\"Display Name\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  /** Entry Id */
  @Deprecated public java.lang.CharSequence entryId;
  /** PacBio File Type identifier */
  @Deprecated public java.lang.CharSequence fileTypeId;
  /** Display Name */
  @Deprecated public java.lang.CharSequence name;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public EntryPoint() {}

  /**
   * All-args constructor.
   */
  public EntryPoint(java.lang.CharSequence entryId, java.lang.CharSequence fileTypeId, java.lang.CharSequence name) {
    this.entryId = entryId;
    this.fileTypeId = fileTypeId;
    this.name = name;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return entryId;
    case 1: return fileTypeId;
    case 2: return name;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: entryId = (java.lang.CharSequence)value$; break;
    case 1: fileTypeId = (java.lang.CharSequence)value$; break;
    case 2: name = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'entryId' field.
   * Entry Id   */
  public java.lang.CharSequence getEntryId() {
    return entryId;
  }

  /**
   * Sets the value of the 'entryId' field.
   * Entry Id   * @param value the value to set.
   */
  public void setEntryId(java.lang.CharSequence value) {
    this.entryId = value;
  }

  /**
   * Gets the value of the 'fileTypeId' field.
   * PacBio File Type identifier   */
  public java.lang.CharSequence getFileTypeId() {
    return fileTypeId;
  }

  /**
   * Sets the value of the 'fileTypeId' field.
   * PacBio File Type identifier   * @param value the value to set.
   */
  public void setFileTypeId(java.lang.CharSequence value) {
    this.fileTypeId = value;
  }

  /**
   * Gets the value of the 'name' field.
   * Display Name   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * Display Name   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /** Creates a new EntryPoint RecordBuilder */
  public static com.pacbio.common.models.pipelines.EntryPoint.Builder newBuilder() {
    return new com.pacbio.common.models.pipelines.EntryPoint.Builder();
  }
  
  /** Creates a new EntryPoint RecordBuilder by copying an existing Builder */
  public static com.pacbio.common.models.pipelines.EntryPoint.Builder newBuilder(com.pacbio.common.models.pipelines.EntryPoint.Builder other) {
    return new com.pacbio.common.models.pipelines.EntryPoint.Builder(other);
  }
  
  /** Creates a new EntryPoint RecordBuilder by copying an existing EntryPoint instance */
  public static com.pacbio.common.models.pipelines.EntryPoint.Builder newBuilder(com.pacbio.common.models.pipelines.EntryPoint other) {
    return new com.pacbio.common.models.pipelines.EntryPoint.Builder(other);
  }
  
  /**
   * RecordBuilder for EntryPoint instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<EntryPoint>
    implements org.apache.avro.data.RecordBuilder<EntryPoint> {

    private java.lang.CharSequence entryId;
    private java.lang.CharSequence fileTypeId;
    private java.lang.CharSequence name;

    /** Creates a new Builder */
    private Builder() {
      super(com.pacbio.common.models.pipelines.EntryPoint.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.pacbio.common.models.pipelines.EntryPoint.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.entryId)) {
        this.entryId = data().deepCopy(fields()[0].schema(), other.entryId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.fileTypeId)) {
        this.fileTypeId = data().deepCopy(fields()[1].schema(), other.fileTypeId);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.name)) {
        this.name = data().deepCopy(fields()[2].schema(), other.name);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing EntryPoint instance */
    private Builder(com.pacbio.common.models.pipelines.EntryPoint other) {
            super(com.pacbio.common.models.pipelines.EntryPoint.SCHEMA$);
      if (isValidValue(fields()[0], other.entryId)) {
        this.entryId = data().deepCopy(fields()[0].schema(), other.entryId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.fileTypeId)) {
        this.fileTypeId = data().deepCopy(fields()[1].schema(), other.fileTypeId);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.name)) {
        this.name = data().deepCopy(fields()[2].schema(), other.name);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'entryId' field */
    public java.lang.CharSequence getEntryId() {
      return entryId;
    }
    
    /** Sets the value of the 'entryId' field */
    public com.pacbio.common.models.pipelines.EntryPoint.Builder setEntryId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.entryId = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'entryId' field has been set */
    public boolean hasEntryId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'entryId' field */
    public com.pacbio.common.models.pipelines.EntryPoint.Builder clearEntryId() {
      entryId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'fileTypeId' field */
    public java.lang.CharSequence getFileTypeId() {
      return fileTypeId;
    }
    
    /** Sets the value of the 'fileTypeId' field */
    public com.pacbio.common.models.pipelines.EntryPoint.Builder setFileTypeId(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.fileTypeId = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'fileTypeId' field has been set */
    public boolean hasFileTypeId() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'fileTypeId' field */
    public com.pacbio.common.models.pipelines.EntryPoint.Builder clearFileTypeId() {
      fileTypeId = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public com.pacbio.common.models.pipelines.EntryPoint.Builder setName(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.name = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'name' field has been set */
    public boolean hasName() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'name' field */
    public com.pacbio.common.models.pipelines.EntryPoint.Builder clearName() {
      name = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public EntryPoint build() {
      try {
        EntryPoint record = new EntryPoint();
        record.entryId = fieldSetFlags()[0] ? this.entryId : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.fileTypeId = fieldSetFlags()[1] ? this.fileTypeId : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.name = fieldSetFlags()[2] ? this.name : (java.lang.CharSequence) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
