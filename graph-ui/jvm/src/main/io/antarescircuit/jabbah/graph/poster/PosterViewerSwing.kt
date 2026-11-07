package io.antarescircuit.jabbah.graph.poster

import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.draw.CloseViewRequest
import io.antarescircuit.jabbah.draw.drawable.PageOrientation
import io.antarescircuit.jabbah.draw.drawable.PageSize
import io.antarescircuit.jabbah.draw.drawable.Resolution
import io.antarescircuit.jabbah.draw.rasterimg.ExportRasterImageAction
import io.antarescircuit.jabbah.draw.view.CanvasJvm
import io.antarescircuit.jabbah.draw.view.FocusPanel
import io.antarescircuit.jabbah.graph.library.Library
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class PosterViewerSwing(
    applicationName: String,
    library: Library,
    private val eventBus: EventBus = BaseModule.eventBus
) : JFrame(), PosterViewerView {

    companion object {
        private const val FIELD_DIST = 20
    }

    private val controller = PosterViewerController(library)

    private val menuBar = PosterViewerMenuBar(controller)

    private val canvas = CanvasJvm(controller.drawingView)

    private val pageSizeComboBox = JComboBox<PageSize>()

    private val pageOrientationComboBox = JComboBox<PageOrientation>()

    private val resolutionComboBox = JComboBox<Resolution>()

    private val borderCheckBox = JCheckBox(Translations.getString("graph.poster.borders.name"))

    private val closeRequestHandler: (CloseViewRequest) -> Unit = { handle(it) }

    val exportImageAction = ExportRasterImageAction(controller.page, controller.resolution, this)

    init {
        controller.view = this

        eventBus.register(CloseViewRequest::class, closeRequestHandler)

        PageSize.PREDEFINED.forEach { pageSizeComboBox.addItem(it) }
        pageSizeComboBox.selectedItem = controller.page.size
        pageSizeComboBox.addActionListener { controller.page = controller.page.copy(size = pageSizeComboBox.selectedItem as PageSize) }

        PageOrientation.entries.forEach { pageOrientationComboBox.addItem(it) }
        pageOrientationComboBox.selectedItem = controller.page.orientation
        pageOrientationComboBox.addActionListener { controller.page = controller.page.copy(orientation = pageOrientationComboBox.selectedItem as PageOrientation) }

        Resolution.PREDEFINED.forEach { resolutionComboBox.addItem(it) }
        resolutionComboBox.selectedItem = controller.resolution
        resolutionComboBox.addActionListener { controller.resolution = resolutionComboBox.selectedItem as Resolution }

        borderCheckBox.isSelected = controller.drawElementBorder
        borderCheckBox.addActionListener { controller.drawElementBorder = borderCheckBox.isSelected }

        buildUI()

        title = "$applicationName - ${Translations.getString("graph.action.poster.title", library.name.value)}"
        jMenuBar = menuBar

        size = Dimension(1024, 768)
        setLocationRelativeTo(getFrames()[0])

        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                controller.dispose()
            }
        })

        isVisible = true

        SwingUtilities.invokeLater {
            canvas.requestFocusInWindow()
        }
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(closeRequestHandler)
        menuBar.dispose()
    }

    private fun buildUI() {
        (contentPane as JComponent).border = UIBasics.createDialogBorder()
        contentPane.layout = BorderLayout(0, 5)

        contentPane.add(createToolbar(), BorderLayout.NORTH)
        contentPane.add(FocusPanel(canvas, controller.drawingView), BorderLayout.CENTER)
    }

    override fun notifyPropertiesChanged() {
        exportImageAction.page = controller.page
        exportImageAction.resolution = controller.resolution
    }

    private fun createToolbar(): JPanel {
        val toolbar = JPanel()
        toolbar.layout = BoxLayout(toolbar, BoxLayout.LINE_AXIS)

        pageSizeComboBox.maximumSize = pageSizeComboBox.preferredSize
        toolbar.add(JLabel("${Translations.getString("draw.pageSize.label")}:"))
        toolbar.add(Box.createHorizontalStrut(UIBasics.LABEL_GAP))
        toolbar.add(pageSizeComboBox)

        toolbar.add(Box.createHorizontalStrut(FIELD_DIST))
        pageOrientationComboBox.maximumSize = pageOrientationComboBox.preferredSize
        toolbar.add(JLabel("${Translations.getString("draw.pageOrientation.label")}:"))
        toolbar.add(Box.createHorizontalStrut(UIBasics.LABEL_GAP))
        toolbar.add(pageOrientationComboBox)

        toolbar.add(Box.createHorizontalStrut(FIELD_DIST))
        resolutionComboBox.maximumSize = resolutionComboBox.preferredSize
        toolbar.add(JLabel("${Translations.getString("draw.resolution.label")}:"))
        toolbar.add(Box.createHorizontalStrut(UIBasics.LABEL_GAP))
        toolbar.add(resolutionComboBox)

        toolbar.add(Box.createHorizontalStrut(FIELD_DIST))
        borderCheckBox.maximumSize = borderCheckBox.preferredSize
        toolbar.add(borderCheckBox)

        toolbar.add(Box.createHorizontalGlue())
        toolbar.add(JButton(ActionWrapperSwing(exportImageAction)))

        return toolbar
    }

    private fun handle(event: CloseViewRequest) {
        if (event.view === controller.drawingView) {
            super.dispose()
            controller.dispose()
        }
    }
}