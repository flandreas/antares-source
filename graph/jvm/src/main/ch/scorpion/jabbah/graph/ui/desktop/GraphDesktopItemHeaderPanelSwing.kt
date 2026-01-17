package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.UIBasics
import java.awt.Color
import java.awt.Dimension
import java.awt.Image
import java.awt.Point
import java.awt.datatransfer.Transferable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.TransferHandler
import javax.swing.UIManager
import kotlin.collections.forEach
import kotlin.math.max

class GraphDesktopItemHeaderPanelSwing(
    private val graphDesktopViewItem: GraphDesktopViewItem,
    private val title: JComponent,
    private val titleIconTextProvider: () -> String,
    private val eventBus: EventBus = BaseModule.eventBus,
    allowClose: Boolean = true,
    actions: List<Action> = emptyList()
) : JPanel() {

    companion object {
        private val LOG by logger(GraphDesktopItemHeaderPanelSwing::class)

        /** The minimum distance (in view coordinate space) to drag the mouse before D&D is initiated. */
        private const val DND_MIN_DRAG_DIST = 6

        const val PREF_HEIGHT = 27
        const val LEFT_INSET = 10

        val headerBackgroundColor: Color get() = UiUtil.getBackgroundDivertColor(UIManager.getColor("Panel.background"))
    }


    init {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        add(Box.createHorizontalStrut(LEFT_INSET))

        add(title)
        background = headerBackgroundColor

        add(Box.createHorizontalGlue())
        actions.forEach {
            add(UiUtil.createToolBarButton(it))
            add(Box.createHorizontalStrut(5))
        }
        if (allowClose) {
            add(UiUtil.createToolBarButton(CloseAction()))
        }

        if (BaseModule.properties.getBoolean(GraphDesktopView.PROP_DOCKING)) {
            val dndMouseAdapter = DndMouseAdapter()
            transferHandler = DesktopItemTransferHandler
            addMouseListener(dndMouseAdapter)
            addMouseMotionListener(dndMouseAdapter)

            // TODO How does this scale with large NavigationStackViews?
            title.addMouseListener(dndMouseAdapter)
            title.addMouseMotionListener(dndMouseAdapter)
        }
    }

    override fun getPreferredSize(): Dimension =
        Dimension(super.getPreferredSize().width, max(PREF_HEIGHT, title.preferredSize.height))

    private fun createDnDImage(): Image {
        val text = titleIconTextProvider.invoke()
        val label: JLabel = title as? JLabel ?: UIBasics.createHeaderLabel(text)
        val fontMetrics = label.getFontMetrics(label.font)

        val image = BufferedImage(fontMetrics.stringWidth(text) + 2 * 10, height, BufferedImage.TYPE_INT_RGB)
        val g2 = image.createGraphics()

        g2.color = headerBackgroundColor
        g2.fillRect(0, 0, image.width, image.height)
        val dy = (image.height - fontMetrics.height) / 2
        g2.font = label.font
        g2.color = label.foreground
        g2.drawString(titleIconTextProvider.invoke(), 10, dy + fontMetrics.ascent)
        g2.dispose()
        return image
    }

    private inner class CloseAction : AbstractAction("base.action.close") {

        init {
            imagePath = "/img/close-16.png"
        }

        override fun execute(event: ActionEvent) {
            eventBus.post(graphDesktopViewItem.createCloseRequest())
        }
    }

    /**
     * Handles DnD. Only used with docking feature.
     */
    private object DesktopItemTransferHandler : TransferHandler() {

        override fun getSourceActions(c: JComponent): Int = MOVE

        override fun canImport(support: TransferSupport?): Boolean = false

        override fun createTransferable(c: JComponent?): Transferable? {
            if (c is GraphDesktopItemHeaderPanelSwing) {
                LOG.trace("createTransferable")
                dragImage = c.createDnDImage()
                dragImageOffset = Point(0, 0)
                BaseModule.eventBus.post(DockingStartedEvent(c.graphDesktopViewItem))
                return GraphDesktopViewItemTransferable(c.graphDesktopViewItem)
            }
            return null
        }

        override fun exportDone(source: JComponent?, data: Transferable?, action: Int) {
            super.exportDone(source, data, action)
            LOG.trace("exportDone")
            if (source is GraphDesktopItemHeaderPanelSwing) {
                BaseModule.eventBus.post(DockingFinishedEvent(source.graphDesktopViewItem))
            }
        }
    }

    /**
     * Listens for mouse events on the header and initiates DnD after the user has dragged
     * the mouse the configured minimum distance. Only used with docking feature.
     */
    private inner class DndMouseAdapter : MouseAdapter() {

        private var startPoint: Point? = null

        override fun mousePressed(e: MouseEvent) {
            if (e.button == MouseEvent.BUTTON1) {
                startPoint = e.point
            }
        }

        override fun mouseReleased(e: MouseEvent?) {
            startPoint = null
        }

        override fun mouseDragged(e: MouseEvent) {
            if (startPoint != null) {
                if (startPoint!!.distance(e.point) > DND_MIN_DRAG_DIST) {
                    startPoint = null
                    transferHandler.exportAsDrag(
                        this@GraphDesktopItemHeaderPanelSwing,
                        e,
                        TransferHandler.MOVE
                    )
                }
            }
        }
    }
}