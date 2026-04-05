package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.model.text.description.NameChangedEvent
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.model.graph.GraphNameCommand
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * A breadcrumb-like view of a [NavigationStack<GraphView>].
 */
interface NavigationStackView : UIView {

	val controller: NavigationStackViewController

	/**
	 * Determines only if the [NavigationStack] head should graphically indicate that
	 * its content [GraphView] is editable. Doesn't affect editability of the [NavigationStackView] itself.
	 */
	var editable: Boolean

	/** The user can only navigate if the [NavigationStackView] is active. */
	var active: Boolean

	/** Called by the [NavigationStackViewController] if the model has changed. */
	fun refresh()
}

class NavigationStackViewController(
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<NavigationStackView>() {

	companion object {
		private val LOG by logger(NavigationStackViewController::class)
	}

	val navigationStack: NavigationStack<GraphView> = NavigationStack(eventBus)

	private val navigationStackHandler: EventHandler<NavigationStackEvent> = {
		if (it.navigationStack === navigationStack) {
			view.refresh()
		}
	}

	private val nameChangeHandler: EventHandler<NameChangedEvent> = {
		if (navigationStack.rootEntry != null && navigationStack.rootEntry!!.content.drawing.graph === it.owner) {
			view.refresh()
		}
	}

	init {
		eventBus.register(NavigationStackEvent::class, navigationStackHandler)
		eventBus.register(NameChangedEvent::class, nameChangeHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(navigationStackHandler)
		eventBus.unregister(nameChangeHandler)
		navigationStack.dispose()
	}

	fun changeName(newName: String) {
		LOG.userTrail("Graph name changed in NavigationStackView to '$newName'")
		val oldName = navigationStack.rootEntry!!.content.drawingView.drawing.name
		EditModule.commandManager.execute(
			GraphNameCommand(
				navigationStack.rootEntry!!.content.drawingView,
				oldName,
				oldName.withTranslation(newName) as Name
			)
		)
	}
}