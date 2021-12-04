package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.InputCount
import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing

class InputCountPropertySwing(
	beanProvider: BeanProvider,
) : CommandPropertySwing<InputCount>(
	"chosenInputCount",
	InputCount.BASE_KEY,
	InputCount::class.java,
	beanProvider
) {

	override fun createCommand(newValue: InputCount?): AbstractPropertyCommand<InputCount> =
		ChangeInputCountCommandSwing(editor!!, beanProvider, beanIds, newValue!!)
}