package io.antarescircuit.jabbah.base.parser

import io.antarescircuit.jabbah.base.dsl.DslLexer

/**
 * Defines the unique types of [Token]s.
 * Every scanned language will define its own closed set of [TokenType]s.
 *
 * @property id the unique identification to be used in user-facing syntax error messages, such as
 * "Unexpected token '('."
 * @property name the name of this [TokenType] to be used in internal technical messages or
 * test assertions. Typically implemented as enum value name.
 */
interface TokenType {
	val id: String
	val name: String
}

/**
 * The result of [DslLexer] representing a part of a scanned text.
 *
 * @property type the type of this [Token]
 * @property T the type of the [value] of this [Token]. Can be [Unit] for [Tokens][Token] without value.
 */
data class Token<out T: Any>(
	val type: TokenType,
	val value: T? = null
) {
	override fun toString(): String =
		value?.let { "Token(${type.name}, $it)" } ?: "Token(${type.name})"
}