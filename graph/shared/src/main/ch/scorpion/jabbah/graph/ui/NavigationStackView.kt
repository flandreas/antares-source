package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ui.AbstractUIController
import ch.scorpion.jabbah.app.ui.UIView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A breadcrumb-like view of a [NavigationStack<GraphView>].
 */
interface NavigationStackView : UIView {

	/**
	 * Determines only if the [NavigationStack] head should graphically indicate that
	 * its content [GraphView] is editable. Doesn't effect editability of the [NavigationStackView] itself.
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
}