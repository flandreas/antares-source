package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.expression.Expression

class HDLNodeAssignment(
	elementName: String = ""
) : AbstractHDLNode(elementName) {

	lateinit var expression: Expression

	val targetNet: HDLNet? get() = outputs.first().net
}