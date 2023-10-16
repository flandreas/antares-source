package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.dsl.AbstractNode
import ch.scorpion.jabbah.base.dsl.Compound
import ch.scorpion.jabbah.base.parser.TextLocation
import ch.scorpion.jabbah.base.parser.Token

class TestScript(
	location: TextLocation,
	val portNames: PortNames,
	val testVectors: Compound<TestVectorNode>
) : AbstractNode(location) {

	override fun toString(): String = "TestScript"

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			portNames.accept(visitor)
			for (testVector in testVectors.children) {
				if (!testVector.accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}
}

class PortNames(
	location: TextLocation,
	val names: List<Token<String>>
) : AbstractNode(location) {

	override fun toString(): String = names.map { it.value }.joinToString(",")
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
