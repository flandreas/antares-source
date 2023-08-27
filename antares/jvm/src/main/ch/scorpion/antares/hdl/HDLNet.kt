package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.model.signal.BitWidth

class HDLNet(
	var name: String = "",
	val bitWidth: BitWidth = BitWidth.BW_1
) {

	private val inputs = mutableListOf<HDLPort>()

	private val inOuts = mutableListOf<HDLPort>()

	private var output: HDLPort? = null

	var isInput: Boolean = false
		private set

	var needsVariable: Boolean = true
		private set

	val isInOutNet: Boolean get() = inOuts.isNotEmpty()

	fun addPort(port: HDLPort) {
		when (port.direction) {
			HDLPort.Direction.IN -> inputs.add(port)
			HDLPort.Direction.INOUT -> inOuts.add(port)
			HDLPort.Direction.OUT -> {
				if (output != null) {
					throw HDLException("HDLNet cannot have more than 1 output")
				} else {
					output = port
				}
			}
		}
	}

	fun removePort(port: HDLPort) {
		if (port === output) {
			output = null
		} else {
			inputs.remove(port)
			inOuts.remove(port)
		}
	}

	fun setIsInput(name: String) {
		this.name = name
		needsVariable = false
		isInput = true
	}

	fun setIsOutput(name: String) {
		this.name = name
		needsVariable = false
	}
}