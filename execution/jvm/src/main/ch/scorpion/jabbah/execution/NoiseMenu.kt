package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.noise.NoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import javax.swing.ButtonGroup
import javax.swing.JMenu
import javax.swing.JRadioButtonMenuItem

/** A [JMenu] containing [Action]s for all available [NoiseGenerator]s.*/
class NoiseMenu(
        noiseGenerators: List<NoiseGenerator>,
        holder: NoiseGeneratorHolder = ExecutionModule.noiseGeneratorHolder,
        eventBus: EventBus = BaseModule.eventBus
) : JMenu(Translations.getString("execution.menu.noise")) {

    init {
        val buttonGroup = ButtonGroup()
        noiseGenerators.forEach { add(it, buttonGroup, holder, eventBus) }
    }

    private fun add(noiseGenerator: NoiseGenerator, buttonGroup: ButtonGroup, holder: NoiseGeneratorHolder, eventBus: EventBus) {
        val menuItem = JRadioButtonMenuItem(ActionWrapperSwing(NoiseGeneratorAction(noiseGenerator, holder, eventBus)))
        buttonGroup.add(menuItem)
        add(menuItem)
    }
}