package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.LongValue
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
import ch.scorpion.jabbah.graph.model.param.ExpressionPropertySwing
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice
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

	fun propagationDelay(name: String = "propagationDelay", editable: Boolean = true, beanProvider: BeanProvider = componentBeanProvider) =
		ExpressionPropertySwing(name, AbstractGraphElementView.BASE_KEY_PROPAGATION_DELAY, LongValue::class.java, beanProvider, _editable = editable)

	fun overallPropagationDelay(name: String ="overallPropagationDelay", beanProvider: BeanProvider = componentBeanProvider) =
		CommandPropertySwing(name, AbstractGraphElementView.BASE_KEY_PROPAGATION_DELAY, Long::class.java, beanProvider)

	fun interactivePropagationDelay(name: String = "interactivePropagationDelay", beanProvider: BeanProvider = componentBeanProvider) =
		CommandPropertySwing(name, InteractableVertice.BASE_KEY_INTERACTIVE_PROPAGATION_DELAY, Long::class.java, beanProvider)

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

	fun graphPortStartValue(
		name: String = "startValue",
		baseKey: String = "graph.property.input.startValue",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Long> = CommandPropertySwing(name, baseKey, Long::class.java, beanProvider)

	fun nonVolatile(
		name: String = "nonVolatile",
		baseKey: String = "graph.property.nonVolatile",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> = CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
}

class PortTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PortType.entries.toTypedArray())
		(editor as JComboBox<PortType>).renderer = EnumRenderer()
	}
}

class LayoutEditor(filter: (LayoutType) -> Boolean = { _ -> true} ) : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(LayoutType.entries.filter { filter.invoke(it) }.toTypedArray())
		(editor as JComboBox<LayoutType>).renderer = EnumRenderer()
	}
}

class NetViewStyleEditor(filter: (NetViewStyle) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(NetViewStyle.entries.filter { filter.invoke(it) }.toTypedArray())
		(editor as JComboBox<NetViewStyle>).renderer = EnumRenderer()
	}
}

class PortLabelPositionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PortLabelPosition.entries.toTypedArray())
		(editor as JComboBox<PortLabelPosition>).renderer = EnumRenderer()
	}
}

class InternalLabelOrientationEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(InternalLabelOrientation.entries.toTypedArray())
		(editor as JComboBox<InternalLabelOrientation>).renderer = EnumRenderer()
	}
}

class VerticeLabelPositionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(VerticeLabelPosition.entries.toTypedArray())
		(editor as JComboBox<VerticeLabelPosition>).renderer = EnumRenderer()
	}
}

class ControlViewVisibilityEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(ControlViewVisibility.entries.toTypedArray())
		(editor as JComboBox<ControlViewVisibility>).renderer = EnumRenderer()
	}
}

class LibraryVisibilityEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(LibraryVisibility.entries.toTypedArray())
		(editor as JComboBox<LibraryVisibility>).renderer = EnumRenderer()
	}
}

class SignalHistoriesTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(SignalHistoriesType.entries.toTypedArray())
		(editor as JComboBox<SignalHistoriesType>).renderer = EnumRenderer()
	}
}
