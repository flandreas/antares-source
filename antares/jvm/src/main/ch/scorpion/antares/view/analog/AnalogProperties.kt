package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing

object AnalogProperties {

	fun resistance(name: String = "resistance", baseKey: String = "element.property.resistance", beanProvider: BeanProvider = componentBeanProvider): CommandPropertySwing<Double> =
		CommandPropertySwing(name,baseKey, Double::class.java, beanProvider)
}