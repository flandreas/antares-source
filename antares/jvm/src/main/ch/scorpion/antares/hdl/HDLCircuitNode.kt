package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.expression.Expression
import ch.scorpion.antares.hdl.expression.NetExpression

class HDLCircuitNode(
	val circuit: HDLCircuit
) : AbstractHDLNode(circuit.uuid.id) {

	private val _inputAssignments = mutableListOf<InputAssignment>()
	val inputAssignments: Collection<InputAssignment> get() = _inputAssignments

	fun createExpressions(): HDLCircuitNode {
		for (input in inputs) {
			input.net?.let {
				_inputAssignments.add(InputAssignment(input.name, NetExpression(it)))
			}
		}
		return this
	}

	data class InputAssignment(
		val name: String,
		val expression: Expression
	)
}