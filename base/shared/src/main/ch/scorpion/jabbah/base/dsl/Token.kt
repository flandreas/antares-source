package ch.scorpion.jabbah.base.dsl

enum class TokenType(val id: String) {
	LITERAL("literal"),
	PLUS("+"),
	MINUS("-"),
	MULTIPLY("*"),
	DIVIDE("/"),
	LPAREN("("),
	RPAREN(")"),
	EOF("EOF"),
	ASSIGN("="),
	ID("ID"),
	LCURLEY("{"),
	RCURLEY("}"),
	VAR("var"),
	STORE("store"),
	EQUAL("="),
	DIFF("!="),
	SMALLER("<"),
	SMALLER_EQUAL("<="),
	GREATER(">"),
	GREATER_EQUAL(">="),
	IF("if"),
	ELSE("else"),
	AND("and"),
	OR("or"),
	NOT("not"),
	SHIFT_LEFT("<<"),
	SHIFT_RIGHT(">>"),
	MOD("%"),
	WHEN("when"),
	COLON(":"),
	FOR("for"),
	IN("in"),
	TO("to"),
	LEFT_BRACKET("["),
	RIGHT_BRACKET("]"),
	QUESTION_MARK("?"),
	RETURN("return"),

	// TokenTypes needed by higher level modules
	CARET("^"),
	INIT("init"),
	AT("@"),
	HASH("#"),
	DOT("."),
	COMMA(","),
	DOUBLE_QUOTE("\"")
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