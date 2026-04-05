package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.module.ExecutionModule
import io.antarescircuit.jabbah.execution.noise.NoiseGenerator
import io.antarescircuit.jabbah.execution.noise.NoiseGeneratorHolder
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