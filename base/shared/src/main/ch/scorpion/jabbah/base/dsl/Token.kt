package ch.scorpion.jabbah.base.dsl

enum class TokenType {
	LITERAL,
	PLUS,
	MINUS,
	MULTIPLY,
	DIVIDE,
	LPAREN,
	RPAREN,
	EOF,
	ASSIGN,
	ID,
	LCURLEY,
	RCURLEY,
	VAR,
	STORE,
	EQUAL,
	DIFF,
	SMALLER,
	SMALLER_EQUAL,
	GREATER,
	GREATER_EQUAL,
	IF,
	ELSE,
	AND,
	OR,
	NOT,
	SHIFT_LEFT,
	SHIFT_RIGHT,
	MOD,
	WHEN,
	COLON,
	FOR,
	IN,
	TO,
	CARET,
	LEFT_BRACKET,
	RIGHT_BRACKET,
	QUESTION_MARK,
	RETURN
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