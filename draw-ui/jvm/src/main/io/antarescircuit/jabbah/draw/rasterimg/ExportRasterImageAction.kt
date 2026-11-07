package io.antarescircuit.jabbah.draw.rasterimg

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.io.WriteFileWrapper
import io.antarescircuit.jabbah.draw.drawable.Page
import io.antarescircuit.jabbah.draw.drawable.Resolution
import io.antarescircuit.jabbah.draw.view.AbstractViewAction
import java.awt.Frame

class ExportRasterImageAction(
    var page: Page? = null,
    var resolution: Resolution? = null,
    private val parent: Frame = Frame.getFrames()[0],
) : AbstractViewAction("draw.action.exportImage") {

    override fun execute(event: ActionEvent) {
        view?.let {
            WriteFileWrapper.wrap(name) {
                ExportRasterImagePanel.showAsDialog(it.mainContent, page, resolution, parent)
            }
        }
    }
}