package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.parser.TextLocation

open class DslError(val location: TextLocation, msg: String) : Throwable(msg) {
	override fun toString(): String =
		Translations.getString("base.dsl.error.msg", message!!, location)
}

/** Thrown by [DslLexer] and [DslParser] if a syntax error is detected.*/
class SyntaxError(location: TextLocation, msg: String) : DslError(location, msg)

/** Thrown during semantic analysis by [SemanticAnalyser].*/
class SemanticError(location: TextLocation, msg: String) : DslError(location, msg)

/** Thrown by [Interpreter] during program execution. */
class RuntimeError(location: TextLocation, msg: String) : DslError(location, msg)