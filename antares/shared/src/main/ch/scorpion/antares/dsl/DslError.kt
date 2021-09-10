package ch.scorpion.antares.dsl

open class DslError(location: CodeLocation, msg: String) : Throwable("$msg at $location")

/** Thrown by [Lexer] and [Parser] if a syntax error is detected.*/
class SyntaxError(location: CodeLocation, msg: String) : DslError(location, msg)

/** Thrown during semantic analysis by [SymbolTableBuilder].*/
class SemanticError(location: CodeLocation, msg: String) : DslError(location, msg)

/** Thrown by [Interpreter] during program execution. */
class RuntimeError(location: CodeLocation, msg: String) : DslError(location, msg)