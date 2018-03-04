package ch.scorpion.jabbah.graph.uifx

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.MenuBarBuilderFx
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

open class GraphMenuBarBuilderFx(
	application: DesktopApplication,
	eventBus: EventBus = BaseModule.eventBus
) : MenuBarBuilderFx(application, eventBus) {

	// TODO Add application-specific menu items
}