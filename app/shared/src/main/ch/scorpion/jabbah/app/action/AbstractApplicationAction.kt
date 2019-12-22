package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.System

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