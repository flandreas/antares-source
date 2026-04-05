package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.hdl.expression.Expression
import io.antarescircuit.antares.hdl.expression.NetExpression
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.signal.BitWidth

class ManyToOneNode(
	node: AbstractHDLNode,
	bitWidth: BitWidth,
	branchCount: BranchCount
) : AbstractHDLNode(node.elementName), Iterable<ManyToOneNode.Assignment> {

	private val assignments = mutableListOf<Assignment>()

	val targetSignal: String? get() = outputs[0].net?.name

	init {
		val narrowSideBitWidth = bitWidth.width / branchCount.count
		for ((index, lsb) in (0 until bitWidth.width step narrowSideBitWidth).withIndex()) {
			val net: HDLNet = node.inputs[index].net ?: throw HDLException("Concentrator with unconnected input")
			assignments.add(Assignment(lsb + narrowSideBitWidth - 1, lsb, NetExpression(net)))
		}
		node.inputs.forEach { addPort(it) }
		node.outputs.forEach { addPort(it) }
	}

	override fun iterator(): Iterator<Assignment> = assignments.iterator()

	data class Assignment(
		val msb: Int,
		val lsb: Int,
		val expression: Expression
	)
}