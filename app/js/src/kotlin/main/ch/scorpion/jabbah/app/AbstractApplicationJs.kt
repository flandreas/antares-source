package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem

abstract class AbstractApplicationJs(
	controller: ApplicationDataViewController
) : AbstractApplication(controller) {

	protected open val logLevel get() = LogLevel.Info

	override fun init() {
		super.init()
		LogSystem.level = logLevel

		openInitialSavable()
	}

	protected abstract fun openInitialSavable()
}