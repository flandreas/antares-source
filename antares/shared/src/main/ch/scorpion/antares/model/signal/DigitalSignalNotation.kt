package ch.scorpion.antares.model.signal

enum class DigitalSignalNotation {

	PREFIX {
		override fun notate(signal: DigitalSignal, representation: DigitalSignalRepresentation): String {
			return "${representation.prefix}${representation.represent(signal)}"
		}
	},

	BASE_SUBSCRIPT {
		override fun notate(signal: DigitalSignal, representation: DigitalSignalRepresentation): String {
			return "${representation.represent(signal)}${createBaseSubscript(representation.base)}"
		}
	},

	SUFFIX {
		override fun notate(signal: DigitalSignal, representation: DigitalSignalRepresentation): String {
			return "${representation.represent(signal)}${representation.suffix}"
		}
	};

	companion object {
		// The code of the Unicode subscript character for the digit 0
		private const val SUBSCRIPT_UNICODE_BASE = 0x2080
	}

	abstract fun notate(signal: DigitalSignal, representation: DigitalSignalRepresentation): String

	protected fun createBaseSubscript(base: Int): String {
		val subscript = StringBuilder()
		var b = base
		do {
			val digit = b.rem(10)
			subscript.append((SUBSCRIPT_UNICODE_BASE + digit).toChar())
			b = b.div(10)
		} while (b > 0)
		return subscript.toString().reversed()
	}
}