package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.jabbah.draw.style.ThemeEvent

import ch.scorpion.jabbah.draw.style.Themes
import javax.swing.Action

/** An [Action] for setting the given [Theme] as the current one of [Themes].*/
class ThemeAction(
	private val theme: Theme,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(name = theme.name, description = null, accelerator = null) {

	private val themeHandler: EventHandler<ThemeEvent> = { updateState() }

	init {
		eventBus.register(ThemeEvent::class, themeHandler)
		updateState()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(themeHandler)
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		Themes.setCurrent(theme.name)
	}

	private fun updateState() {
		selected = theme === Themes.current
	}
}