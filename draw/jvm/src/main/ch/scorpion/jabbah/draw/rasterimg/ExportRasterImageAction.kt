package ch.scorpion.jabbah.draw.rasterimg

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.view.AbstractViewAction

class ExportRasterImageAction : AbstractViewAction("draw.action.exportImage") {
    override fun execute(event: ActionEvent) {
        view?.let {
            ExportRasterImagePanel.showAsDialog(it.mainContent)
        }
    }
}