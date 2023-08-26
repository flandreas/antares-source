package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.HDLPort.Direction.IN
import ch.scorpion.antares.hdl.HDLPort.Direction.OUT

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
}