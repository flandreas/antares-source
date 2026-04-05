package io.antarescircuit.jabbah.graph.ui.container

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.ui.HelpAction
import io.antarescircuit.jabbah.graph.library.BasicLibraryTreeViewSwing
import java.awt.BorderLayout
import javax.swing.*

class SymbolComparatorViewSwing(
    private val controller: SymbolComparatorController
) : JPanel(), SymbolComparatorView {

    companion object {
        val HELP_ID = HelpId("symbolComparison")
        val helpAction: Action = HelpAction.withSmallImage(HELP_ID)
    }

    private val directionComboBox = JComboBox<Direction>()

    private val libraryTreeView = BasicLibraryTreeViewSwing(controller.libraryTreeViewController)

    init {
        controller.view = this

        directionComboBox.model = DefaultComboBoxModel(Direction.entries.toTypedArray())
        directionComboBox.selectedItem = controller.direction
        directionComboBox.addActionListener {
            controller.direction = directionComboBox.selectedItem as Direction
        }

        buildUI()
    }

    override fun dispose() {}

    private fun buildUI() {
        layout = BorderLayout(0, 8)
        border = BorderFactory.createEmptyBorder(8, 5, 5, 5)
        add(buildHeader(), BorderLayout.NORTH)
        add(buildContent(), BorderLayout.CENTER)
    }

    private fun buildHeader(): JComponent {
        val header = JPanel()
        header.layout = BoxLayout(header, BoxLayout.LINE_AXIS)
        header.add(JLabel(Translations.getString("graph.container.symbolComparison.position")))
        header.add(Box.createHorizontalStrut(5))
        header.add(directionComboBox)
        header.add(Box.createGlue())
        return header
    }

    private fun buildContent(): JComponent {
        val content = JPanel(BorderLayout(0, 8 ))

        val text = JTextArea(Translations.getString("graph.container.symbolComparison.explanation"))
        text.border = null
        text.isEditable = false
        text.lineWrap = true
        text.wrapStyleWord = true
        val descriptionScroll = JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        descriptionScroll.border = null
        content.add(descriptionScroll, BorderLayout.NORTH)

        val treeScrollPane = JScrollPane(libraryTreeView)
        content.add(treeScrollPane, BorderLayout.CENTER)

        return content
    }

    override fun reset() {
        libraryTreeView.selectionModel.clearSelection()
    }

    override fun refresh() {
        libraryTreeView.reload()
    }
}