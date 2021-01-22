package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem

abstract class AbstractApplicationJs(
	controller: ApplicationDataViewController
) : AbstractApplication(controller) {

	override fun init() {
		super.init()
		LogSystem.level = LogLevel.Info

		openInitialSavable()
	}

	protected abstract fun openInitialSavable()
}