package ch.scorpion.antares.hdl.expression

import ch.scorpion.antares.hdl.HDLNet
import ch.scorpion.antares.model.signal.DigitalSignal

interface Expression

/** A reference to a [HDLNet]. The value of the [Expression] is the name of the [HDLNet].*/
class NetExpression(val net: HDLNet) : Expression

/**
 * Represents a NOT operation.
 * @property expression the [Expression] to invert
 */
class NotExpression(val expression: Expression): Expression

class OperationExpression(
	val operation: Operation,
	val operands: List<Expression>
) : Expression {

	enum class Operation {
		AND,
		OR,
		XOR
	}
}

class ConstantExpression(
	val value: DigitalSignal
) : Expression