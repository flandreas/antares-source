package io.antarescircuit.antares.dsl

import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.dsl.GraphViewExternalFunctions

class AntaresGraphViewExternalFunctions : GraphViewExternalFunctions() {

	fun getButton(buttonId: Int): SwitchView? =
		getComponent(buttonId, SwitchView::class, Translations.getString("library.element.Switch.name"))

	fun getLED(ledId: Int): LEDView? =
		getComponent(ledId, LEDView::class, Translations.getString("library.element.LED.name"))
}