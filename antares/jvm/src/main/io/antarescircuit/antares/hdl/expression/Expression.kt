package io.antarescircuit.antares.hdl.expression

import io.antarescircuit.antares.hdl.HDLNet
import io.antarescircuit.antares.model.signal.DigitalSignal

interface Expression {
	val delay: Long
}

abstract class AbstractExpression(
	override val delay: Long = 0
) : Expression

/** A reference to a [HDLNet]. The value of the [Expression] is the name of the [HDLNet].*/
class NetExpression(val net: HDLNet, delay: Long = 0) : AbstractExpression(delay)

/**
 * Represents a NOT operation.
 * @property expression the [Expression] to invert
 */
class NotExpression(val expression: Expression, delay: Long = 0): AbstractExpression(delay)

class OperationExpression(
	val operation: Operation,
	val operands: List<Expression>,
	delay: Long = 0
) : AbstractExpression(delay) {

	enum class Operation {
		AND,
		OR,
		XOR
	}
}

class ConstantExpression(
	val value: DigitalSignal,
	delay: Long = 0
) : AbstractExpression(delay)