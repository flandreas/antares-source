package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.net.edge.LayoutType
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.JComboBox

object GraphProperties {

	fun propagationDelay(beanProperty: BeanProvider = componentBeanProvider): PropertyImpl<Long> {
		return PropertyImpl("propagationDelay", "element.property.propagationDelay", Long::class.java, beanProperty)
	}

	fun label(
		name: String = "label",
		baseKey: String = "graph.property.label",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<String> {
		return PropertyImpl(name, baseKey, String::class.java, beanProvider)
	}

	fun verticalLabelPosition(
		name: String = "labelPosition",
		baseKey: String = "graph.property.VerticeLabelPosition",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<VerticeLabelPosition> {
		return PropertyImpl(name, baseKey, VerticeLabelPosition::class.java, beanProvider)
	}
}

class PortTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PortType.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<PortType>()
	}
}

class LayoutEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(LayoutType.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<LayoutType>()
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
