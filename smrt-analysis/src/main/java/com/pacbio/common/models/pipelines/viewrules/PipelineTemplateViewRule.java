/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.pacbio.common.models.pipelines.viewrules;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class PipelineTemplateViewRule extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"PipelineTemplateViewRule\",\"namespace\":\"com.pacbio.common.models.pipelines.viewrules\",\"fields\":[{\"name\":\"id\",\"type\":\"string\",\"doc\":\"Pipeline Template Id to apply rules to.\"},{\"name\":\"description\",\"type\":\"string\",\"doc\":\"Override pipeline template description\"},{\"name\":\"name\",\"type\":\"string\",\"doc\":\"Override pipeline template name\"},{\"name\":\"taskOptions\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"PipelineOptionView\",\"fields\":[{\"name\":\"id\",\"type\":\"string\",\"doc\":\"Tool Contract option id (e.g., pbcommand.task_options.my_option)\"},{\"name\":\"hidden\",\"type\":\"boolean\",\"doc\":\"Should option be hidden\"}]}}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  /** Pipeline Template Id to apply rules to. */
  @Deprecated public java.lang.CharSequence id;
  /** Override pipeline template description */
  @Deprecated public java.lang.CharSequence description;
  /** Override pipeline template name */
  @Deprecated public java.lang.CharSequence name;
  @Deprecated public java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView> taskOptions;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public PipelineTemplateViewRule() {}

  /**
   * All-args constructor.
   */
  public PipelineTemplateViewRule(java.lang.CharSequence id, java.lang.CharSequence description, java.lang.CharSequence name, java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView> taskOptions) {
    this.id = id;
    this.description = description;
    this.name = name;
    this.taskOptions = taskOptions;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return description;
    case 2: return name;
    case 3: return taskOptions;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.CharSequence)value$; break;
    case 1: description = (java.lang.CharSequence)value$; break;
    case 2: name = (java.lang.CharSequence)value$; break;
    case 3: taskOptions = (java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   * Pipeline Template Id to apply rules to.   */
  public java.lang.CharSequence getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * Pipeline Template Id to apply rules to.   * @param value the value to set.
   */
  public void setId(java.lang.CharSequence value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'description' field.
   * Override pipeline template description   */
  public java.lang.CharSequence getDescription() {
    return description;
  }

  /**
   * Sets the value of the 'description' field.
   * Override pipeline template description   * @param value the value to set.
   */
  public void setDescription(java.lang.CharSequence value) {
    this.description = value;
  }

  /**
   * Gets the value of the 'name' field.
   * Override pipeline template name   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * Override pipeline template name   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'taskOptions' field.
   */
  public java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView> getTaskOptions() {
    return taskOptions;
  }

  /**
   * Sets the value of the 'taskOptions' field.
   * @param value the value to set.
   */
  public void setTaskOptions(java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView> value) {
    this.taskOptions = value;
  }

  /** Creates a new PipelineTemplateViewRule RecordBuilder */
  public static com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder newBuilder() {
    return new com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder();
  }
  
  /** Creates a new PipelineTemplateViewRule RecordBuilder by copying an existing Builder */
  public static com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder newBuilder(com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder other) {
    return new com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder(other);
  }
  
  /** Creates a new PipelineTemplateViewRule RecordBuilder by copying an existing PipelineTemplateViewRule instance */
  public static com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder newBuilder(com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule other) {
    return new com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder(other);
  }
  
  /**
   * RecordBuilder for PipelineTemplateViewRule instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<PipelineTemplateViewRule>
    implements org.apache.avro.data.RecordBuilder<PipelineTemplateViewRule> {

    private java.lang.CharSequence id;
    private java.lang.CharSequence description;
    private java.lang.CharSequence name;
    private java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView> taskOptions;

    /** Creates a new Builder */
    private Builder() {
      super(com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.description)) {
        this.description = data().deepCopy(fields()[1].schema(), other.description);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.name)) {
        this.name = data().deepCopy(fields()[2].schema(), other.name);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.taskOptions)) {
        this.taskOptions = data().deepCopy(fields()[3].schema(), other.taskOptions);
        fieldSetFlags()[3] = true;
      }
    }
    
    /** Creates a Builder by copying an existing PipelineTemplateViewRule instance */
    private Builder(com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule other) {
            super(com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.description)) {
        this.description = data().deepCopy(fields()[1].schema(), other.description);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.name)) {
        this.name = data().deepCopy(fields()[2].schema(), other.name);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.taskOptions)) {
        this.taskOptions = data().deepCopy(fields()[3].schema(), other.taskOptions);
        fieldSetFlags()[3] = true;
      }
    }

    /** Gets the value of the 'id' field */
    public java.lang.CharSequence getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder setId(java.lang.CharSequence value) {
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
    public com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'description' field */
    public java.lang.CharSequence getDescription() {
      return description;
    }
    
    /** Sets the value of the 'description' field */
    public com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder setDescription(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.description = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'description' field has been set */
    public boolean hasDescription() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'description' field */
    public com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder clearDescription() {
      description = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder setName(java.lang.CharSequence value) {
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
    public com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder clearName() {
      name = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'taskOptions' field */
    public java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView> getTaskOptions() {
      return taskOptions;
    }
    
    /** Sets the value of the 'taskOptions' field */
    public com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder setTaskOptions(java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView> value) {
      validate(fields()[3], value);
      this.taskOptions = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'taskOptions' field has been set */
    public boolean hasTaskOptions() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'taskOptions' field */
    public com.pacbio.common.models.pipelines.viewrules.PipelineTemplateViewRule.Builder clearTaskOptions() {
      taskOptions = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public PipelineTemplateViewRule build() {
      try {
        PipelineTemplateViewRule record = new PipelineTemplateViewRule();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.description = fieldSetFlags()[1] ? this.description : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.name = fieldSetFlags()[2] ? this.name : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.taskOptions = fieldSetFlags()[3] ? this.taskOptions : (java.util.List<com.pacbio.common.models.pipelines.viewrules.PipelineOptionView>) defaultValue(fields()[3]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}