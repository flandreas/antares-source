package ch.scorpion.jabbah.graph.view

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.net.edge.Layout
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import javax.swing.JComboBox

class PortTypeEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(PortType.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<PortType>()
    }
}

class LayoutEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Layout.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Layout>()
    }
}

class NetViewStyleEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(NetViewStyle.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<NetViewStyle>()
    }
}

class PortLabelPositionEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(PortLabelPosition.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<PortLabelPosition>()
    }
}

class VerticeLabelPositionEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(VerticeLabelPosition.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<VerticeLabelPosition>()
    }
}
