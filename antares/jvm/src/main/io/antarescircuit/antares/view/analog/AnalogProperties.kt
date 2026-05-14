package io.antarescircuit.antares.view.analog

import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.MagnitudeValueProperty

object AnalogProperties {

	fun resistance(name: String = "resistance", baseKey: String = "element.property.resistance", beanProvider: BeanProvider = componentBeanProvider): MagnitudeValueProperty =
		EditProperties.ohm(name, baseKey, beanProvider)
}