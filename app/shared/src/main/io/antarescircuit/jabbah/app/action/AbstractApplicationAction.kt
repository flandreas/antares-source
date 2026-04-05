package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.System

abstract class AbstractApplicationAction(
	name: String,
	description: String? = null,
	accelerator: String? = null,
	protected val application: Application
) : AbstractAction(name, description, accelerator) {

	constructor(
		baseName: String,
		application: Application
	) : this(
		Translations.getString("$baseName.name"),
		Translations.getOptionalString("$baseName.desc"),
		Translations.getOptionalString(System.getActionAcceleratorKey(baseName)),
		application)
}