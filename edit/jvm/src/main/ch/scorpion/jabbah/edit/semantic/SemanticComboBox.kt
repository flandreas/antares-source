package ch.scorpion.jabbah.edit.semantic

import java.awt.Component
import javax.swing.*

fun createSemanticComboBox(
    noneText: String
): JComboBox<Semantic> {
    return JComboBox<Semantic>().apply {
        renderer = SemanticRenderer(noneText)
        addItem(null)
        SemanticRegistry.semantics.forEach { addItem(it) }
    }
}

private class SemanticRenderer(
    private val noneText: String
) : DefaultListCellRenderer() {

    override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
        label.text = if (value == null) {
            noneText
        } else {
            (value as Semantic).translatedName
        }
        return label
    }
}