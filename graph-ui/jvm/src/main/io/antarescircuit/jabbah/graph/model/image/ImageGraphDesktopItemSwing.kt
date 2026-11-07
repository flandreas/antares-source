package io.antarescircuit.jabbah.graph.model.image

import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.app.ToolBar
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.view.AbstractZoomPanAction
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JPanel
import javax.swing.JScrollPane
import kotlin.math.ceil
import kotlin.math.max

class ImageGraphDesktopItemSwing(
    val element: ImageLibraryElement,
    private val applicationDataHolder: ApplicationDataHolder,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractTitledGraphDesktopViewItemSwing(
    createTitleText(element.storable),
    JPanel(),
    applicationDataHolder,
    eventBus
) {
    companion object {
        fun createTitleText(storable: ImageIdentification): String =
            "${Translations.getString("edit.component.image")} \"${storable.name.getTranslation()}\""
    }

    private val imageIdentification: ImageIdentification get() = applicationDataHolder.data!!.content as ImageIdentification

    override fun createHeaderText(): String = createTitleText(imageIdentification)

    override fun displays(content: Any?): Boolean =
        applicationDataHolder.data?.content is ImageIdentification && content === imageIdentification

    private val imageData = EditModule.imageRepository.getImage(imageIdentification.uuid)

    private val imagePanel = ImagePanel()

    private var zoomFactor = 1.0

    private val zoomInAction = ZoomInAction()
    private val zoomNormal = ZoomNormalAction()
    private val zoomOutAction = ZoomOutAction()

    init {
        buildUI()
    }

    private fun buildUI() {
        contentPanel.layout = BorderLayout()
        val scrollPane = JScrollPane(imagePanel)
        scrollPane.verticalScrollBar.blockIncrement = 100
        scrollPane.verticalScrollBar.unitIncrement = 10
        scrollPane.horizontalScrollBar.blockIncrement = 100
        scrollPane.horizontalScrollBar.unitIncrement = 10
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        val toolBar = ToolBar()
        toolBar.addAction(zoomInAction)
        toolBar.addAction(zoomNormal)
        toolBar.addAction(zoomOutAction)
        contentPanel.add(toolBar, BorderLayout.NORTH)
    }

    private fun tryApplyNewZoomFactor(newZoomFactor: Double) {
        if (newZoomFactor >= BaseModule.properties.getFloat(View.PROP_MIN_ZOOM_FACTOR) && newZoomFactor <= BaseModule.properties.getFloat(View.PROP_MAX_ZOOM_FACTOR)) {
            zoomFactor = newZoomFactor
            imagePanel.invalidate()
            imagePanel.revalidate()
            imagePanel.repaint()
        }
    }

    private inner class ZoomInAction : AbstractAction("view.action.zoomIn", "/img/plus-18.png") {
        override fun execute(event: ActionEvent) {
            tryApplyNewZoomFactor(zoomFactor * BaseModule.properties.getFloat(AbstractZoomPanAction.PROP_ZOOM_STEP))
        }
    }

    private inner class ZoomNormalAction : AbstractAction(name = "100%", description = Translations.getString("view.action.zoomNormal.name"), accelerator = null) {
        override fun execute(event: ActionEvent) {
            tryApplyNewZoomFactor(1.0)
        }
    }

    private inner class ZoomOutAction : AbstractAction("view.action.zoomOut", "/img/minus-18.png") {
        override fun execute(event: ActionEvent) {
            tryApplyNewZoomFactor(zoomFactor  / BaseModule.properties.getFloat(AbstractZoomPanAction.PROP_ZOOM_STEP))
        }
    }

    private inner class ImagePanel : JPanel() {

        init {
            layout = BorderLayout()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as java.awt.Graphics2D
            val x = max(0.0, (width - imageData!!.image.width * zoomFactor) / 2)
            val y = max(0.0, (height - imageData.image.height * zoomFactor) / 2)

            g2.translate(x, y)
            g2.scale(zoomFactor, zoomFactor)
            Graphics2DJvm.drawImage(g2, imageData.image, 0, 0)
            g2.scale(1 / zoomFactor, 1 / zoomFactor)
            g2.translate(-x, -y)
        }

        override fun getPreferredSize(): Dimension =
            Dimension(
                ceil(imageData!!.image.width * zoomFactor).toInt(),
                ceil(imageData.image.height * zoomFactor).toInt()
            )
    }
}