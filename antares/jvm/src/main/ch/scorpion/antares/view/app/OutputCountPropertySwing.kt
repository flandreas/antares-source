package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.PortCount
import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing

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