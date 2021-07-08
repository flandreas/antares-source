package ch.scorpion.antares.view

import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.model.PortType

object AntaresProperties {

	fun bitWidth(): CommandPropertySwing<BitWidth> =
		CommandPropertySwing("bitWidth", "element.property.bitWidth", BitWidth::class.java, componentBeanProvider)

	fun portType(name: String = "portType"): CommandPropertySwing<PortType> =
		CommandPropertySwing(name, "graph.property.portType", PortType::class.java, componentBeanProvider)

	fun signalRepresentation(name: String ="signalRepresentation"): CommandPropertySwing<DigitalSignalRepresentation> =
		CommandPropertySwing(name, "element.property.DigitalSignalRepresentation", DigitalSignalRepresentation::class.java, componentBeanProvider)

	fun handedness(name: String = "handedness", baseKey: String): CommandPropertySwing<Handedness> =
		CommandPropertySwing(name, baseKey, Handedness::class.java, componentBeanProvider)

	fun lightColor(name: String = "lightColor", baseKey: String = "element.property.LEDColor"): CommandPropertySwing<LightColor> =
		CommandPropertySwing(name, baseKey, LightColor::class.java, componentBeanProvider)

	fun pullDirection(name: String = "pullDirection", baseKey: String = "element.property.PullDirection"): CommandPropertySwing<PullDirection> =
		CommandPropertySwing(name, baseKey, PullDirection::class.java, componentBeanProvider)

	fun transistorType(name: String = "transistorType", baseKey: String = "element.property.transistorType"): CommandPropertySwing<TransistorType> =
		CommandPropertySwing(name, baseKey, TransistorType::class.java, componentBeanProvider)

	fun ledSquare(name: String = "square", baseKey: String = "element.property.LED.square"): CommandPropertySwing<Boolean> =
		CommandPropertySwing(name, baseKey, Boolean::class.java, componentBeanProvider)

	fun joystickDeflection(name: String = "deflection", baseKey: String = "element.property.joystickDeflection"): CommandPropertySwing<JoystickDeflectionEditor> =
		CommandPropertySwing(name, baseKey, JoystickDeflectionEditor::class.java, componentBeanProvider)
}