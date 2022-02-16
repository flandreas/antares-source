package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.input.PeriodOrFrequencyUnit.Nanosecond
import ch.scorpion.jabbah.base.Translations

enum class PeriodOrFrequencyUnit(
	val customName: String,
	val factor: Long,
	val inverse: Boolean = false,
	val denotation: String = customName
) {
	Second("s",1_000_000_000),
	Millisecond("ms", 1_000_000),
	Microsecond("us", 1_000, denotation = "µs"),
	Nanosecond("ns", 1),
	Hertz("Hz", 1_000_000_000, inverse = true),
	KiloHertz("kHz", 1_000_000, inverse = true),
	MegaHertz("MHz", 1_000, inverse = true),
	GigaHertz("GHz", 1, inverse = true);

	companion object {
		fun withName(customName: String): PeriodOrFrequencyUnit =
			values().firstOrNull { it.customName == customName }
			?: throw IllegalArgumentException("unknown PeriodOrFrequencyUnit '$customName'")
	}
}

data class PeriodOrFrequency(val value: Long, val unit: PeriodOrFrequencyUnit) {

	companion object {
		fun fromNanoseconds(value: Long, unit: PeriodOrFrequencyUnit): PeriodOrFrequency =
			if (unit.inverse) {
				PeriodOrFrequency(unit.factor / value, unit)
			} else {
				PeriodOrFrequency(value / unit.factor, unit)
			}
	}

	init {
		require(value > 0)
	}

	override fun toString(): String = "$value ${unit.denotation}"

	val asNanoseconds: PeriodOrFrequency get() = if (unit.inverse) {
		if (value == 0L) {
			PeriodOrFrequency(Long.MAX_VALUE, Nanosecond)
		} else {
			PeriodOrFrequency(unit.factor / value, Nanosecond)
		}
	} else {
		PeriodOrFrequency(value * unit.factor, Nanosecond)
	}
}

object PeriodOrFrequencyParser {

	fun parse(text: String): PeriodOrFrequency {
		val s = text.trim().lowercase()
		try {
			val value = s.toLong()
			if (value <= 0) {
				throwInvalidValueException(text)
			}
			return PeriodOrFrequency(value, Nanosecond)
		} catch (e: NumberFormatException) {
			// continue
		}

		return PeriodOrFrequency(extractValue(s), extractUnit(s))
	}

	private fun extractUnit(text: String): PeriodOrFrequencyUnit {
		val unitText = text.dropWhile { it.isDigit() }.trim()
		return PeriodOrFrequencyUnit.values().firstOrNull {
			unitText == it.denotation.lowercase() || unitText == it.customName.lowercase()
		} ?: throw IllegalArgumentException(Translations.getString("element.property.periodOrFrequency.invalidUnit.text", unitText))
	}

	private fun extractValue(text: String): Long {
		val valueText = text.dropLastWhile { !it.isDigit() }.trim()
		try {
			val value = valueText.toLong()
			if (value <= 0) {
				throwInvalidValueException(valueText)
			}
			return value
		} catch (e: NumberFormatException) {
			throwInvalidValueException(valueText)
		}
	}

	private fun throwInvalidValueException(valueText: String): Nothing =
		throw IllegalArgumentException(Translations.getString("element.property.periodOrFrequency.invalidValue.text", valueText))
}