package ch.scorpion.antares.model.testcase.result

import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

data class GeneralTestResult(
    val error: Boolean,
    val description: String
)

class SingleTestRunGeneralResultPanelSwing(
    private val results: List<GeneralTestResult>
) : JPanel() {
    private val list = JList(DefaultListModel<GeneralTestResult>().also { model ->
        results.forEach { model.addElement(it) }
    })

    companion object {
        val FAILED_ICON = UiUtil.themedIcon("/img/error-16.png")
        val PASSED_ICON = UiUtil.themedIcon("/img/checkmark.png")
    }

    init {
        list.cellRenderer = Renderer()
        list.visibleRowCount = 5
        buildUI()
    }

    private fun buildUI() {
        border = BorderFactory.createEmptyBorder(5, 10, 0, 5)
        layout = BorderLayout()
        val scroll = JScrollPane(list)
        scroll.preferredSize = Dimension(list.preferredSize.width, 50)
        scroll.minimumSize = Dimension(list.minimumSize.width, 50)
        add(scroll, BorderLayout.CENTER)
    }

    private class Renderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val result = value as GeneralTestResult
            val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
            label.icon = if (result.error) FAILED_ICON else PASSED_ICON
            label.text = result.description
            return label
        }
    }
}