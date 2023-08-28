package ch.scorpion.antares.hdl

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.signal.BitWidth

class HDLPort(
	val name: String,
	direction: Direction,
	net: HDLNet? = null,
	val bitWidth: BitWidth = BitWidth.BW_1,
	val logic: Logic = Logic.POSITIVE
) {

	enum class Direction {
		IN,
		OUT,
		INOUT
	}

	var direction: Direction = direction
		private set

	var net: HDLNet? = net
		set(value) {
			if (field != null) {
				field!!.removePort(this)
			}
			field = value
			value?.addPort(this)
		}

	init {
		net?.addPort(this)
	}

	fun setInOut() {
		direction = Direction.INOUT
	}
}