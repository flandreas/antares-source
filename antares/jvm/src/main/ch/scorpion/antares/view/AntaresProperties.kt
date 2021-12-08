package ch.scorpion.antares.view

import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.net.TransistorViewSymbol
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.signal.BitWidthPropertySwing
import ch.scorpion.jabbah.base.dsl.ParserFactory
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView

object AntaresProperties {

	fun bitWidth(
		name: String = "bitWidth",
		baseKey: String = BitWidth.BASE_KEY,
		beanProvider: BeanProvider = componentBeanProvider,
		parserFactory: ParserFactory? = null
	): BitWidthPropertySwing = BitWidthPropertySwing(name, baseKey, beanProvider, parserFactory)

	fun bitWidth(
		name: String = "bitWidth",
		baseKey: String = BitWidth.BASE_KEY,
		beanProvider: BeanProvider = componentBeanProvider,
		editor: Editor
	): BitWidthPropertySwing {
		val graphView = editor.drawing as GraphView
		val parserFactory = if (graphView.graph != null) {
			graphView.graph!!::createParser
		} else {
			null
		}
		return BitWidthPropertySwing(name, baseKey, beanProvider, parserFactory)
	}

	fun portType(name: String = "portType"): CommandPropertySwing<PortType> =
		CommandPropertySwing(name, PortType.BASE_KEY, PortType::class.java, componentBeanProvider)

	fun signalRepresentation(name: String ="signalRepresentation"): CommandPropertySwing<DigitalSignalRepresentation> =
		CommandPropertySwing(name, DigitalSignalRepresentation.BASE_KEY, DigitalSignalRepresentation::class.java, componentBeanProvider)

	fun handedness(name: String = "handedness", baseKey: String): CommandPropertySwing<Handedness> =
		CommandPropertySwing(name, baseKey, Handedness::class.java, componentBeanProvider)

	fun lightColor(name: String = "lightColor", baseKey: String = "element.property.LEDColor"): CommandPropertySwing<LightColor> =
		CommandPropertySwing(name, baseKey, LightColor::class.java, componentBeanProvider)

	fun pullDirection(name: String = "pullDirection", baseKey: String = "element.property.PullDirection"): CommandPropertySwing<PullDirection> =
		CommandPropertySwing(name, baseKey, PullDirection::class.java, componentBeanProvider)

	fun transistorType(name: String = "transistorType", baseKey: String = "element.property.transistorType"): CommandPropertySwing<TransistorType> =
		CommandPropertySwing(name, baseKey, TransistorType::class.java, componentBeanProvider)

	fun transistorSymbol(name: String = "symbol", baseKey: String = "element.property.transistorSymbol"): CommandPropertySwing<TransistorViewSymbol> =
		CommandPropertySwing(name, baseKey, TransistorViewSymbol::class.java, componentBeanProvider)

	fun ledSquare(name: String = "square", baseKey: String = "element.property.LED.square"): CommandPropertySwing<Boolean> =
		CommandPropertySwing(name, baseKey, Boolean::class.java, componentBeanProvider)

	fun joystickDeflection(name: String = "deflection", baseKey: String = "element.property.joystickDeflection"): CommandPropertySwing<JoystickDeflectionEditor> =
		CommandPropertySwing(name, baseKey, JoystickDeflectionEditor::class.java, componentBeanProvider)

	fun portViewSpacing(name: String = "portViewSpacing", baseKey: String = "element.property.portViewSpacing"): CommandPropertySwing<PortViewSpacing> =
		CommandPropertySwing(name, baseKey, PortViewSpacing::class.java, componentBeanProvider)
}