package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.expression.Expression
import ch.scorpion.antares.hdl.expression.NetExpression

class BuiltInNode(elementName: String) : AbstractHDLNode(elementName), Iterable<BuiltInNode.InputAssignment> {

	private val inputAssignments = mutableListOf<InputAssignment>()

	fun createExpressions(): BuiltInNode {
		for (input in inputs) {
			input.net?.let {
				inputAssignments.add(InputAssignment(input.name, NetExpression(it)))
			}
		}
		return this
	}

	override fun iterator(): Iterator<InputAssignment> = inputAssignments.iterator()

	data class InputAssignment(val name: String, val expression: Expression)
}