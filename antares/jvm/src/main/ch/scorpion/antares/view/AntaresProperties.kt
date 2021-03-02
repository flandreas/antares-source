package ch.scorpion.antares.view

import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import ch.scorpion.jabbah.graph.model.PortType

object AntaresProperties {

	fun bitWidth(): PropertyImpl<BitWidth> =
		PropertyImpl("bitWidth", "element.property.bitWidth", BitWidth::class.java, componentBeanProvider)

	fun portType(name: String = "portType"): PropertyImpl<PortType> =
		PropertyImpl(name, "graph.property.portType", PortType::class.java, componentBeanProvider)

	fun signalRepresentation(name: String ="signalRepresentation"): PropertyImpl<DigitalSignalRepresentation> =
		PropertyImpl(name, "element.property.DigitalSignalRepresentation", DigitalSignalRepresentation::class.java, componentBeanProvider)

	fun handedness(name: String = "handedness", baseKey: String): PropertyImpl<Handedness> =
		PropertyImpl(name, baseKey, Handedness::class.java, componentBeanProvider)

	fun lightColor(name: String = "lightColor", baseKey: String = "element.property.LEDColor"): PropertyImpl<LightColor> =
		PropertyImpl(name, baseKey, LightColor::class.java, componentBeanProvider)

	fun pullDirection(name: String = "pullDirection", baseKey: String = "element.property.PullDirection"): PropertyImpl<PullDirection> =
		PropertyImpl(name, baseKey, PullDirection::class.java, componentBeanProvider)

}