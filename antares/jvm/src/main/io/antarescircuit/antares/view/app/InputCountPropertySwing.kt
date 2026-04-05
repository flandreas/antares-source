package io.antarescircuit.antares.view.app

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.jabbah.edit.AbstractPropertyCommand
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing

class InputCountPropertySwing(
	beanProvider: BeanProvider,
) : CommandPropertySwing<PortCount>(
	"chosenInputCount",
	PortCount.INPUT_COUNT_BASE_KEY,
	PortCount::class.java,
	beanProvider
) {

	override fun createCommand(newValue: PortCount?): AbstractPropertyCommand<PortCount> =
		ChangeInputCountCommandSwing(editor!!, beanProvider, beanIds, newValue!!)
}