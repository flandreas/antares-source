package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.antares.model.testcase.parser.PortNameType.*
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.AbstractNode
import ch.scorpion.jabbah.base.dsl.Compound
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.parser.Token

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

class RunNode(
	location: TextLocation,
	children: List<TestVectorNode>
): Compound<TestVectorNode>(location, children) {

	override fun toString(): String = "Run"
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

class ValueNode(
	location: TextLocation,
	val value: Value
) : AbstractNode(location) {

	override fun toString(): String = value.toString()
}
