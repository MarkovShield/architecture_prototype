/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package ch.hsr.markovshield.models;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Session extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 7412211821263870979L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Session\",\"namespace\":\"ch.hsr.markovshield.models\",\"fields\":[{\"name\":\"session\",\"type\":\"string\"},{\"name\":\"user\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public CharSequence session;
  @Deprecated public CharSequence user;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public Session() {}

  /**
   * All-args constructor.
   * @param session The new value for session
   * @param user The new value for user
   */
  public Session(CharSequence session, CharSequence user) {
    this.session = session;
    this.user = user;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public Object get(int field$) {
    switch (field$) {
    case 0: return session;
    case 1: return user;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, Object value$) {
    switch (field$) {
    case 0: session = (CharSequence)value$; break;
    case 1: user = (CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'session' field.
   * @return The value of the 'session' field.
   */
  public CharSequence getSession() {
    return session;
  }

  /**
   * Sets the value of the 'session' field.
   * @param value the value to set.
   */
  public void setSession(CharSequence value) {
    this.session = value;
  }

  /**
   * Gets the value of the 'user' field.
   * @return The value of the 'user' field.
   */
  public CharSequence getUser() {
    return user;
  }

  /**
   * Sets the value of the 'user' field.
   * @param value the value to set.
   */
  public void setUser(CharSequence value) {
    this.user = value;
  }

  /**
   * Creates a new Session RecordBuilder.
   * @return A new Session RecordBuilder
   */
  public static SessionBuilder newBuilder() {
    return new SessionBuilder();
  }

  /**
   * Creates a new Session RecordBuilder by copying an existing SessionBuilder.
   * @param other The existing builder to copy.
   * @return A new Session RecordBuilder
   */
  public static SessionBuilder newBuilder(SessionBuilder other) {
    return new SessionBuilder(other);
  }

  /**
   * Creates a new Session RecordBuilder by copying an existing Session instance.
   * @param other The existing instance to copy.
   * @return A new Session RecordBuilder
   */
  public static SessionBuilder newBuilder(Session other) {
    return new SessionBuilder(other);
  }





}