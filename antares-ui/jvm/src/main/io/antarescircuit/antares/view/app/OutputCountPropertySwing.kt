package io.antarescircuit.antares.view.app

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.jabbah.edit.AbstractPropertyCommand
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing

class OutputCountPropertySwing(
	propertyName: String = "chosenOutputCount",
	beanProvider: BeanProvider
) : CommandPropertySwing<PortCount>(
	propertyName,
	PortCount.OUTPUT_COUNT_BASE_KEY,
	PortCount::class.java,
	beanProvider,
	setterPropertyName = propertyName,
	getterPropertyName = propertyName,
) {

	override fun createCommand(newValue: PortCount?): AbstractPropertyCommand<PortCount> =
		ChangeOutputCountCommandSwing(editor!!, beanProvider, beanIds, newValue!!, getterPropertyName, setterPropertyName)
}