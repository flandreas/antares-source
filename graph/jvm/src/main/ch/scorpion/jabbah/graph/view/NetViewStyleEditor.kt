package ch.scorpion.jabbah.graph.view

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import javax.swing.JComboBox

class NetViewStyleEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(NetViewStyle.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<NetViewStyle>()
    }
}