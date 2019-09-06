package com.cinchapi.ccl.order;

/**
 * Sort directions.
 *
 * @author Jeff Nelson
 */
public enum Direction {

    ASCENDING(1), DESCENDING(-1);

    /**
     * Return the default {@link Direction}.
     *
     * @return the default
     */
    static Direction $default() {
        return ASCENDING;
    }

    /**
     * The coefficient is multiplied by the result of a {@link Comparator} to
     * sort elements in forward or reverse order.
     */
    private final int coefficient;

    /**
     * Construct a new instance.
     *
     * @param coefficient
     */
    Direction(int coefficient) {
        this.coefficient = coefficient;
    }

    /**
     * Return the coefficient associated with this {@link Direction}.
     *
     * @return the coefficient
     */
    public int coefficient() {
        return coefficient;
    }

}

