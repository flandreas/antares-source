package io.antarescircuit.jabbah.execution.noise

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.random.Random

interface NoiseGenerator {

	/**
	 * Returns a base resource key of the displayable name of this [NoiseGenerator].
	 * @return a translated, displayable name of this [NoiseGenerator].
	 */
	val nameKey: String

	/**
	 * Creates a noise value.
	 * @param bound the upper bound of the noise value (exclusive).
	 * *
	 * @return the created noise value.
	 */
	fun noise(bound: Int): Int
}

abstract class AbstractNoiseGenerator(override val nameKey: String) : NoiseGenerator

class NoNoiseGenerator : AbstractNoiseGenerator("execution.noiseGenerator.none") {

	override fun noise(bound: Int): Int {
		return 0
	}
}

class RandomNoiseGenerator : AbstractNoiseGenerator("execution.noiseGenerator.random") {

	override fun noise(bound: Int): Int {
		return Random.nextInt(0, bound)
	}
}

class NoiseGeneratorChangedEvent(val current: NoiseGenerator)

class NoiseGeneratorHolder(
	current: NoiseGenerator,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	var current: NoiseGenerator = current
		set(value) {
			field = value
			eventBus.post(NoiseGeneratorChangedEvent(field))
		}
}
