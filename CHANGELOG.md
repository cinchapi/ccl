# Changelog

#### Version 4.0.0 (TBD)
##### Command Support
The CCL grammar has been expanded to support parsing **Concourse commands** in addition to conditions, orders, and pages. A command string (e.g., `select name from 1 where age > 30`) is parsed into a `CommandTree` containing a `CommandSymbol` that represents the operation, along with optional `ConditionTree`, `OrderTree`, and `PageTree` children.

###### Supported Commands
The following Concourse operations can now be expressed and parsed in CCL:

**Data Modification**
* `add` - Add a value to a key in a record (e.g., `add name as jeff in 1`)
* `set` - Set a value for a key in a record or records (e.g., `set name as jeff in [1, 2, 3]`)
* `remove` - Remove a value from a key in a record (e.g., `remove name as jeff from 1`)
* `clear` - Clear all values for a key in a record (e.g., `clear name from 1`)
* `insert` - Insert a JSON document (e.g., `insert {"name": "jeff"}`)
* `reconcile` - Reconcile values for a key in a record (e.g., `reconcile name in 1 with [jeff, bob]`)
* `verify_and_swap` - Atomically verify and swap a value (e.g., `verify_and_swap name as jeff to bob in 1`)
* `verify_or_set` - Verify or set a value (e.g., `verify_or_set name as jeff in 1`)

**Data Reading**
* `select` - Select keys from records with optional conditions, ordering, and pagination
* `get` - Get key values from records at an optional timestamp
* `find` - Find records matching a condition with optional ordering and pagination
* `find_or_add` / `find_or_insert` - Find records matching a condition or add/insert if none found
* `browse` - Browse values for keys (e.g., `browse name` or `browse [name, age]`)
* `navigate` - Navigate linked data (e.g., `navigate friends.name from 1 where age > 21`)
* `describe` - Describe a record's keys at an optional timestamp (e.g., `describe 1 at "yesterday"`)
* `search` - Full-text search (e.g., `search name for "jeff"`)
* `verify` - Verify a value exists (e.g., `verify name as jeff in 1`)
* `jsonify` - Export records as JSON with optional identifier inclusion

**History and Auditing**
* `audit` - View the audit log for a record or key (e.g., `audit name in 1 from "2024-01-01" to "2024-02-01"`)
* `chronicle` - View the change history for a key in a record (e.g., `chronicle name in 1`)
* `diff` - Compare data between timestamps (e.g., `diff 1 from "yesterday" to "today"`)
* `revert` - Revert keys in records to a prior state (e.g., `revert name in 1 at "last week"`)
* `trace` - Trace incoming references to a record (e.g., `trace 1`)

**Calculations**
* `calculate` - Perform aggregate calculations (`sum`, `average`, `count`, `min`, `max`) on key values with optional record filtering and conditions (e.g., `calculate sum age where department = engineering`)

**Links**
* `link` / `unlink` - Create or remove links between records (e.g., `link friends from 1 to 2`)

**Transactions**
* `stage` / `commit` / `abort` - Transaction control

**Utility**
* `ping` - Server health check
* `holds` - Check if records contain data (e.g., `holds 1` or `holds [1, 2, 3]`)
* `consolidate` - Merge records (e.g., `consolidate 1 2 3`)
* `inventory` - List all records

###### New Symbol Classes
Each command is represented by a dedicated `CommandSymbol` implementation: `AddSymbol`, `SetSymbol`, `RemoveSymbol`, `ClearSymbol`, `InsertSymbol`, `ReconcileSymbol`, `VerifyAndSwapSymbol`, `VerifyOrSetSymbol`, `SelectSymbol`, `GetSymbol`, `FindSymbol`, `FindOrAddSymbol`, `FindOrInsertSymbol`, `BrowseSymbol`, `NavigateSymbol`, `DescribeSymbol`, `SearchSymbol`, `VerifySymbol`, `JsonifySymbol`, `AuditSymbol`, `ChronicleSymbol`, `DiffSymbol`, `RevertSymbol`, `TraceSymbol`, `CalculateSymbol`, `LinkSymbol`, `UnlinkSymbol`, `StageSymbol`, `CommitSymbol`, `AbortSymbol`, `PingSymbol`, `HoldsSymbol`, `ConsolidateSymbol`, `InventorySymbol`.

##### Multi-Statement Parsing
Added support for parsing multiple semicolon-delimited CCL statements in a single call, similar to SQL. The new `Compiler#compile(String)` and `Compiler#compile(String, Multimap)` methods accept a CCL string containing one or more statements separated by `;` and return a `List<AbstractSyntaxTree>` with one tree per statement.
* Semicolons (`;`) are now a reserved token in the grammar. Unquoted semicolons in values are no longer permitted (use quoted strings instead).

##### Mixed-time reads via bracket-timestamp syntax
A single CCL operation can now read each of its keys at a different point in time. Every key accepts an optional bracket annotation `[<timestamp>]` that pins the read it represents — the brackets are the timestamp pivot, so no leading `at`/`on`/`during` keyword is required inside (the keyword form is accepted for backward compatibility, but the canonical serialization is keyword-less). This is qualitatively new: previous CCL versions could only express one timestamp per operation.

```
select [name[t1], age[t2], score[t3]] from 1
find name[t1] = "Jeff" AND age[t2] >= 30
calculate sum revenue[t1] from 1
A[t1].(foo[t2] = "X" AND bar[t3] = "Y")
children[t]*.name = "Jeff"
```

Each bracket binds exactly the read it is adjacent to:
* Bracket on a leaf key pins the leaf's evaluation timestamp.
* Bracket on a navigation stop pins that stop's traversal timestamp.
* Bracket on a scope prefix pins the scope's traversal timestamp.

When a stop also carries the transitive marker `*`, the canonical order is `key[t]*` — the bracket binds to the key, and the asterisk terminates the stop (e.g., `children[t]*.name`). The reverse order `key*[t]` is rejected at parse time. A key carries at most one bracket-timestamp annotation; double annotations such as `a.b[t1][t2]` or `children[t1]*[t2]` are rejected at parse time.

Without an annotation a key reads at the present moment. A new `TemporalKeySymbol` AST type wraps any `KeyTokenSymbol` with the bracket-derived `TimestampSymbol`. `NavigationKeySymbol` carries per-stop timestamps via the `stops()` accessor. Existing CCL strings without brackets parse identically to before.

###### Read vs. write commands
Brackets are accepted on every command whose semantics map to a single point-in-time read — `select`, `get`, `find`, `browse`, `navigate`, `calculate`, `search`, `verify`, and the `order by` clause. They are **rejected at parse time** for writes (`add`, `set`, `remove`, `clear`, `link`, `unlink`, `reconcile`, `verify_and_swap`, `verify_or_set`, `find_or_add`, `revert`) and for range-history reads (`audit`, `chronicle`, `diff`) whose timestamp parameter is a window rather than a point. Rejection produces a `SyntaxException` that names the offending command and key.

###### Precedence when bracket and trailing-`at` coexist
When a per-key bracket and a trailing-`at` (or `as of`) appear on the same operation, the more-specific form wins: the bracket pins the keys it covers, and the trailing-`at` provides a default for keys that have no bracket. This lets callers mix per-key control with a sensible operation-wide default (`select [name[t1], age, score] from 1 at t2` reads `name` at `t1` and the rest at `t2`) and keeps gradual migration off the deprecated trailing-`at` form friction-free. The AST preserves both pieces of information so downstream engines and drivers can apply the precedence rule at evaluation time; consult `KeyTokenSymbol.isTemporal()` / `TemporalKeySymbol.timestamp()` for the per-key timestamp and the command/expression symbol's own `timestamp()` accessor for the operation-level fallback.

##### Statement and Command Analysis API
[GH-67](https://github.com/cinchapi/ccl/issues/67): Expanded `Compiler#analyze(ConditionTree)` and added `Compiler#analyze(CommandTree)` so consumers can introspect the bracket-timestamp-aware shape of a parsed CCL statement without walking the AST by hand.

`StatementAnalysis` gains six bracket-aware accessors — each with an `(Operator)` overload that scopes the result to condition leaves evaluated against that operator:
* `storageKeys()` — every distinct storage-level key the statement touches; navigation paths are exploded into stops, and bracket annotations / transitive markers are stripped.
* `temporalKeys()` — `Map<String, Set<Long>>` from storage key to the distinct microsecond timestamps it is bracket-pinned at. Reports bracket annotations only; the legacy trailing-`at` is reachable via `ExpressionSymbol#timestamp()`.
* `transitiveNavigationKeys()` — storage keys that appear as a transitive (`key*`) navigation stop.
* `navigationKeys()` — distinct navigation paths in canonical storage form (no brackets, no transitive markers).
* `navigationKeyStops()` — storage keys that participate in any navigation path.
* `scopedKeys()` — `Map<String, List<String>>` from each scope-pivot key to the direct child keys evaluated within that scope.

The existing `keys()` accessor now returns storage-form keys (bracket annotations stripped) so per-key timestamps don't leak into general key listings; use `temporalKeys()` to recover them. `keys(Operator)` and `operators()` are otherwise unchanged.

A new `Compiler#analyze(CommandTree)` returns `CommandAnalysis extends StatementAnalysis`, aggregating selection-side and condition-side keys under the inherited accessors and adding:
* `commandType()` — the parsed command name (`"SELECT"`, `"FIND"`, `"ADD"`, ...).
* `commandTimestamp()` — command-level `at` / `as of` as `Long` micros, or `null`.
* `rangeStart()` / `rangeEnd()` — for `audit`, `chronicle`, `diff`.
* `referencedRecords()` — `Set<Long>` of record ids the command directly touches.

##### Other Grammar Updates
* [GH-51](https://github.com/cinchapi/ccl/issues/51): Allow an optional `*` suffix on navigation key segments to mark them as transitive (e.g., `children*.name`, `a.b*.c.d*.e`, `children*`). Transitive segments instruct Concourse to follow links recursively until exhaustion, supporting the Transitive Navigation feature in [cinchapi/concourse#632](https://github.com/cinchapi/concourse/issues/632). A standalone transitive stop (e.g., `children*`) is also accepted as a navigation key and is equivalent to a single-stop path whose terminal stop is transitive. Added `NavigationKeyStop` to model each segment as a structured `(name, transitive)` pair, and `NavigationKeySymbol#stops()` to expose them; the existing `components()` method is unchanged.
* [GH-52](https://github.com/cinchapi/ccl/issues/52): Added **scoped condition groups** via the `prefix.(...)` syntax. Tells the engine that all conditions inside the group must be satisfied by the **same** destination record reachable through the explicit navigation `prefix`, rather than being evaluated independently (which can produce false positives on multi-valued links). Supports [cinchapi/concourse#533](https://github.com/cinchapi/concourse/issues/533).
  * **In CCL**: write the navigation pivot as an explicit prefix ending in `.(`, with inner conditions relative to the pivot. Example: `find where friend.(name = "Jeff" AND age > 30)` — matches records whose `friend` links to a single record satisfying both. Multi-segment (`a.b.(...)`), transitive (`children*.(...)`), and nested (`a.(b.(...))`) pivots compose naturally.
  * **In parsing code**: the parser produces a `ScopedConditionTree` carrying an explicit `prefix()` (a `KeyTokenSymbol`) and an inner `ConditionTree`. `Visitor` implementations must explicitly handle `ScopedConditionTree` — the default `visit(ScopedConditionTree)` throws.
  * **In postfix**: `Compiler#arrange` and `Compiler#tokenize` emit a `ScopeSymbol` (carrying the prefix) as the opening bracket and `ScopeEndSymbol.INSTANCE` as the closing bracket. `Parsing#toPostfixNotation` treats `ScopeSymbol` as a precedence boundary analogous to `(`.

##### API Breaks and Deprecations
###### Pagination
* Changed pagination to use canonical offset-based forms only: `limit n`, `skip n`, `offset n`, `skip n limit m`, `offset n limit m`, `limit m skip n`, and `limit m offset n`.
* Updated `PageSymbol` to model pagination as a required skip and an optional limit, with factories for `fromSkip(int)`, `fromLimit(int)`, and `fromSkipLimit(int, Integer)`.
* Removed page-number pagination syntax such as `page n`, `size n`, and `page n size m`.

###### Trailing `at <timestamp>` on Relational Expressions
* The trailing `at <timestamp>` form on a relational expression (`name = "X" at t`) is now **deprecated** in favor of the per-key bracket annotation (`name[t] = "X"`). The legacy form continues to parse indefinitely, but new code should prefer brackets.
* For a navigation key the trailing form pins every stop in the chain to the same time, with no way to express per-stop differences — the bracket syntax is the only way to do so.
* The compiler preserves whichever form a CCL string uses — trailing-`at` strings round-trip unchanged. Within a bracket the optional keyword is canonicalized away, so `name[at 123]` and `name[123]` parse to identical ASTs and both serialize as `name[123]`.

#### Version 3.2.2 (March 31, 2026)
* Fixed a bug that caused the `Compiler`'s local evaluation to fail when the condition contained navigation keys (e.g., `friend.name = jeff`). The `evaluate` method now accepts an `Association` whose `fetch` method natively resolves dot-separated key paths by traversing nested data structures. The existing `Multimap`-based `evaluate` method is still supported and automatically converts to an `Association` for interoperability, but callers are encouraged to use the `Association` overload directly for better performance.

#### Version 3.2.1 (March 10, 2026)
* Fixed a bug that caused the `CONTAINS` and `NOT_CONTAINS` search operators to fail when used with navigation keys (e.g., `mother.children contains 'foo'`).

#### Version 3.2.0 (February 22, 2025)
* Added support for the new `CONTAINS` and `NOT_CONTAINS` search operators introduced in Concourse version 0.12.0. These operators can be used directly in CCL statements with the following keywords:
  * **CONTAINS**: `contains`, `search`, `search_match`, `~`
  * **NOT_CONTAINS**: `not_contains`, `search_exclude`, `ncontains`, `~!`
* [GH-47](https://github.com/cinchapi/ccl/issues/47): Fixed a bug that caused navigation or function keys to not be included in a Compiler's analysis of a ConditionTree.

#### Version 3.1.2 (March 9, 2022)
* Fixed a regression that caused parenthetical expressions within a `Condition` containing `LIKE` `REGEX`, `NOT_LIKE` and `NOT_REGEX` operators (e.g., `a = b and (email regex email.com)`) to mistakenly throw a `SyntaxException` when being parsed by a `Compiler`.

#### Version 3.1.1 (March 4, 2022)
* Fixed a regression that caused Conditions with `LIKE` `REGEX`, `NOT_LIKE` and `NOT_REGEX` operators followed by a whitespace containing value to be incorrectly parsed.

#### Version 3.1.0 (June 22, 2020)
* Added support for parsing standalone `function statements` in a compiler. Now, the following forms will parse into a `FunctionTree` that contains a symbolic representation of the function expressed in the CCL statement:
* `function(key)` produces a FunctionTree whose root node contains an `IndexFunction`
* `function(key, record)` produces a FunctionTree whose root node contains a `KeyRecordsFunction`
* `function(key, record1,record2,...,recordN)` produces a FunctionTree whose root node contains a `KeyRecordsFunction`
* `function(key, condition)` produces a FunctionTree whose root node contains a `KeyConditionFunction`
* `key \| function`  produces a FunctionTree whose root node contains an `ImplicitKeyRecordFunction`
* Fixed a bug where the paramaterized type of `KeyRecordsFunction` was a `List<String>` instead of a `List<Long>`.
* Added support for including an optional timestamp within a function value statement.

#### Version 3.0.0 (June 15, 2020)
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

So, in the context of a database query like `select(<key1>, "<key2> <operator> <value>")`, `key2` and `key1` are both keys, but they have different roles in the operation. In this scenario, `key2` isn't returned to the caller, but is part of the *evaluation* expression. So, we call `key2` an **evaluation key**. On the other hand, `key1` doesn't play a role in *evaluation*, but is an *artifact* of the operation. So, we call this a **operation key**. As you can imagine, in more complex examples, a key can play both roles.

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

##### Expanded Grammar
* This grammar has been expanded and renamed from _Concourse Criteria Language_ to **Concourse Command Language**. In addition to supporting the parsing of `Condition` statements, this grammar now supports parsing the following additional statements:
  * Page
  * Order

###### Page Statements
A Page statement can be parsed from the following forms:
  * `SIZE n` = the first page with `n` items
  * `PAGE n` = the `n`th page with the default number of items
  * `PAGE m SIZE n` = the `m`th page with `n` items

###### Order Statements
An Order statement can be parsed from the following forms:
  * `ORDER BY {key}` = sort by a single key
  * `ORDER BY {key1}, {key2}, ... {keyn}` = sort by multiple keys
  * `ORDER BY {key} {direction}` = sort by a single key with `direction`
  * `ORDER BY {key1} {direction}, {key2}, ... {key3} {direction}` = sort by multiple keys, each with an independent and optional `direction`
  * `ORDER BY {key} at {timestamp}` = sort by a single key at `timestamp`
  * `ORDER BY {key1} at {timestamp}, {key2}, ... {keyn} at {timestamp}` = sort by multiple keys, each with an independent and optional `timestamp`
  * `ORDER BY {key} {direction} at {timestamp}` = sort by a single key at `timestamp` with `direction
  * `ORDER BY {key1} {direction} at {timestamp}, {key2}, ... {keyn} {direction} at {timestamp}` = sort by multiple keys, each with an independent and optional `timestamp` and `direction`

##### API Breaks
* The `Expression` symbol has been deprecated and renamed `ExpressionSymbol` for clarity.
* The deprecated `ConcourseParser` has been removed.
* Renamed the `com.cinchapi.ccl.v2.generated` package to `com.cinchapi.ccl.generated`.
* The `Parser` construct has been deprecated in favor of a `Compiler`. Compilers are superior to Parsers because they provide a superset of functonality and are stateless.

#### Bug Fixes
* Fixed a bug that caused erroneous parsing errors in a CCL statement containing the `REGEX`, `NREGEX`, `LIKE`, or `NOT_LIKE` operators followed by a string value with a parenthesis (e.g. a regex grouping character).

#### Version 2.6.3 (May 9, 2020)
* Fixed a bug that caused non-numeric Tags to be erroneously parsed and transformed into symbols containing String values instead of Tag values
* Fixed a bug that caused Strings or String-like values that contained an `=` (equals sign) or whitespace character to not be properly quoted in a `ValueSymbol`.

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
