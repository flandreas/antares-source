package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.noise.NoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorChangedEvent
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import java.awt.event.ActionEvent
import javax.swing.Action

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

    override fun actionPerformed(e: ActionEvent?) {
        noiseGeneratorHolder.current = noiseGenerator
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, noiseGeneratorHolder.current == noiseGenerator)
    }
}