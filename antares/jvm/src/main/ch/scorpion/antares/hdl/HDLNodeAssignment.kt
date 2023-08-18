package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.expression.Expression

class HDLNodeAssignment(
	elementName: String,
	val expression: Expression
) : AbstractHDLNode(elementName)