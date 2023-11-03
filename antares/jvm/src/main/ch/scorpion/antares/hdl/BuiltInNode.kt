package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.expression.Expression
import ch.scorpion.antares.hdl.expression.NetExpression

open class BuiltInNode(elementName: String) : AbstractHDLNode(elementName), Iterable<BuiltInNode.InputAssignment> {

	private val _inputAssignments = mutableListOf<InputAssignment>()
	val inputAssignments: Collection<InputAssignment> get() = _inputAssignments

	private val _attributes = mutableMapOf<String, Any>()
	val attributes: Map<String, Any>get() = _attributes

	override fun iterator(): Iterator<InputAssignment> = _inputAssignments.iterator()

	fun createExpressions(): BuiltInNode {
		for (input in inputs) {
			input.net?.let {
				_inputAssignments.add(InputAssignment(input.name, NetExpression(it)))
			}
		}
		return this
	}

	fun setAttribute(name: String, value: Any) {
		_attributes[name] = value
	}

	data class InputAssignment(val name: String, val expression: Expression)
}