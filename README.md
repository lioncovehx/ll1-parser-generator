# LL(1) Parser Generator

The second assignment for Compiler Construction (CS4031, FAST-NUCES), by Saad Lone and Mobeen Hassan.

You give it a context-free grammar in a text file. It rewrites the grammar into a form a top-down parser can use, builds the LL(1) parsing table, and then parses input strings with a stack, printing every step and drawing a parse tree. Everything is written from scratch in Java, with no parser libraries.

## What it does

1. **Left factoring.** Productions that start with the same symbols get split out into a new non-terminal. This repeats until no common prefixes are left.
2. **Left recursion removal.** Removes both direct recursion (`Expr -> Expr + Term`) and indirect recursion (where `A -> B ...` and `B -> A ...`).
3. **FIRST and FOLLOW sets** for every non-terminal.
4. **LL(1) table.** If two rules land in the same cell, the grammar isn't LL(1), and it reports exactly which cells conflict.
5. **Stack-based parsing.** It prints a full trace (stack, remaining input, action) for each string and draws an ASCII parse tree for the ones it accepts.
6. **Panic-mode error recovery.** When it hits a bad token, it reports the error, skips ahead to a token in the FOLLOW set and keeps parsing. One mistake doesn't end the run.

## Test grammars and results

| Grammar | What it tests | LL(1)? |
|---|---|---|
| `grammar1.txt` | Simple `a b` sequence with an optional part | Yes |
| `grammar2.txt` | Arithmetic expressions (`+`, `*`, brackets). Left-recursive, so it has to be rewritten first | Yes, after rewriting |
| `grammar3.txt` | `if-then-else` (dangling else) | No. The conflict is reported, as it should be |
| `grammar4.txt` | Indirect left recursion | No. The recursion is removed, but there's still a conflict |

Each grammar is tested with a few valid strings and a few broken ones; they're set in `Main.java`. For the expression grammar, `id + id`, `id * id` and `( id + id ) * id` are all accepted with the correct trees. For `id + * id` and `+ id`, it reports the error at the right token and recovers. There are more test strings in `input/input_valid.txt` and `input/input_errors.txt`.

The outputs from our run are saved in `output/`:

| File | Contents |
|---|---|
| `grammar_transformed.txt` | Each grammar after factoring and recursion removal |
| `first_follow_sets.txt` | FIRST and FOLLOW sets |
| `parsing_table.txt` | LL(1) tables and conflict reports |
| `parsing_trace1.txt` | Step-by-step traces for the valid inputs |
| `parsing_trace2.txt` | Traces for the invalid inputs, showing the error recovery |
| `parse_trees.txt` | Parse trees for the accepted strings |

Example tree for `( id + id ) * id`:

```
└── Expr
    ├── Term
    │   ├── Factor
    │   │   ├── (
    │   │   ├── Expr
    │   │   │   ├── Term
    │   │   │   │   ├── Factor
    │   │   │   │   │   └── id
...
```

## Run it

You need JDK 8 or newer. Run these from the project root, because the classes are in `package src`:

```bash
javac src/*.java
java -cp . src.Main
```

It runs all four grammars and rewrites the files in `output/`.

## Grammar file format

```
Expr -> Expr + Term | Term
Term -> Term * Factor | Factor
Factor -> ( Expr ) | id
```

- `->` separates the left side from the rules, and `|` separates alternatives.
- Symbols are separated by spaces.
- Write the empty string as `epsilon` or `@`.
- Input strings are space-separated tokens (`id + id * id`). The `$` at the end is added for you.

## Code

| File | What it does |
|---|---|
| `Grammar.java` | Reads the grammar, does left factoring and left recursion removal |
| `FirstFollow.java` | Computes FIRST and FOLLOW sets |
| `Parser.java` | Builds the table, runs the parse, builds the tree |
| `ErrorHandler.java` | Panic-mode recovery |
| `Stack.java`, `Tree.java` | Parse stack and parse tree |
| `Main.java` | Runs every grammar and writes the outputs |
