package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.expression.Expression
import ch.scorpion.antares.hdl.expression.NetExpression

open class BuiltInNode(elementName: String) : AbstractHDLNode(elementName), Iterable<BuiltInNode.InputAssignment> {

	private val _inputAssignments = mutableListOf<InputAssignment>()
	val inputAssignments: Collection<InputAssignment> get() = _inputAssignments

	fun createExpressions(): BuiltInNode {
		for (input in inputs) {
			input.net?.let {
				_inputAssignments.add(InputAssignment(input.name, NetExpression(it)))
			}
		}
		return this
	}

	override fun iterator(): Iterator<InputAssignment> = _inputAssignments.iterator()

	data class InputAssignment(val name: String, val expression: Expression)
}