package ch.scorpion.antares.hdl.verilog

import ch.scorpion.antares.hdl.ExportHDLPanel
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.view.AbstractViewAction

class ExportVerilogAction : AbstractViewAction("antares.verilog.action") {

    override fun calculateEnabled(): Boolean {
        if (!super.calculateEnabled()) {
            return false
        }
        val drawable = view!!.mainContent.drawable
        return drawable is DigitalGraphView
    }

    override fun execute(event: ActionEvent) {
        ExportHDLPanel.showAsDialog(
            ExportHDLPanel.Companion.Language.Verilog,
            (view!!.mainContent.drawable as DigitalGraphView).graph as DigitalGraph
        )
    }
}