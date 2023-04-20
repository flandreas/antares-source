package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.drawingBeanProvider
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.container.InternalLabelOrientation
import ch.scorpion.jabbah.graph.library.LibraryVisibility
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoriesType
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.view.net.edge.LayoutType
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.ControlViewVisibility
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.JComboBox

object GraphProperties {

	fun modelId(
		name: String = "modelId",
		baseKey: String = AbstractGraphElementView.BASE_KEY_MODEL_ID,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Int> = CommandPropertySwing(name, baseKey, Int::class.java, beanProvider)

	fun propagationDelay(name: String = "propagationDelay", beanProvider: BeanProvider = componentBeanProvider): CommandPropertySwing<Long> =
		CommandPropertySwing(name, AbstractGraphElementView.BASE_KEY_PROPAGATION_DELAY, Long::class.java, beanProvider)

	fun startupTime(beanProvider: BeanProvider = drawingBeanProvider): CommandPropertySwing<Long> =
		CommandPropertySwing("graph.startupTime", "graph.property.startupTime", Long::class.java, beanProvider)

	fun label(
		name: String = "label",
		baseKey: String = "graph.property.label",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<TranslatableText> =
		CommandPropertySwing(name, baseKey, TranslatableText::class.java, beanProvider)

	fun verticalLabelPosition(
		name: String = "labelPosition",
		baseKey: String = "graph.property.VerticeLabelPosition",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<VerticeLabelPosition> =
		CommandPropertySwing(name, baseKey, VerticeLabelPosition::class.java, beanProvider)

	fun purelyScripted(beanProvider: BeanProvider = componentBeanProvider): CommandPropertySwing<Boolean> =
		CommandPropertySwing("purelyScripted", "graph.property.GraphViewImpl.purelyScripted", Boolean::class.java, beanProvider)

	fun controlViewVisibility(beanProvider: BeanProvider = componentBeanProvider): CommandPropertySwing<ControlViewVisibility> =
		CommandPropertySwing("controlViewVisibility", baseKey = ControlViewVisibility.BASE_KEY, ControlViewVisibility::class.java, beanProvider)

	fun graphParamDefinitions(
		name: String = "graph.parameterDefinitions",
		baseKey: String = "graph.property.graphParams",
		beanProvider: BeanProvider = drawingBeanProvider
	): CommandPropertySwing<GraphParamDefinitions> =
		CommandPropertySwing(name, baseKey, GraphParamDefinitions::class.java, beanProvider)
}

class PortTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PortType.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<PortType>()
	}
}

class LayoutEditor(filter: (LayoutType) -> Boolean = { _ -> true} ) : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(LayoutType.values().filter { filter.invoke(it) }.toTypedArray())
		(editor as JComboBox<*>).renderer = EnumRenderer<LayoutType>()
	}
}

class NetViewStyleEditor(filter: (NetViewStyle) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(NetViewStyle.values().filter { filter.invoke(it) }.toTypedArray())
		(editor as JComboBox<*>).renderer = EnumRenderer<NetViewStyle>()
	}
}

class PortLabelPositionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PortLabelPosition.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<PortLabelPosition>()
	}
}

class InternalLabelOrientationEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(InternalLabelOrientation.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<InternalLabelOrientation>()
	}
}

class VerticeLabelPositionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(VerticeLabelPosition.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<VerticeLabelPosition>()
	}
}

class ControlViewVisibilityEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(ControlViewVisibility.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<ControlViewVisibility>()
	}
}

class LibraryVisibilityEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(LibraryVisibility.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<LibraryVisibility>()
	}
}

class SignalHistoriesTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(SignalHistoriesType.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<SignalHistoriesType>()
	}
}
