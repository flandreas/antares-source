package ch.scorpion.antares.dsl

enum class TokenType {
	INTEGER,
	PLUS,
	MINUS,
	MULTIPLY,
	DIVIDE,
	LPAREN,
	RPAREN,
	EOL,
	EOF,
	ASSIGN,
	ID,
	LCURLEY,
	RCURLEY,
	VAR
}

/**
 * The result of [Lexer] representing a part of a scanned text.
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