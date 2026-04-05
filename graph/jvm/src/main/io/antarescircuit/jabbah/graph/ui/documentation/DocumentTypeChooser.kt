package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.model.DocumentType
import java.awt.FlowLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class DocumentTypeChooser : JPanel() {

    private val comboBox = JComboBox<DocumentType>()

    init {
        DocumentType.entries.forEach { comboBox.addItem(it) }
        comboBox.isEnabled = false
        buildUI()
    }

    private fun buildUI() {
        layout = FlowLayout(FlowLayout.LEFT)
        add(JLabel("${Translations.getString("element.property.documentType.name")}:"))
        add(comboBox)

        maximumSize = preferredSize
    }
}