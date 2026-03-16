# Concourse Command Language (CCL) Reference

CCL is the query and command language for [Concourse](https://cinchapi.com/technology/concourse), a distributed database for transactions and real-time search. This document is the authoritative reference for the language grammar, syntax, and semantics.

**Parser:** JavaCC (grammar defined in `grammar/grammar.jjt`)
**Package:** `com.cinchapi.ccl`

---

## Table of Contents

1. [Language Overview](#1-language-overview)
2. [Statement Types](#2-statement-types)
3. [Data Types and Values](#3-data-types-and-values)
4. [Keys](#4-keys)
5. [Operators](#5-operators)
6. [Conditions (Expressions)](#6-conditions-expressions)
7. [Logical Connectives](#7-logical-connectives)
8. [Timestamps](#8-timestamps)
9. [Ordering](#9-ordering)
10. [Pagination](#10-pagination)
11. [Functions](#11-functions)
12. [Commands](#12-commands)
    - [Data Modification](#data-modification-commands)
    - [Record Operations](#record-operations)
    - [Link Operations](#link-operations)
    - [Query Operations](#query-operations)
    - [Transaction Operations](#transaction-operations)
    - [Revert Operation](#revert-operation)
    - [Utility Operations](#utility-operations)
13. [Multi-Statement Support](#13-multi-statement-support)
14. [Variable References](#14-variable-references)
15. [Escape Sequences](#15-escape-sequences)
16. [Operator Precedence](#16-operator-precedence)
17. [Formal Grammar Summary](#17-formal-grammar-summary)

---

## 1. Language Overview

CCL is a case-insensitive language (all keywords can be written in any case). Whitespace (spaces and tabs) separates tokens but is otherwise insignificant.

A CCL input can be one of five statement types:
- A **condition** (filter expression with optional ordering and pagination)
- An **order** specification
- A **page** specification
- A **command** (a database operation like `SELECT`, `FIND`, `ADD`, etc.)
- A **function** (an aggregate computation)

Multiple statements can be separated by semicolons.

### Preposition Aliasing

CCL accepts semantically appropriate preposition aliases before record references. The full rules are:

| Operation Type | Commands | Accepted Prepositions |
|----------------|----------|----------------------|
| **Write/Put** | ADD, INSERT | `in`, `to`, `within`, `into` (INSERT only) |
| **Write/Put** | SET | `in`, `within` |
| **Read/Extract** | SELECT, GET, NAVIGATE, CALCULATE | `from`, `in`, `within` |
| **Remove** | REMOVE, CLEAR | `from`, `in`, `within` |
| **Inspect** | VERIFY, VERIFY_AND_SWAP, VERIFY_OR_SET, CHRONICLE, DIFF, AUDIT, REVERT, RECONCILE | `in`, `within` |
| **Directional** | LINK, UNLINK | `from` ... `to` (no aliases) |

---

## 2. Statement Types

The top-level grammar rule is:

```
Statement :=
    Command
  | [WHERE] Condition [Order] [Page]
  | Page
  | Order
  | Function
```

A bare condition can optionally be prefixed with the `where` keyword. Order and page clauses can follow a condition.

---

## 3. Data Types and Values

### Strings

Strings can be quoted with double quotes, single quotes, or backticks:

```
"hello world"
'hello world'
`hello world`
```

Smart quotes (curly quotes) are also accepted. Escape sequences `\"` and `\'` are supported within quoted strings.

Unquoted values are also valid when they don't conflict with keywords or operators. Multi-word unquoted values are supported (tokens are joined with spaces):

```
name = Jeff Nelson
```

### Numbers

```
42              -- integer
-7              -- signed integer
3.14            -- decimal
-2.5            -- signed decimal
```

Unquoted numeric strings are parsed by the value transform function, which determines whether they become integers, longs, floats, etc.

### Record Identifiers

Records are identified by positive integer IDs:

```
1
42
1000
```

### Record Collections

Multiple record IDs listed comma-separated. Brackets are optional:

```
[1, 2, 3]
1, 2, 3
```

### Key Collections

Multiple keys listed comma-separated. Brackets are optional:

```
[name, age, email]
name, age, email
```

### Value Collections

Multiple values enclosed in square brackets (used by `reconcile`):

```
[jeff, bob, alice]
```

### Reserved Identifiers

`$id$` is reserved for JSON record identifiers, used with `jsonify ... with $id$`.

---

## 4. Keys

Keys identify fields/columns in the database. There are three forms:

### Simple Keys

Alphanumeric identifiers (may include underscores, digits):

```
name
age
favorite_color
$id$
```

### Navigation Keys

Dot-separated paths that traverse linked records:

```
friends.name
mother.children.age
location.address.city
```

### Function Keys

A key piped to an aggregation function (see [Functions](#11-functions)):

```
score | avg
age | sum
```

---

## 5. Operators

All operator keywords are case-insensitive.

### Comparison Operators (Unary)

These operators compare a key to a single value:

| Syntax | Aliases | Meaning |
|--------|---------|---------|
| `=` | `==`, `eq`, `equals` | Equals |
| `!=` | `ne`, `not_equals` | Not equals |
| `>` | `gt`, `greater_than` | Greater than |
| `>=` | `gte`, `greater_than_or_equals` | Greater than or equal |
| `<` | `lt`, `less_than` | Less than |
| `<=` | `lte`, `less_than_or_equals` | Less than or equal |

### Binary Operator

Requires two values:

| Syntax | Aliases | Meaning |
|--------|---------|---------|
| `><` | `bw`, `between` | Between (inclusive) |

### String/Pattern Operators

| Syntax | Aliases | Meaning |
|--------|---------|---------|
| `regex` | | Regular expression match |
| `nregex` | `not_regex` | Negated regex match |
| `like` | | Pattern match (SQL-style) |
| `nlike` | `not_like` | Negated pattern match |

### Search Operators

| Syntax | Aliases | Meaning |
|--------|---------|---------|
| `~` | `contains`, `search_match` | Full-text search match |
| `!~` | `not_contains`, `search_exclude` | Negated search match |

### Link Operator

| Syntax | Aliases | Meaning |
|--------|---------|---------|
| `->` | `lnk2`, `lnks2`, `links_to` | Links to record |

The `links_to` operator's value must be a numeric record ID.

---

## 6. Conditions (Expressions)

A condition is one or more relational expressions joined by logical connectives.

### Relational Expressions

The basic form of an expression is:

```
key operator value [timestamp]
```

For binary operators (between):

```
key operator value1 value2 [timestamp]
```

For regex operators, the pattern can be parenthesized:

```
key regex (pattern)
key regex pattern
```

For search operators:

```
key ~ value
key !~ value
```

### Examples

```
name = jeff
age > 30
age >= 18
score != 0
email like %example.com%
age bw 18 65
name regex ([A-Z][a-z]+)
friends -> 42
tags ~ database
```

---

## 7. Logical Connectives

Expressions can be combined with `AND` and `OR`, and grouped with parentheses.

### AND (Conjunction)

| Syntax |
|--------|
| `and` |
| `&&` |
| `&` |

### OR (Disjunction)

| Syntax |
|--------|
| `or` |
| `\|\|` |

### Grouping

Parentheses override default precedence:

```
(a = 1 or b = 2) and (c = 3 or d = 4)
```

### Precedence

`AND` binds tighter than `OR`. Both are left-associative.

```
a = 1 or b = 2 and c = 3
```
is equivalent to:
```
a = 1 or (b = 2 and c = 3)
```

---

## 8. Timestamps

Timestamps specify a point in time for historical queries. They are introduced by one of these keywords (all synonyms):

| Keyword |
|---------|
| `at` |
| `on` |
| `during` |
| `in` |

Additionally, `as of` is accepted in command contexts (read commands).

### Timestamp Values

Timestamps can be:

- **Natural language:** `"yesterday"`, `"last week"`, `"last christmas"`, `"3 days ago"`
- **Date strings:** `"2024-01-15"`, `"2024-01-15 10:30:00"`
- **Microsecond epoch:** A numeric value representing microseconds since Unix epoch

Timestamp values are typically quoted. They are parsed by the `NaturalLanguage.parseMicros()` utility.

### Usage in Expressions

```
name = jeff at "yesterday"
age > 30 on "2024-01-01"
score >= 90 during "last week"
```

### Usage in Commands

```
select name from 1 at "yesterday"
find age > 30 as of "2024-01-01"
describe 1 at "last christmas"
```

Some commands accept two timestamps for range queries:

```
diff 1 from "yesterday" to "today"
chronicle name in 1 from "2024-01-01" to "2024-06-01"
audit 1 from "last month" to "today"
```

---

## 9. Ordering

Order clauses sort results by one or more keys.

### Syntax

```
order by key [direction] [timestamp], key [direction] [timestamp], ...
```

### Direction

| Syntax | Meaning |
|--------|---------|
| `asc` | Ascending (default) |
| `desc` | Descending |
| `<` (prefix) | Ascending (symbolic) |
| `>` (prefix) | Descending (symbolic) |

When no direction is specified, ascending is the default.

### Symbolic Direction

The `<` and `>` symbols are placed **before** the key:

```
order by <name        -- ascending by name
order by >age         -- descending by age
```

### Word Direction

The `asc` and `desc` keywords are placed **after** the key:

```
order by name asc
order by age desc
```

### Multi-Key Ordering

```
order by name asc, age desc, score
order by >name, <age, score desc
```

### Ordering with Timestamps

```
order by name at "yesterday"
order by name asc at "2024-01-01", age desc
```

### Usage with Commands and Conditions

```
find age > 30 order by name
select name where age > 30 order by age desc
select name from 1 order by name page 1 size 10
```

---

## 10. Pagination

Page clauses limit and offset results.

### Syntax

```
page <number>
size <number>
page <number> size <number>
size <number> page <number>
```

`page` and `size` can appear in either order.

- `page` specifies the 1-based page number
- `size` specifies the number of results per page

### Examples

```
page 1
page 2 size 10
size 50 page 3
```

### Usage with Conditions and Commands

```
find age > 30 page 1 size 20
select name where age > 30 order by name page 2 size 10
```

---

## 11. Functions

Functions compute aggregate values. They appear in two contexts: as keys (implicit) and as values (explicit).

### Implicit Key Functions (Pipe Syntax)

Used on the key side of an expression. The pipe `|` separates the key from the function name:

```
key | function_name
```

This computes the function over all values of the key and uses the result as the evaluation key.

```
score | avg > 80
age | sum > 1000
items | count > 5
```

### Explicit Value Functions (Call Syntax)

Used on the value side of an expression. Four forms exist:

**1. Index Function** -- Aggregates across all records:

```
function(key)
```

```
age > avg(age)
score >= min(score)
```

**2. Index Function with Timestamp:**

```
function(key, at timestamp)
```

```
age > avg(age, at "yesterday")
```

**3. Key-Records Function** -- Aggregates over specific records:

```
function(key, record1, record2, ...)
function(key, [record1, record2, ...], at timestamp)
```

```
age > avg(age, 1, 2, 3)
score > sum(score, [1, 2, 3], at "yesterday")
```

**4. Key-Condition Function** -- Aggregates over records matching a condition:

```
function(key, condition)
function(key, condition, at timestamp)
```

```
age > avg(age, department = engineering)
score > sum(score, status = active, at "last week")
```

### Standalone Functions

Functions can also be standalone statements:

```
score | avg
avg(score)
avg(score, 1, 2, 3)
avg(score, age > 30)
```

### Common Function Names

`avg`, `average`, `sum`, `count`, `min`, `max`

(The function name is passed through to the execution layer; any name the backend supports is valid.)

---

## 12. Commands

Commands are database operations. All command keywords are case-insensitive.

---

### Data Modification Commands

#### ADD

Add a value to a key, optionally in specific record(s).

```
add <key> as <value>
add <key> as <value> (in|to|within) <record>
add <key> as <value> (in|to|within) [<records>]
```

```
add name as jeff
add name as jeff in 1
add name as jeff to [1, 2, 3]
add name as jeff in 1, 2, 3
```

#### SET

Set a value for a key in a record (replaces existing values).

```
set <key> as <value> (in|within) <record>
set <key> as <value> (in|within) [<records>]
```

```
set name as jeff in 1
set name as jeff within [1, 2, 3]
set name as jeff in 1, 2, 3
```

#### REMOVE

Remove a specific value from a key.

```
remove <key> as <value> (from|in|within) <record>
remove <key> as <value> (from|in|within) [<records>]
```

```
remove name as jeff from 1
remove name as jeff from [1, 2, 3]
```

Note: `from`, `in`, and `within` are interchangeable in the remove command.

#### CLEAR

Clear all values for a key in a record, or clear an entire record.

```
clear <record>
clear [<records>]
clear <key> (from|in|within) <record>
clear <key> (from|in|within) [<records>]
clear [<keys>] (from|in|within) <record>
clear [<keys>] (from|in|within) [<records>]
```

```
clear 1
clear [1, 2, 3]
clear 1, 2, 3
clear name from 1
clear name in 1
clear [name, age] within [1, 2, 3]
clear name, age from 1
```

#### VERIFY_AND_SWAP

Atomic compare-and-swap: if the key has the expected value, replace it.

```
verify_and_swap <key> as <expected> (in|within) <record> with <replacement>
verifyAndSwap <key> as <expected> (in|within) <record> with <replacement>
```

```
verify_and_swap name as jeff in 1 with bob
verifyAndSwap status as pending in 42 with active
```

#### VERIFY_OR_SET

If the key doesn't have the value, set it.

```
verify_or_set <key> as <value> (in|within) <record>
verifyOrSet <key> as <value> (in|within) <record>
```

```
verify_or_set name as jeff in 1
verifyOrSet status as active in 42
```

---

### Record Operations

#### INSERT

Insert a JSON document as a new record or into existing record(s).

```
insert <json>
insert <json> (in|into|to|within) <record>
insert <json> (in|into|to|within) [<records>]
```

The JSON must be a quoted string containing a valid JSON object:

```
insert '{"name": "jeff", "age": 30}'
insert '{"status": "active"}' in 1
insert '{"role": "admin"}' into [1, 2, 3]
insert '{"role": "admin"}' to 1
```

Note: `in`, `into`, `to`, and `within` are interchangeable.

---

### Link Operations

#### LINK

Create a link between records through a key.

```
link <key> from <source> to <destination>
link <key> from <source> to [<destinations>]
```

```
link friends from 1 to 2
link friends from 1 to [2, 3, 4]
```

#### UNLINK

Remove a link between records.

```
unlink <key> from <source> to <destination>
```

```
unlink friends from 1 to 2
```

---

### Query Operations

#### SELECT

Select key values from records, with condition, or by record ID.

```
select <key> (from|in|within) <record> [timestamp] [order] [page]
select <key> (from|in|within) <records> [timestamp] [order] [page]
select <keys> (from|in|within) <record> [timestamp] [order] [page]
select <keys> (from|in|within) <records> [timestamp] [order] [page]
select <key> where <condition> [timestamp] [order] [page]
select <keys> where <condition> [timestamp] [order] [page]
select where <condition> [timestamp] [order] [page]
select <record> [timestamp]
select <records> [timestamp]
```

```
select name from 1
select name in 1
select [name, age] within 1
select name, age from 1
select name from [1, 2, 3]
select 1
select 1, 2, 3
select name where age > 30
select where age > 30
select name where age > 30 order by name page 1 size 10
select name from 1 at "yesterday"
select name from 1 as of "2024-01-01"
```

#### GET

Get key values (similar to select with different semantics).

```
get <key> (from|in|within) <record> [timestamp] [order] [page]
get <key> (from|in|within) <records> [timestamp] [order] [page]
get <keys> (from|in|within) <record> [timestamp] [order] [page]
get <keys> (from|in|within) <records> [timestamp] [order] [page]
get <key> where <condition> [timestamp] [order] [page]
get <keys> where <condition> [timestamp] [order] [page]
get where <condition> [timestamp] [order] [page]
get (from|in|within) <record> [timestamp] [order] [page]
get (from|in|within) <records> [timestamp] [order] [page]
get <record> [timestamp]
get <records> [timestamp]
```

```
get name from 1
get name in 1
get [name, age] within [1, 2, 3]
get name, age from 1
get name where age > 30
get where age > 30
get from 1
get from [1, 2, 3]
get 1
get [1, 2, 3]
get 1, 2, 3
get [name, age] where score > 90 order by name
```

#### FIND

Find records matching a condition.

```
find <condition> [timestamp] [order] [page]
```

```
find age > 30
find age > 30 order by name
find age > 30 page 2 size 20
find name = jeff at "yesterday"
find age > 30 as of "2024-01-01"
```

#### FIND_OR_ADD

Find a record with the given key-value, or add it if none exists.

```
findOrAdd <key> as <value>
find_or_add <key> as <value>
```

```
findOrAdd name as jeff
```

#### FIND_OR_INSERT

Find records matching a condition, or insert a JSON document if none match.

```
findOrInsert <condition> [timestamp] <json>
find_or_insert <condition> [timestamp] <json>
```

```
findOrInsert age > 30 '{"name": "jeff", "age": 35}'
findOrInsert age > 30 at "2024-01-01" '{"name": "jeff", "age": 35}'
```

**Note:** The timestamp must be a single token (quoted string or number). Expression-level timestamps within parenthesised sub-conditions are also supported:

```
findOrInsert (age > 30 at "yesterday") at "2024-01-01" '{"name": "jeff"}'
```

#### DESCRIBE

Describe the keys in a record or all records.

```
describe [timestamp]
describe <record> [timestamp]
describe [<records>] [timestamp]
```

```
describe
describe 1
describe [1, 2, 3]
describe 1 at "yesterday"
describe as of "2024-01-01"
```

#### VERIFY

Check if a specific key-value pair exists in a record.

```
verify <key> as <value> (in|within) <record> [timestamp]
```

```
verify name as jeff in 1
verify name as jeff in 1 at "yesterday"
```

#### SEARCH

Full-text search for a query string within a key.

```
search <key> for <quoted_query>
```

```
search name for "jeff"
search description for "database engine"
```

#### BROWSE

Browse all values for a key or keys.

```
browse <key> [timestamp]
browse [<keys>] [timestamp]
```

```
browse name
browse [name, age]
browse name at "yesterday"
```

#### NAVIGATE

Navigate linked data through navigation keys.

```
navigate <key> (from|in|within) <record> [timestamp]
navigate <key> (from|in|within) [<records>] [timestamp]
navigate [<keys>] (from|in|within) <record> [timestamp]
navigate [<keys>] (from|in|within) [<records>] [timestamp]
navigate <key> where <condition> [timestamp]
navigate [<keys>] where <condition> [timestamp]
```

```
navigate friends.name from 1
navigate friends.name in 1
navigate [friends.name, age] within [1, 2]
navigate friends.name where age > 30
```

#### CHRONICLE

View the change history for a key in a record.

```
chronicle <key> (in|within) <record> [from <start_timestamp> to <end_timestamp>]
```

```
chronicle name in 1
chronicle name in 1 from "2024-01-01" to "2024-06-01"
```

#### DIFF

Compare state between timestamps.

```
diff <record> from <start_timestamp> to <end_timestamp>
diff <key> (in|within) <record> from <start_timestamp> to <end_timestamp>
diff <key> from <start_timestamp> to <end_timestamp>
```

```
diff 1 from "yesterday" to "today"
diff name in 1 from "yesterday" to "today"
diff name within 1 from "yesterday" to "today"
diff name from "last week" to "today"
```

#### AUDIT

View the audit log for a record or a key in a record.

```
audit <record> [from <start_timestamp> to <end_timestamp>]
audit <key> (in|within) <record> [from <start_timestamp> to <end_timestamp>]
```

```
audit 1
audit name in 1
audit 1 from "2024-01-01" to "2024-06-01"
audit name in 1 from "last month" to "today"
```

#### TRACE

Trace incoming references to a record.

```
trace <record> [timestamp]
trace [<records>] [timestamp]
```

```
trace 1
trace [1, 2, 3]
trace 1 at "yesterday"
```

#### INVENTORY

List all records in the database.

```
inventory
```

#### JSONIFY

Export records as JSON.

```
jsonify <record> [with $id$] [timestamp]
jsonify [<records>] [with $id$] [timestamp]
```

```
jsonify 1
jsonify [1, 2, 3]
jsonify 1 with $id$
jsonify 1 at "yesterday"
```

The `with $id$` option includes the record identifier in the JSON output.

#### RECONCILE

Reconcile a key's values in a record with a given set of values.

```
reconcile <key> (in|within) <record> with [<values>]
```

```
reconcile tags in 1 with [database, nosql, java]
```

---

### Transaction Operations

#### STAGE

Begin a new transaction.

```
stage
```

#### COMMIT

Commit the current transaction.

```
commit
```

#### ABORT

Abort (rollback) the current transaction.

```
abort
```

---

### Revert Operation

#### REVERT

Revert key(s) in record(s) to a previous state at a given timestamp.

```
revert <key> (in|within) <record> <timestamp>
revert <key> (in|within) [<records>] <timestamp>
revert [<keys>] (in|within) <record> <timestamp>
revert [<keys>] (in|within) [<records>] <timestamp>
```

```
revert name in 1 at "yesterday"
revert name in [1, 2, 3] at "2024-01-01"
revert [name, age] in 1 at "last week"
revert [name, age] in [1, 2, 3] at "2024-01-01"
```

---

### Utility Operations

#### PING

Health check. Returns whether the server is reachable.

```
ping
```

#### HOLDS

Check if record(s) contain data.

```
holds <record>
holds [<records>]
```

```
holds 1
holds [1, 2, 3]
```

#### CONSOLIDATE

Merge records together.

```
consolidate <record> <record>
consolidate <record> [<records>]
```

```
consolidate 1 2
consolidate 1 [2, 3, 4]
```

#### CALCULATE

Perform aggregate calculations.

```
calculate <function> <key> [timestamp]
calculate <function> <key> (from|in|within) <record> [timestamp]
calculate <function> <key> (from|in|within) [<records>] [timestamp]
calculate <function> <key> where <condition> [timestamp]
```

```
calculate sum age
calculate avg score in 1
calculate avg score from 1
calculate count name within [1, 2, 3]
calculate sum salary where department = engineering
calculate avg score where score > 50 at "yesterday"
```

Supported function names: `sum`, `avg`, `average`, `count`, `min`, `max` (and any name the backend supports).

---

## 13. Multi-Statement Support

Multiple statements can be combined in a single input, separated by semicolons:

```
select name from 1; find age > 30; add status as active in 1
```

Leading, trailing, and consecutive semicolons are tolerated:

```
; select name from 1 ;; find age > 30 ;
```

Each statement produces its own AST. The `Compiler.compileBatch()` method returns a `List<AbstractSyntaxTree>`.

---

## 14. Variable References

Values prefixed with `$` are resolved as variable references from a provided data context (`Multimap<String, Object>`):

```
age bw $minAge $maxAge
name = $targetName
```

The variable name (without `$`) is looked up in the context. If the variable has exactly one value, it is substituted. If it has zero or multiple values, a `SyntaxException` is thrown.

---

## 15. Escape Sequences

| Sequence | Result |
|----------|--------|
| `\"` | Literal `"` inside double-quoted strings |
| `\'` | Literal `'` inside single-quoted strings |
| `\$` | Literal `$` (prevents variable resolution) |
| `\@` | Literal `@` |

---

## 16. Operator Precedence

From highest to lowest:

1. **Parentheses** `()` -- explicit grouping
2. **Relational operators** -- `=`, `!=`, `>`, `<`, `>=`, `<=`, `><`, `regex`, `like`, `->`, `~`, etc.
3. **AND** -- `and`, `&&`, `&`
4. **OR** -- `or`, `||`

AND binds more tightly than OR. Both are left-associative.

---

## 17. Formal Grammar Summary

```ebnf
(* Top-level *)
Statement         ::= Command
                    | ['where'] Condition [Order] [Page]
                    | Page
                    | Order
                    | Function

(* Conditions *)
Condition         ::= Conjunction (('or' | '||') Conjunction)*
Conjunction       ::= Unary (('and' | '&&' | '&') Unary)*
Unary             ::= '(' Condition ')' | Expression
Expression        ::= Key Operator Value [Timestamp]
                    | Key BinaryOperator Value Value [Timestamp]
                    | Key SearchOperator Value

(* Keys *)
Key               ::= FunctionKey | SimpleKey | NavigationKey
SimpleKey         ::= ALPHANUMERIC | NUMERIC | SIGNED_INTEGER | SIGNED_DECIMAL | '$id$'
NavigationKey     ::= PERIOD_SEPARATED_STRING          (* e.g., friends.name *)
FunctionKey       ::= Key '|' ALPHANUMERIC             (* e.g., score | avg *)

(* Values *)
Value             ::= FunctionValue | QUOTED_STRING | UnquotedTokens | VariableRef
FunctionValue     ::= ALPHANUMERIC '(' Key [',' Args] ')'
VariableRef       ::= '$' ALPHANUMERIC

(* Operators *)
UnaryOperator     ::= '=' | '==' | 'eq' | 'equals'
                    | '!=' | 'ne' | 'not_equals'
                    | '>' | 'gt' | 'greater_than'
                    | '>=' | 'gte' | 'greater_than_or_equals'
                    | '<' | 'lt' | 'less_than'
                    | '<=' | 'lte' | 'less_than_or_equals'
BinaryOperator    ::= '><' | 'bw' | 'between'
RegexOperator     ::= 'regex' | 'nregex' | 'not_regex' | 'like' | 'nlike' | 'not_like'
LinkOperator      ::= '->' | 'lnk2' | 'lnks2' | 'links_to'
SearchOperator    ::= '~' | 'contains' | 'search_match'
                    | '!~' | 'not_contains' | 'search_exclude'

(* Timestamp *)
Timestamp         ::= ('at' | 'on' | 'during') TimestampValue
TimestampCommand  ::= ('at' | 'on' | 'during' | 'as of') TimestampValue
TimestampValue    ::= QUOTED_STRING | NUMERIC+

(* Ordering *)
Order             ::= 'order by' OrderClause (',' OrderClause)*
OrderClause       ::= DirectionSymbol Key [Timestamp]
                    | Key [DirectionWord] [Timestamp]
DirectionSymbol   ::= '<' | '>'
DirectionWord     ::= 'asc' | 'desc'

(* Pagination *)
Page              ::= 'page' NUMERIC ['size' NUMERIC]
                    | 'size' NUMERIC ['page' NUMERIC]

(* Functions *)
Function          ::= FunctionKey | FunctionValue

(* Collections — brackets are optional for 2+ items *)
RecordCollection  ::= '[' NUMERIC (',' NUMERIC)* ']'
                    | NUMERIC (',' NUMERIC)+
KeyCollection     ::= '[' Key (',' Key)* ']'
                    | Key (',' Key)+
ValueCollection   ::= '[' Value (',' Value)* ']'
                    | Value (',' Value)+

(* JSON *)
JsonObject        ::= QUOTED_STRING       (* containing valid JSON *)

(* Prepositions *)
WritePreposition  ::= 'in' | 'to' | 'within'
SetPreposition    ::= 'in' | 'within'
InsertPreposition ::= 'in' | 'into' | 'to' | 'within'
ReadPreposition   ::= 'from' | 'in' | 'within'
RemovePreposition ::= 'from' | 'in' | 'within'
InspectPreposition::= 'in' | 'within'

(* Commands *)
Command           ::= AddCommand | SetCommand | InsertCommand
                    | RemoveCommand | ClearCommand
                    | SelectCommand | GetCommand | NavigateCommand | CalculateCommand
                    | VerifyCommand | VerifyAndSwapCommand | VerifyOrSetCommand
                    | ChronicleCommand | DiffCommand | AuditCommand
                    | RevertCommand | ReconcileCommand
                    | LinkCommand | UnlinkCommand
                    | FindCommand | PingCommand

AddCommand        ::= 'add' Key 'as' Value WritePreposition NUMERIC
                    | 'add' Key 'as' Value WritePreposition RecordCollection
SetCommand        ::= 'set' Key 'as' Value SetPreposition NUMERIC
                    | 'set' Key 'as' Value SetPreposition RecordCollection
InsertCommand     ::= 'insert' JsonObject
                    | 'insert' JsonObject InsertPreposition NUMERIC
                    | 'insert' JsonObject InsertPreposition RecordCollection
RemoveCommand     ::= 'remove' Key 'as' Value RemovePreposition NUMERIC
                    | 'remove' Key 'as' Value RemovePreposition RecordCollection
ClearCommand      ::= 'clear' Key RemovePreposition NUMERIC
                    | 'clear' Key RemovePreposition RecordCollection
                    | 'clear' KeyCollection RemovePreposition NUMERIC
                    | 'clear' KeyCollection RemovePreposition RecordCollection
                    | 'clear' NUMERIC
                    | 'clear' RecordCollection

SelectCommand     ::= 'select' Key ReadPreposition NUMERIC [TimestampCommand] [Order] [Page]
                    | 'select' Key ReadPreposition RecordCollection [TimestampCommand] [Order] [Page]
                    | 'select' KeyCollection ReadPreposition NUMERIC [TimestampCommand] [Order] [Page]
                    | 'select' KeyCollection ReadPreposition RecordCollection [TimestampCommand] [Order] [Page]
                    | 'select' Key 'where' Condition [TimestampCommand] [Order] [Page]
                    | 'select' KeyCollection 'where' Condition [TimestampCommand] [Order] [Page]
                    | 'select' 'where' Condition [TimestampCommand] [Order] [Page]
                    | 'select' NUMERIC [TimestampCommand]
                    | 'select' RecordCollection [TimestampCommand]
GetCommand        ::= 'get' Key ReadPreposition NUMERIC [TimestampCommand] [Order] [Page]
                    | 'get' Key ReadPreposition RecordCollection [TimestampCommand] [Order] [Page]
                    | 'get' KeyCollection ReadPreposition NUMERIC [TimestampCommand] [Order] [Page]
                    | 'get' KeyCollection ReadPreposition RecordCollection [TimestampCommand] [Order] [Page]
                    | 'get' Key 'where' Condition [TimestampCommand] [Order] [Page]
                    | 'get' KeyCollection 'where' Condition [TimestampCommand] [Order] [Page]
                    | 'get' 'where' Condition [TimestampCommand] [Order] [Page]
                    | 'get' ReadPreposition NUMERIC [TimestampCommand] [Order] [Page]
                    | 'get' ReadPreposition RecordCollection [TimestampCommand] [Order] [Page]
                    | 'get' NUMERIC [TimestampCommand]
                    | 'get' RecordCollection [TimestampCommand]
NavigateCommand   ::= 'navigate' Key ReadPreposition NUMERIC [TimestampCommand]
                    | 'navigate' Key ReadPreposition RecordCollection [TimestampCommand]
                    | 'navigate' KeyCollection ReadPreposition NUMERIC [TimestampCommand]
                    | 'navigate' KeyCollection ReadPreposition RecordCollection [TimestampCommand]
                    | 'navigate' Key 'where' Condition [TimestampCommand]
                    | 'navigate' KeyCollection 'where' Condition [TimestampCommand]
CalculateCommand  ::= 'calculate' ALPHANUMERIC Key [TimestampCommand]
                    | 'calculate' ALPHANUMERIC Key ReadPreposition NUMERIC [TimestampCommand]
                    | 'calculate' ALPHANUMERIC Key ReadPreposition RecordCollection [TimestampCommand]
                    | 'calculate' ALPHANUMERIC Key 'where' Condition [TimestampCommand]

VerifyCommand     ::= 'verify' Key 'as' Value InspectPreposition NUMERIC [TimestampCommand]
VerifyAndSwapCommand ::= 'verify_and_swap' Key 'as' Value InspectPreposition NUMERIC 'with' Value
VerifyOrSetCommand::= 'verify_or_set' Key 'as' Value InspectPreposition NUMERIC
ChronicleCommand  ::= 'chronicle' Key InspectPreposition NUMERIC ['from' TimestampValue 'to' TimestampValue]
DiffCommand       ::= 'diff' Key InspectPreposition NUMERIC 'from' TimestampValue 'to' TimestampValue
                    | 'diff' NUMERIC 'from' TimestampValue 'to' TimestampValue
                    | 'diff' Key 'from' TimestampValue 'to' TimestampValue
AuditCommand      ::= 'audit' Key InspectPreposition NUMERIC ['from' TimestampValue 'to' TimestampValue]
                    | 'audit' NUMERIC ['from' TimestampValue 'to' TimestampValue]
RevertCommand     ::= 'revert' Key InspectPreposition NUMERIC TimestampCommand
                    | 'revert' Key InspectPreposition RecordCollection TimestampCommand
                    | 'revert' KeyCollection InspectPreposition NUMERIC TimestampCommand
                    | 'revert' KeyCollection InspectPreposition RecordCollection TimestampCommand
ReconcileCommand  ::= 'reconcile' Key InspectPreposition NUMERIC 'with' ValueCollection
LinkCommand       ::= 'link' Key 'from' NUMERIC 'to' NUMERIC
                    | 'link' Key 'from' NUMERIC 'to' RecordCollection
UnlinkCommand     ::= 'unlink' Key 'from' NUMERIC 'to' NUMERIC

FindCommand       ::= 'find' Condition [TimestampCommand] [Order] [Page]
PingCommand       ::= 'ping'

(* Multi-statement *)
Input             ::= Statement (';' Statement)* [';']
```

---

## Appendix: AST Node Types

The compiler produces these abstract syntax tree types (in `com.cinchapi.ccl.syntax`):

| AST Class | Produced By |
|-----------|-------------|
| `ConditionTree` | Condition/where statements |
| `ExpressionTree` | Individual relational expressions |
| `ConjunctionTree` | AND/OR combinations |
| `OrderTree` | ORDER BY clauses |
| `PageTree` | PAGE/SIZE clauses |
| `CommandTree` | All command statements |
| `FunctionTree` | Standalone function statements |

All extend `AbstractSyntaxTree` and support the Visitor pattern via `accept(Visitor)`.

---

## Appendix: Java API Entry Points

| Class | Method | Purpose |
|-------|--------|---------|
| `Compiler` | `compile(String)` | Parse one or more semicolon-separated CCL statements into a list of ASTs |
| `Compiler` | `compile(String, Multimap)` | Same as above with local variable data for resolution |
| `Compiler` | `tokenize(AbstractSyntaxTree)` | Tokenize a condition AST into postfix notation symbols |
| `Parsing` | `toPostfixNotation(List<Symbol>)` | Convert symbol list to postfix (Shunting-Yard) |
