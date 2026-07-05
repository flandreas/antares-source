package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.model.EditProperties.magnitudeValue
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.edit.properties.MagnitudeValueProperty
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit

object AnalogProperties {

	fun resistance(name: String = "resistance", baseKey: String = "element.property.resistance", beanProvider: BeanProvider = componentBeanProvider): MagnitudeValueProperty =
		EditProperties.ohm(name, baseKey, beanProvider)

	fun farad(
		name: String = "capacitance",
		baseKey: String = "element.property.capacitance",
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Farad)

	fun henry(
		name: String = "inductance",
		baseKey: String = "element.property.inductance",
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Henry)

	fun volt(
		name: String = "voltage",
		baseKey: String = "element.property.voltage",
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Volt)

	fun ampere(
		name: String = "current",
		baseKey: String = "element.property.current",
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Ampere)

	fun variable(name: String = "variable", baseKey: String = "element.property.variable", beanProvider: BeanProvider = componentBeanProvider): CommandPropertySwing<Boolean> =
		CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
}