package io.antarescircuit.antares.hdl.vhdl

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.draw.view.AbstractViewAction

class ExportVHDLAction : AbstractViewAction("antares.vhdl.action") {

	override val opensDialog: Boolean get() = true

	override fun calculateEnabled(): Boolean {
		if (!super.calculateEnabled()) {
			return false
		}
		val drawable = view!!.mainContent.drawable
		return drawable is DigitalGraphView
	}

	override fun execute(event: ActionEvent) {
		ExportVHDLPanel.showAsDialog(name, (view!!.mainContent.drawable as DigitalGraphView).graph as DigitalGraph)
	}
}