package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing

object AnalogProperties {

	fun resistance(name: String = "resistance", baseKey: String = "element.property.resistance", beanProvider: BeanProvider = componentBeanProvider): CommandPropertySwing<Double> =
		CommandPropertySwing(name,baseKey, Double::class.java, beanProvider)
}