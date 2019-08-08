package com.cinchapi.ccl.grammar;

/**
 * An {@link KeyFunction} describes a function that can only be used as an
 * evaluation or selection key that operates on a key
 */
public abstract class KeyFunction {
    /**
     * The function
     */
    protected final String function;

    /**
     * The key
     */
    protected final String key;

    /**
     * Creates a new instances
     *
     * @param function the function
     * @param key the key
     */
    protected KeyFunction(String function, String key) {
        this.function = function;
        this.key = key;
    }

    /**
     * Retrieves the function.
     *
     * @return the function
     */
    public String function() {
        return function;
    }

    /**
     * Retrieves the key
     *
     * @return the keu
     */
    public String key() {
        return key;
    }

    /**
     * Return a string representation
     *
     * @return the string representation
     */
    public abstract String toString();
}

