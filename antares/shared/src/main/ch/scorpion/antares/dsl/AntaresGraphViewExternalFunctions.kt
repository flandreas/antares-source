package ch.scorpion.antares.dsl

import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.dsl.GraphViewExternalFunctions

class AntaresGraphViewExternalFunctions : GraphViewExternalFunctions() {

	fun getButton(buttonId: Int): SwitchView? =
		getComponent(buttonId, SwitchView::class, Translations.getString("library.element.Switch.name"))
}