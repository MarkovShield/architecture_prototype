package ch.hsr.markovshield.models;

import ch.hsr.markovshield.models.Session;

import static ch.hsr.markovshield.models.Session.SCHEMA$;

/**
 * RecordBuilder for Session instances.
 */
public class SessionBuilder extends org.apache.avro.specific.SpecificRecordBuilderBase<Session>
        implements org.apache.avro.data.RecordBuilder<Session> {
    private static final org.apache.avro.io.DatumWriter
            WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);

    private static final org.apache.avro.io.DatumReader
            READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);

    private CharSequence session;
    private CharSequence user;

    /** Creates a new SessionBuilder */
    protected SessionBuilder() {
        super(SCHEMA$);
    }

    /**
     * Creates a SessionBuilder by copying an existing SessionBuilder.
     * @param other The existing SessionBuilder to copy.
     */
    protected SessionBuilder(SessionBuilder other) {
        super(other);
        if (isValidValue(fields()[0], other.session)) {
            this.session = data().deepCopy(fields()[0].schema(), other.session);
            fieldSetFlags()[0] = true;
        }
        if (isValidValue(fields()[1], other.user)) {
            this.user = data().deepCopy(fields()[1].schema(), other.user);
            fieldSetFlags()[1] = true;
        }
    }

    /**
     * Creates a SessionBuilder by copying an existing Session instance
     * @param other The existing instance to copy.
     */
    protected SessionBuilder(Session other) {
        super(SCHEMA$);
        if (isValidValue(fields()[0], other.session)) {
            this.session = data().deepCopy(fields()[0].schema(), other.session);
            fieldSetFlags()[0] = true;
        }
        if (isValidValue(fields()[1], other.user)) {
            this.user = data().deepCopy(fields()[1].schema(), other.user);
            fieldSetFlags()[1] = true;
        }
    }

    /**
     * Gets the value of the 'session' field.
     * @return The value.
     */
    public CharSequence getSession() {
        return session;
    }

    /**
     * Sets the value of the 'session' field.
     * @param value The value of 'session'.
     * @return This builder.
     */
    public SessionBuilder setSession(CharSequence value) {
        validate(fields()[0], value);
        this.session = value;
        fieldSetFlags()[0] = true;
        return this;
    }

    /**
     * Checks whether the 'session' field has been set.
     * @return True if the 'session' field has been set, false otherwise.
     */
    public boolean hasSession() {
        return fieldSetFlags()[0];
    }


    /**
     * Clears the value of the 'session' field.
     * @return This builder.
     */
    public SessionBuilder clearSession() {
        session = null;
        fieldSetFlags()[0] = false;
        return this;
    }

    /**
     * Gets the value of the 'user' field.
     * @return The value.
     */
    public CharSequence getUser() {
        return user;
    }

    /**
     * Sets the value of the 'user' field.
     * @param value The value of 'user'.
     * @return This builder.
     */
    public SessionBuilder setUser(CharSequence value) {
        validate(fields()[1], value);
        this.user = value;
        fieldSetFlags()[1] = true;
        return this;
    }

    /**
     * Checks whether the 'user' field has been set.
     * @return True if the 'user' field has been set, false otherwise.
     */
    public boolean hasUser() {
        return fieldSetFlags()[1];
    }


    /**
     * Clears the value of the 'user' field.
     * @return This builder.
     */
    public SessionBuilder clearUser() {
        user = null;
        fieldSetFlags()[1] = false;
        return this;
    }

    @Override
    public Session build() {
        try {
            Session record = new Session();
            record.session = fieldSetFlags()[0] ? this.session : (CharSequence) defaultValue(fields()[0]);
            record.user = fieldSetFlags()[1] ? this.user : (CharSequence) defaultValue(fields()[1]);
            return record;
        } catch (Exception e) {
            throw new org.apache.avro.AvroRuntimeException(e);
        }
    }
}