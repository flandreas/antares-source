package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.jabbah.draw.style.ThemeEvent
import java.awt.event.ActionEvent

import ch.scorpion.jabbah.draw.style.Themes
import javax.swing.Action

/** An [Action] for setting the given [Theme] as the current one of [Themes].*/
class ThemeAction(
        private val theme: Theme,
        eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(name = theme.name) {

    init {
        eventBus.register(ThemeEvent::class, { updateState() })
        updateState()
    }

    override fun actionPerformed(e: ActionEvent?) {
        Themes.setCurrent(theme.name)
    }

    private fun updateState() {
        putValue(Action.SELECTED_KEY, theme === Themes.current)
    }
}