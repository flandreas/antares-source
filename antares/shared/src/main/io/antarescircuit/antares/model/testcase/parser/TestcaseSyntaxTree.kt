package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.model.testcase.Value
import io.antarescircuit.antares.model.testcase.parser.PortNameType.*
import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.base.dsl.AbstractNode
import io.antarescircuit.jabbah.base.dsl.Compound
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.parser.TextLocation
import io.antarescircuit.jabbah.base.parser.Token

class TestScript(
	location: TextLocation,
	val portNames: PortNames,
	statements: List<Node>
) : Compound<Node>(location, statements) {

	override fun toString(): String = "TestScript"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			portNames.accept(visitor)
			for (testVector in children) {
				if (!testVector.accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}
}

enum class PortNameType {
	DEFAULT,
	INPUT,
	OUTPUT
}

class PortName(
	location: TextLocation,
	val name: Token<String>,
	val type: PortNameType
) : AbstractNode(location) {

	override fun toString(): String =
		when (type) {
			DEFAULT -> name.value!!
			INPUT -> ">${name.value}"
			OUTPUT -> "<${name.value}"
		}
}

class PortNames(
	location: TextLocation,
	val names: List<PortName>
) : AbstractNode(location) {

	override fun toString(): String = names.joinToString(",") { it.toString() }
}

/** Contains [TestVectorNode]s and [LoopNode]s that are executed in a single simulation run.*/
class RunNode(
	location: TextLocation,
	children: List<Node>
): Compound<Node>(location, children) {

	override fun toString(): String = "Run"
}

/**
 * Repeats its [children] for every value of [variable] from [from] to [to] (both inclusive).
 * Counts downwards if [from] is greater than [to].
 */
class LoopNode(
	location: TextLocation,
	val variable: String,
	val from: ULong,
	val to: ULong,
	children: List<Node>
): Compound<Node>(location, children) {

	val range: ULongProgression get() = if (from <= to) from..to else from downTo to

	/** The number of iterations, or `null` if it exceeds [ULong.MAX_VALUE].*/
	val iterationCount: ULong? get() = (if (from <= to) to - from else from - to).let { if (it == ULong.MAX_VALUE) null else it + 1UL }

	override fun toString(): String = "Loop $variable $from..$to"
}

class TestVectorNode(
	location: TextLocation,
	val values: List<ValueNode>
) : AbstractNode(location) {

	override fun toString(): String = "TestVector"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			for (value in values) {
				if (!value.accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}
}

sealed class ValueNode(location: TextLocation) : AbstractNode(location) {
	abstract val type: Value.Type
}

/** A constant value, such as a number, X or Z.*/
class LiteralValueNode(
	location: TextLocation,
	val value: Value
) : ValueNode(location) {

	override val type: Value.Type get() = value.type

	override fun toString(): String = value.toString()
}

/** A parenthesized [Expression], evaluated for every test vector. [type] is [Value.Type.NORMAL] or [Value.Type.CLOCKED].*/
class ExpressionValueNode(
	location: TextLocation,
	val expression: Expression,
	override val type: Value.Type
) : ValueNode(location) {

	override fun toString(): String = if (type == Value.Type.CLOCKED) "^$expression" else "$expression"
}

/** Integer expressions in test vectors, operating on unsigned 64 bit values.*/
sealed interface Expression

class NumberExpression(val value: ULong) : Expression {
	override fun toString(): String = value.toString()
}

class VariableExpression(val location: TextLocation, val name: String) : Expression {
	override fun toString(): String = name
}

class UnaryExpression(val operator: TestcaseTokenType, val operand: Expression) : Expression {
	override fun toString(): String = "${operator.id}$operand"
}

class BinaryExpression(val left: Expression, val operator: TestcaseTokenType, val right: Expression) : Expression {
	override fun toString(): String = "($left ${operator.id} $right)"
}
