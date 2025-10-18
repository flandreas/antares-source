package ch.scorpion.jabbah.draw.rasterimg

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.io.WriteFileWrapper
import ch.scorpion.jabbah.draw.drawable.Page
import ch.scorpion.jabbah.draw.drawable.Resolution
import ch.scorpion.jabbah.draw.view.AbstractViewAction
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