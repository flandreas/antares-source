package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException

enum class DigitalSignalNotation(
	val customName: String,
	private val translationKey: String
) {

	PREFIX("prefix", "element.signal.notation.prefix") {
		override fun notate(signal: DigitalSignal, representation: DigitalSignalRepresentation): String {
			val r = representation.represent(signal)
			return if (r.length > 1) {
				"${representation.prefix}$r"
			} else {
				r
			}
		}
	},

	BASE_SUBSCRIPT("baseSubscript", "element.signal.notation.baseSubscript") {
		override fun notate(signal: DigitalSignal, representation: DigitalSignalRepresentation): String {
			val r = representation.represent(signal)
			return if (r.length > 1) {
				"$r${createBaseSubscript(representation.base)}"
			} else {
				r
			}
		}
	},

	SUFFIX("suffix", "element.signal.notation.suffix") {
		override fun notate(signal: DigitalSignal, representation: DigitalSignalRepresentation): String {
			val r = representation.represent(signal)
			return if (r.length > 1) {
				"$r${representation.suffix}"
			} else {
				r
			}
		}
	};

	companion object {

		/** The name of the [String] property in [Properties] designating the [DigitalSignalNotation]. */
		const val PROP_DIGITAL_SIGNAL_NOTATION = "ch.scorpion.antares.model.signal.notation"

		// The code of the Unicode subscript character for the digit 0
		private const val SUBSCRIPT_UNICODE_BASE = 0x2080

		fun withName(customName: String): DigitalSignalNotation {
			return values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown DigitalSignalNotation '$customName'")
		}
	}

	abstract fun notate(signal: DigitalSignal, representation: DigitalSignalRepresentation): String

	override fun toString(): String = Translations.getString(translationKey)

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