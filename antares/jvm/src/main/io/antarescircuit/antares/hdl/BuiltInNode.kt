package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.hdl.expression.Expression
import io.antarescircuit.antares.hdl.expression.NetExpression

/**
 * Represents a build-in Antares component that is not specifically treated like logic gates,
 * constants, power or ground.
 *
 * The HDL behaviour of such components are encapsulated in HDL template files identified
 * by element name, which is expected to be the simple class name of the component.
 *
 * @property translatedName the translated name of the Antares component to be displayed
 * in error messages to the user, such as "Switch".
 */
open class BuiltInNode(
	elementName: String,
	val translatedName: String
) : AbstractHDLNode(elementName), Iterable<BuiltInNode.InputAssignment> {

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