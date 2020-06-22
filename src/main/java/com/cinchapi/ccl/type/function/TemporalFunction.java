package com.cinchapi.ccl.type.function;

import com.cinchapi.ccl.type.Function;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A function that contains a temporal aspect
 */
public abstract class TemporalFunction extends Function {
    /**
     * Indicates that this function has no timestamp.
     */
    protected static long NO_TIMESTAMP = Long.MAX_VALUE; // matches
                                                       // com.cinchapi.concourse.time.Time#NONE

    /**
     * The selection timestamp associated with this function.
     */
    protected final long timestamp;

    /**
     * The degree of precision to use for this {@link Function} when
     * determining {@link #equals(Object) equality} and the {@link #hashCode()}.
     */
    private TimeUnit timestampPrecision;

    /**
     * Construct a new instance.
     *
     * @param operation
     * @param key
     * @param args
     */
    protected TemporalFunction(String operation, String key, Object... args) {
        this(NO_TIMESTAMP, operation, key, args);
    }

    /**
     * Construct a new instance.
     *
     * @param timestamp
     * @param operation
     * @param key
     * @param args
     */
    protected TemporalFunction(long timestamp, String operation, String key, Object... args) {
        super(operation, key, args);
        this.timestamp = timestamp;
        this.timestampPrecision = TimeUnit.MICROSECONDS;
    }

    /**
     * Return the timestamp describing when the function is applied.
     *
     * @return the timestamp
     */
    public long timestamp() {
        return timestamp;
    }


    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if(timestamp != NO_TIMESTAMP && equals) {
            equals = timestampPrecision.convert(timestamp,
                    TimeUnit.MICROSECONDS) == timestampPrecision.convert(
                    ((TemporalFunction) obj).timestamp,
                    TimeUnit.MICROSECONDS);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        if(timestamp != NO_TIMESTAMP) {
            return Objects.hash(hashCode, TimeUnit.MICROSECONDS
                    .convert(timestamp, timestampPrecision));
        }
        return hashCode;
    }

    /**
     * Set the {@link #timestampPrecision} for this {@link Function} and return
     * it for chaining.
     *
     * @param timestampPrecision
     * @return this
     */
    public TemporalFunction setTimestampPrecision(
            TimeUnit timestampPrecision) {
        this.timestampPrecision = timestampPrecision;
        return this;
    }
}
