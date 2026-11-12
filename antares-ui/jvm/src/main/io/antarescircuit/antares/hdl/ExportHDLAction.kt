package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.draw.view.AbstractViewAction

/** Exports the circuit of the active view to the specified [language]. */
class ExportHDLAction(private val language: HDLLanguage) : AbstractViewAction("${language.keyPrefix}.action") {

	override val opensDialog: Boolean get() = true

	override fun calculateEnabled(): Boolean {
		if (!super.calculateEnabled()) {
			return false
		}
		val drawable = view!!.mainContent.drawable
		return drawable is DigitalGraphView
	}

	override fun execute(event: ActionEvent) {
		ExportHDLPanel.showAsDialog(name, language, (view!!.mainContent.drawable as DigitalGraphView).graph as DigitalGraph)
	}
}
