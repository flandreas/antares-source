package io.antarescircuit.jabbah.base.dsl

import io.antarescircuit.jabbah.base.parser.TokenType

enum class BaseTokenType(override val id: String) : TokenType {
	ID("ID"),
	LITERAL("literal"),
	EOF("EOF"),
	EOL("EOL"),
	DOUBLE_QUOTE("\""),
	SINGLE_QUOTE("'"),
}