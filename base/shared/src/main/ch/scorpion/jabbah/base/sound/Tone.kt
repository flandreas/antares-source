package ch.scorpion.jabbah.base.sound

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations

enum class WaveformType(
	override val customName: String
) : EnumProperty<WaveformType> {
	Sine("sine"),
	Square("square"),
	Triangle("triangle"),
	Sawtooth("sawtooth"),
	Noise("noise");

	companion object {
		const val BASE_KEY = "element.property.waveformType"

		fun withName(customName: String): WaveformType =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown WaveformType $customName")
	}

	override fun toString(): String = Translations.getString("$BASE_KEY.$customName")
}

data class ToneParams(
	val frequency: Int,
	val volume: Double,
	val waveformType: WaveformType = WaveformType.Sine,
	val leftChannel: Boolean = true,
	val rightChannel: Boolean = true,
	val smoothLevel: Int = 2,
	val smoothWidth: Int = 2
)

interface Tone {
	fun play()
	fun update(params: ToneParams)
	fun stop()
}

expect object ToneFactory {
	fun create(params: ToneParams): Tone
}

