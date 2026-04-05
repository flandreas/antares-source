package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.LogLevel
import io.antarescircuit.jabbah.base.LogSystem

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