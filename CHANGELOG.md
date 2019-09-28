# Changelog

#### Version 3.0.0 (TBD)
##### Function Statements
In version `3.0.0` we added support for **function statements**.

###### *Background*
###### Functions in Concourse
In Concourse, a function is an operation that is applied to a collection of values; values that can be retrived from a key stored in one or more records. Concourse functions be expressed in the following ways:
* `function(key)` - applied to every value stored for key in every record
* `function(key, records)` - applied to every value stored for key in each of the `records`
* `function(key, ccl/criteria)` - applied to every value stored for key in each of the records that match the `ccl` or `criteria`.
* `function(key, record)` - applied to every value stored for key in the `record`

###### Expressions in CCL
In CCL, the notion of an `expression` is core. A `Condition` is really just one or more expressions that are logically joined together in a manner that expresses clear evaluation precedence.

An expression, generally takes the form:
```
<key> <operator> <values>
```

So, in the context of a database query like `select(<key1>, "<key2> <operator> <value>")`, `key2` and `key1` are both keys, but they have different roles in the operation. In this scenario, `key2` isn't returned to the caller, but is part of the *evaluation* expression. So, we call `key2` an **evaluation key**. On the other hand, `key1` doesn't play a role in *evaluation*, but is an *artifact* of the operation. So, we call this a **operation key**. As you can imahine, in more complex examples, a key can play both roles. 

Similar to an **evaluation key**, a value that is part of an expression plays the role of **evaluation value**.

##### Functions in CCL
The roles **evaluation key** and **evaluation value** are important for understanding how functions work in CCL. Conceptually the value(s) of an expression's `evaluation key` are retrieved and considered in relation to the expression's `operator` and `evaluation values` to determine if the record satisfies the expression. And since functions return a value, you can imagine using a function statement as either am evaluation key or an evaluation value.

In a programming language this would be easy, but in CCL it is possible with caveats due to language ambiguity. To understand these challenges, consider the question: *who's average score is greater than the average of all scores?*. 

This question could be answered by issuing a database query of the form `find("{evaluation key} > {evaluation value}")`. In this case, we know that the evaluation value should be `average(score)` since we want to compare against the average of all scores. Now, our query looks like `find("{evaluation key} > average(score)")`. 

But confusion abounds when we consider how to express selecting the average score of each record that is being evaluated. Concourse functions support providing an explicit record or records, but, in CCL, we don't know which records are being evaluated. To get around this, we created **implicit function** syntax that uses the pipe character (e.g. `|`) to indicate that an operation should only be applied to the key in the record that is currently being evaluated. So, our complete query would look like `find("score | average > average(score)")`.

In an effort to avoid any ambiguity, we've adopted the following conventions:
* An **implicit function** statement can only be used as an evaluation key and never an evaluation value
* All other function statements can be used as an evaluation value but never as an evlauation key.

|                        | Operation Key | Evaluation Key | Evaluation Value |
|------------------------|---------------|----------------|------------------|
| `function(key)`          | NO            | NO             | YES              |
| `function(key, records)` | NO            | NO             | YES              |
| `function(key, record)`  | NO            | NO             | YES              |
| `function(key, ccl)`     | NO            | NO             | YES              |
| `key \| function`             | YES           | YES            | NO               |

##### API Breaks
* The `Expression` symbol has been deprecated and renamed `ExpressionSymbol` for clarity.
* The deprecated `ConcourseParser` has been removed. We will no longer distinguish the current parser as `v2` since it is the only one.
* Renamed the `com.cinchapi.ccl.v2.generated` package to `com.cinchapi.ccl.generated`.

#### Version 2.6.3 (TBD)
* Fixed a bug that caused non-numeric Tags to be erroneously parsed and transformed into symbols containing String values instead of Tag values

#### Version 2.6.2 (August 20, 2019)
* Fixed a bug that caused the `v2` parser to fail when trying to parse a CCL statement that contained unquoted string values with periods.

#### Version 2.6.1 (July 20, 2019)
* Fixed a bug that caused a `ValueSymbol` containing a `Timestamp` value to be written in a CCL format that could not be re-parsed by a CCL parser. This bug cased a `SyntaxException` to thrown when attempting to tokenize a CCL statement generated by a Criteria that contained any `Timestamp` values.

#### Version 2.6.0 (July 16, 2019)
* Added support for **navigation keys**. A **navigation key** is used to traverse the document graph in Concourse. It is made up of multiple keys that are joined by the `.` character (i.e. `friends.friends.name`).

#### Version 2.5.2 (February 16, 2019)
* Fixed a bug that caused the CCL parser to fail on certain Unicode quote characters.

#### Version 2.5.1 (October 31, 2018)
* Added context about the CCL statement being processed to the exceptions thrown from operations in the `v2` parser.

#### Version 2.5.0 (October 14, 2018)
* Added the `Parser#evaluate` method that performs local evaluation of the parsed query on an input dataset.

#### Version 2.4.1 (August 14, 2018)
* Fixed a bug that caused both the `v1` and `v2` parsers to mishandle numeric `String` and `Tag` values. These values were treated as numbers instead of their actual type. This made it possible for queries containing those values to return inaccurate results.

#### Version 2.4.0 (May 21, 2018)
* Added support for escaping special characters in value tokens.
* Added enforcement within the `v2` parser that ensures that the `LINKS_TO` operator is followed by a numeric token.

#### Version 2.3.3 (March 26, 2018)
* Fixed a bug that caused the `v2` parser to fail to parse CCL statements containing operator tokens named after their respective enums (e.g. `LINKS_TO` instead of `lnks2`).

#### Version 2.3.2 (February 1, 2018)
* Fixed a bug that caused the `v2` parser to fail to parse CCL statements containing the `LIKE` and `NOT_LIKE` operators.

#### Version 2.3.1 (January 10, 2018)
* Fixed a bug that caused the `v2` parser to incorrectly tokenize CCL statements that contained unquoted string values containing spaces.

#### Version 2.3.0 (January 7, 2018)
* Deprecated `Parser#newParser` static factory methods in favor of the `Parser#create` static factory methods.
* Deprecated the internal `ConcourseParser` in favor of an implementation generated by the [`javacc`](https://javacc.org/) framework. This makes the parser more performant, stable and amenable to future enhancements.

#### Version 2.2.1
* Added additional date time formatters that may be used to transform a string into a number of microseconds from the unix epoch in the `NaturalLanguage#parseMicros` method.

#### Version 2.2.0
* Added support for traversing an `AbstractSyntaxTree` using the visitor pattern.
* Fixed bugs in the `AbstractSyntaxTree` generation for the `ConcourseParser`.

#### Version 2.1.0
* Added a `Parsing#toPostfixNotation` utility method that transforms a list of `Symbol`s into a `Queue` of `PostfixNotationSymbol`s.

#### Version 2.0.0
* Changed the `Parser` interface to maintain parsing state and expose instance methods instead of static functions.
* Fixed a bug that caused the Parser's analyzer to not correctly detect the keys being evaluated by a CCL statement.
