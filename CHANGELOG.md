# Changelog

#### Version 2.2.0
* Added support for traversing an `AbstractSyntaxTree` using the visitor pattern.
* Fixed bugs in the `AbstractSyntaxTree` generation for the `ConcourseParser`.

#### Version 2.1.0
* Added a `Parsing#toPostfixNotation` utility method that transforms a list of `Symbol`s into a `Queue` of `PostfixNotationSymbol`s.

#### Version 2.0.0
* Changed the `Parser` interface to maintain parsing state and expose instance methods instead of static functions.
* Fixed a bug that caused the Parser's analyzer to not correctly detect the keys being evaluated by a CCL statement.
