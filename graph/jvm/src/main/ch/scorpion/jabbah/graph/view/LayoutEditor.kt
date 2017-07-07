package ch.scorpion.jabbah.graph.view

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.graph.view.net.edge.Layout
import javax.swing.JComboBox

class LayoutEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Layout.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Layout>()
    }
}