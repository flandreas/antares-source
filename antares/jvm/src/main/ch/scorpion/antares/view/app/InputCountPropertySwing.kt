package ch.scorpion.antares.view.app

import ch.scorpion.antares.model.InputCount
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.PropertyCommandSwing

class InputCountPropertySwing(
	private val beanProvider: BeanProvider,
) : CommandPropertySwing<InputCount>(
	"chosenInputCount",
	InputCount.BASE_KEY,
	InputCount::class.java,
	beanProvider
) {

	override fun createCommand(newValue: InputCount?): PropertyCommandSwing<InputCount> =
		ChangeInputCountCommandSwing(editor!!, beanProvider, beanIds, newValue!!)
}