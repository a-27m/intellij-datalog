# Changelog 

## Unreleased

### Soufflé Language Support
* Parse float (`1.5`) and unsigned (`1u`, `0x1Fu`) literals, and escaped quotes in strings.
* Add multi-head rules (`a(x), b(x) :- c(x).`), `.plan`, `.override` and `.limitsize`.
* Add algebraic data types (`.type T = A {x: number} | B {}`) with `$A(1)` constructors that resolve to their branch.
* Accept comma-separated relation lists in `.input`, `.output` and `.printsize` with a shared parameter list, and arbitrary
  I/O parameters such as `delimiter` (previously only a fixed set was accepted).
* Add the `choice-domain`, `no_inline`, `magic`, `no_magic` and `btree_delete` relation qualifiers, and `stateful` functors
  with named or user-typed parameters.
* Add `#if`, `#elif` and `#else`.
* Add unary `-`, `bnot` and `lnot`, the `bshl`, `bshr` and `bshru` operators, `as(x, T)` casts and the `max(a, b)` / `min(a, b)` functors.
* Accept primitive types as component arguments (`.init c = C<number>`) and `.type Name` without a definition.
* Fix `max` and `min` token names being swapped.


## 2.0.0

### Syntax and Language Support
* Add support for base type syntax (#4).
* Add support `float` and `unsigned` data types.
* Fix parsing for aggregations; add `mean` aggregator.
* Improve support for parameters of `.input` and `.oudput` directives.

### Editor
 * Fix syntax highlighting of `#include` directives.
 * Fix bug in reference resolution logic (#3).
 * Fix missing jump-to-relation line markers for fact statements.
 * Improve jump-to-base / jump-to-sub-components line markers for components.
 * Add quick-fix for replacing unused variables with wildcards in statements.

### Performance and Stability
 * Replace usages of deprecated APIs (#6).
 * Fix performance issues for line marker generation.
 * Use latest IntelliJ Plugin SDK.

## 1.1.0
 * Fix annotations for relation arity mistakes.
 * Extend documentation parsing to all declaration types (e.g. preprocessor definitions, types, etc.).
 * Include comment declarations in code completion.
 * Fix rule lookup via gutter-icon.
 * Add support for relation qualifiers.
 
## 1.0.0
 * Initial release.