package com.cinchapi.ccl.v3;

import com.cinchapi.concourse.annotate.PackagePrivate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

public class Convert {

    /**
     * A mapping from strings that can be translated to {@link Operator
     * operators} to the operations to which they can be translated.
     */
    @PackagePrivate
    static Map<String, Operator> OPERATOR_STRINGS;
    static {
        OPERATOR_STRINGS = Maps.newHashMap();
        OPERATOR_STRINGS.put("==", Operator.EQUALS);
        OPERATOR_STRINGS.put("=", Operator.EQUALS);
        OPERATOR_STRINGS.put("eq", Operator.EQUALS);
        OPERATOR_STRINGS.put("!=", Operator.NOT_EQUALS);
        OPERATOR_STRINGS.put("ne", Operator.NOT_EQUALS);
        OPERATOR_STRINGS.put(">", Operator.GREATER_THAN);
        OPERATOR_STRINGS.put("gt", Operator.GREATER_THAN);
        OPERATOR_STRINGS.put(">=", Operator.GREATER_THAN_OR_EQUALS);
        OPERATOR_STRINGS.put("gte", Operator.GREATER_THAN_OR_EQUALS);
        OPERATOR_STRINGS.put("<", Operator.LESS_THAN);
        OPERATOR_STRINGS.put("lt", Operator.LESS_THAN);
        OPERATOR_STRINGS.put("<=", Operator.LESS_THAN_OR_EQUALS);
        OPERATOR_STRINGS.put("lte", Operator.LESS_THAN_OR_EQUALS);
        OPERATOR_STRINGS.put("><", Operator.BETWEEN);
        OPERATOR_STRINGS.put("bw", Operator.BETWEEN);
        OPERATOR_STRINGS.put("->", Operator.LINKS_TO);
        OPERATOR_STRINGS.put("lnks2", Operator.LINKS_TO);
        OPERATOR_STRINGS.put("lnk2", Operator.LINKS_TO);
        OPERATOR_STRINGS.put("regex", Operator.REGEX);
        OPERATOR_STRINGS.put("nregex", Operator.NOT_REGEX);
        OPERATOR_STRINGS.put("like", Operator.LIKE);
        OPERATOR_STRINGS.put("nlike", Operator.NOT_LIKE);
        for (Operator operator : Operator.values()) {
            OPERATOR_STRINGS.put(operator.name(), operator);
            OPERATOR_STRINGS.put(operator.symbol(), operator);
        }
        OPERATOR_STRINGS = ImmutableMap.copyOf(OPERATOR_STRINGS);
    }

    /**
     * Convert the {@code symbol} into the appropriate {@link Operator}.
     *
     * @param symbol - the string form of a symbol (i.e. =, >, >=, etc) or a
     *            CaSH shortcut (i.e. eq, gt, gte, etc)
     * @return the {@link Operator} that is parsed from the string
     *         {@code symbol}
     */
    public static Operator stringToOperator(String symbol) {
        Operator operator = OPERATOR_STRINGS.get(symbol);
        if(operator == null) {
            throw new IllegalStateException(
                    "Cannot parse " + symbol + " into an operator");
        }
        else {
            return operator;
        }
    }

    /**
     * Convert the {@code operator} to a string representation.
     *
     * @param operator
     * @return the operator string
     */
    public static String operatorToString(
            Operator operator) {
        String string = "";
        switch (operator) {
        case EQUALS:
            string = "=";
            break;
        case NOT_EQUALS:
            string = "!=";
            break;
        case GREATER_THAN:
            string = ">";
            break;
        case GREATER_THAN_OR_EQUALS:
            string = ">=";
            break;
        case LESS_THAN:
            string = "<";
            break;
        case LESS_THAN_OR_EQUALS:
            string = "<=";
            break;
        case BETWEEN:
            string = "><";
            break;
        default:
            string = operator.name();
            break;

        }
        return string;
    }
}
