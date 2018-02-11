package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.noise.NoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorChangedEvent
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder

/** An [Action] for setting the current [NoiseGenerator] in [NoiseGeneratorHolder].*/
class NoiseGeneratorAction(
        private val noiseGenerator: NoiseGenerator,
        private val noiseGeneratorHolder: NoiseGeneratorHolder = ExecutionModule.noiseGeneratorHolder,
        eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(noiseGenerator.nameKey) {

    init {
        eventBus.register(NoiseGeneratorChangedEvent::class, { updateState() })
        updateState()
    }

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
        noiseGeneratorHolder.current = noiseGenerator
    }

    private fun updateState() {
        selected = noiseGeneratorHolder.current == noiseGenerator
    }
}