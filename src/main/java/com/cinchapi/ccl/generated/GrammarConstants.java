/* Generated By:JJTree&JavaCC: Do not edit this line. GrammarConstants.java */
package com.cinchapi.ccl.generated;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface GrammarConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int OPEN_PARENTHESES = 3;
  /** RegularExpression Id. */
  int CLOSE_PARENTHESES = 4;
  /** RegularExpression Id. */
  int TIMESTAMP = 5;
  /** RegularExpression Id. */
  int WHERE = 6;
  /** RegularExpression Id. */
  int RESERVED_IDENTIFIER = 7;
  /** RegularExpression Id. */
  int CONJUNCTION = 8;
  /** RegularExpression Id. */
  int DISJUNCTION = 9;
  /** RegularExpression Id. */
  int UNARY_OPERATOR = 10;
  /** RegularExpression Id. */
  int BINARY_OPERATOR = 11;
  /** RegularExpression Id. */
  int OPEN_ANGLE_BRACKET = 12;
  /** RegularExpression Id. */
  int CLOSE_ANGLE_BRACKET = 13;
  /** RegularExpression Id. */
  int EQUALS = 14;
  /** RegularExpression Id. */
  int NOT_EQUALS = 15;
  /** RegularExpression Id. */
  int GREATER_THAN = 16;
  /** RegularExpression Id. */
  int GREATER_THAN_OR_EQUALS = 17;
  /** RegularExpression Id. */
  int LESS_THAN = 18;
  /** RegularExpression Id. */
  int LESS_THAN_OR_EQUALS = 19;
  /** RegularExpression Id. */
  int LINKS_TO = 20;
  /** RegularExpression Id. */
  int REGEX = 21;
  /** RegularExpression Id. */
  int NOT_REGEX = 22;
  /** RegularExpression Id. */
  int LIKE = 23;
  /** RegularExpression Id. */
  int NOT_LIKE = 24;
  /** RegularExpression Id. */
  int BETWEEN = 25;
  /** RegularExpression Id. */
  int ORDER = 26;
  /** RegularExpression Id. */
  int PIPE = 27;
  /** RegularExpression Id. */
  int AMPERSAND = 28;
  /** RegularExpression Id. */
  int ASC = 29;
  /** RegularExpression Id. */
  int DESC = 30;
  /** RegularExpression Id. */
  int QUOTED_STRING = 31;
  /** RegularExpression Id. */
  int DOUBLE_QUOTED_STRING = 32;
  /** RegularExpression Id. */
  int SINGLE_QUOTED_STRING = 33;
  /** RegularExpression Id. */
  int COMMA_SEPARATED_SIGNED_INTEGER = 34;
  /** RegularExpression Id. */
  int COMMA_SEPARATED_SIGNED_DECIMAL = 35;
  /** RegularExpression Id. */
  int COMMA_SEPARATED_ALPHANUMERIC = 36;
  /** RegularExpression Id. */
  int COMMA_SEPARATED_PERIOD_SEPARATED_STRING = 37;
  /** RegularExpression Id. */
  int NUMERIC = 38;
  /** RegularExpression Id. */
  int SIGNED_INTEGER = 39;
  /** RegularExpression Id. */
  int SIGNED_DECIMAL = 40;
  /** RegularExpression Id. */
  int ALPHANUMERIC = 41;
  /** RegularExpression Id. */
  int PERIOD_SEPARATED_STRING = 42;
  /** RegularExpression Id. */
  int NON_ALPHANUMERIC_AND_ALPHANUMERIC = 43;
  /** RegularExpression Id. */
  int NON_ALPHANUMERIC = 44;
  /** RegularExpression Id. */
  int LETTER = 45;
  /** RegularExpression Id. */
  int DIGIT = 46;
  /** RegularExpression Id. */
  int PERIOD = 47;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"(\"",
    "\")\"",
    "<TIMESTAMP>",
    "\"where\"",
    "\"$id$\"",
    "<CONJUNCTION>",
    "<DISJUNCTION>",
    "<UNARY_OPERATOR>",
    "<BINARY_OPERATOR>",
    "\"<\"",
    "\">\"",
    "<EQUALS>",
    "<NOT_EQUALS>",
    "<GREATER_THAN>",
    "<GREATER_THAN_OR_EQUALS>",
    "<LESS_THAN>",
    "<LESS_THAN_OR_EQUALS>",
    "<LINKS_TO>",
    "\"regex\"",
    "<NOT_REGEX>",
    "\"like\"",
    "<NOT_LIKE>",
    "<BETWEEN>",
    "\"order\"",
    "\"|\"",
    "\"@\"",
    "\"ASC\"",
    "\"DESC\"",
    "<QUOTED_STRING>",
    "<DOUBLE_QUOTED_STRING>",
    "<SINGLE_QUOTED_STRING>",
    "<COMMA_SEPARATED_SIGNED_INTEGER>",
    "<COMMA_SEPARATED_SIGNED_DECIMAL>",
    "<COMMA_SEPARATED_ALPHANUMERIC>",
    "<COMMA_SEPARATED_PERIOD_SEPARATED_STRING>",
    "<NUMERIC>",
    "<SIGNED_INTEGER>",
    "<SIGNED_DECIMAL>",
    "<ALPHANUMERIC>",
    "<PERIOD_SEPARATED_STRING>",
    "<NON_ALPHANUMERIC_AND_ALPHANUMERIC>",
    "<NON_ALPHANUMERIC>",
    "<LETTER>",
    "<DIGIT>",
    "\".\"",
    "\"\\n\"",
    "\"_\"",
    "\"=\"",
  };

}