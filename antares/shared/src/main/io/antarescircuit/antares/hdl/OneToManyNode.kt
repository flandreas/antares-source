package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.signal.BitWidth

class OneToManyNode(
	node: AbstractHDLNode,
	val bitWidth: BitWidth,
	val branchCount: BranchCount
) : AbstractHDLNode(node.elementName) {

	val sourceSignal: String? get() = inputs[0].net?.name

	init {
		node.inputs.forEach { addPort(it) }
		node.outputs.forEach { addPort(it) }
	}
}