package io.antarescircuit.jabbah.graph.view.scenario

import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import javax.swing.ButtonGroup
import javax.swing.JMenu
import javax.swing.JRadioButtonMenuItem

class ScenarioModeMenu : JMenu(Translations.getString("scenario.mode.name")) {

    init {
        val buttonGroup = ButtonGroup()
        ScenarioMode.entries.forEach { scenarioMode ->
            val menuItem = JRadioButtonMenuItem(ActionWrapperSwing(ScenarioModeAction(scenarioMode)))
            buttonGroup.add(menuItem)
            add(menuItem)
        }
    }
}