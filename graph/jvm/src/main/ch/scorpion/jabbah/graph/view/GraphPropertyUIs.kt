package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.net.edge.LayoutType
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.JComboBox

object GraphProperties {

	fun modelId(
		name: String = "modelId",
		baseKey: String = "graph.property.modelId",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Int> {
		return CommandPropertySwing(name, baseKey, Int::class.java, beanProvider)
	}

	fun propagationDelay(beanProperty: BeanProvider = componentBeanProvider): CommandPropertySwing<Long> {
		return CommandPropertySwing("propagationDelay", "element.property.propagationDelay", Long::class.java, beanProperty)
	}

	fun label(
		name: String = "label",
		baseKey: String = "graph.property.label",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<TranslatableText> {
		return CommandPropertySwing(name, baseKey, TranslatableText::class.java, beanProvider)
	}

	fun verticalLabelPosition(
		name: String = "labelPosition",
		baseKey: String = "graph.property.VerticeLabelPosition",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<VerticeLabelPosition> {
		return CommandPropertySwing(name, baseKey, VerticeLabelPosition::class.java, beanProvider)
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
