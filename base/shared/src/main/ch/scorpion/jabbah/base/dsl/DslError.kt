package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations

open class DslError(val location: CodeLocation, msg: String)
	: Throwable(Translations.getString("base.dsl.error.msg", msg, location))

/** Thrown by [Lexer] and [Parser] if a syntax error is detected.*/
class SyntaxError(location: CodeLocation, msg: String) : DslError(location, msg)

/** Thrown during semantic analysis by [SemanticAnalyser].*/
class SemanticError(location: CodeLocation, msg: String) : DslError(location, msg)

/** Thrown by [Interpreter] during program execution. */
class RuntimeError(location: CodeLocation, msg: String) : DslError(location, msg)