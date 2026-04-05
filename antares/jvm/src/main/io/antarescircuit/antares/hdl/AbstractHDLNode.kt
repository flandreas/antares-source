package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.hdl.HDLPort.Direction.*

abstract class AbstractHDLNode(
	var elementName: String
) {
	private val ports = mutableListOf<HDLPort>()

	var hdlEntityName: String = elementName

	fun addPort(port: HDLPort) {
		ports.add(port)
	}

	val inputs: List<HDLPort> get() = ports.filter { it.direction == IN }

	val outputs: List<HDLPort> get() = ports.filter { it.direction == OUT }

	val inOuts: List<HDLPort> get() = ports.filter { it.direction == INOUT }

	open fun rename(renaming: HDLRenaming) {
		ports.forEach { it.rename(renaming) }
	}
}