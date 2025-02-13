package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.view.AbstractViewAction

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