package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
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