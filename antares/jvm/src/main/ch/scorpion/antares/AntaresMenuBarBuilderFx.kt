package ch.scorpion.antares

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.MenuBarBuilderFx
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

class AntaresMenuBarBuilderFx(
	application: DesktopApplication,
	eventBus: EventBus = BaseModule.eventBus
) : MenuBarBuilderFx(application, eventBus) {

	// TODO Add application-specific menu items
}