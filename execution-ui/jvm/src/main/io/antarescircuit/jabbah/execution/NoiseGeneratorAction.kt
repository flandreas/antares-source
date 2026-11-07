package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.module.ExecutionModule
import io.antarescircuit.jabbah.execution.noise.NoiseGenerator
import io.antarescircuit.jabbah.execution.noise.NoiseGeneratorChangedEvent
import io.antarescircuit.jabbah.execution.noise.NoiseGeneratorHolder

/** An [Action] for setting the current [NoiseGenerator] in [NoiseGeneratorHolder].*/
class NoiseGeneratorAction(
	private val noiseGenerator: NoiseGenerator,
	private val noiseGeneratorHolder: NoiseGeneratorHolder = ExecutionModule.noiseGeneratorHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(noiseGenerator.nameKey) {

	private val noiseGeneratorHandler: EventHandler<NoiseGeneratorChangedEvent> = { updateState() }

	init {
		eventBus.register(NoiseGeneratorChangedEvent::class, noiseGeneratorHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(noiseGeneratorHandler)
	}

	override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
		noiseGeneratorHolder.current = noiseGenerator
	}

	private fun updateState() {
		selected = noiseGeneratorHolder.current == noiseGenerator
	}
}