package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A breadcrumb-like view of a [NavigationStack<GraphView>].
 */
interface NavigationStackView {

	var editable: Boolean

	/** The user can only navigate if the [NavigationStackView] is active. */
	var active: Boolean

	fun update()
}

class NavigationStackViewController(
	initView: NavigationStackView? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	private var _view: NavigationStackView? = initView

	val navigationStack: NavigationStack<GraphView> = NavigationStack(eventBus)

	var view: NavigationStackView
		get() = _view!!
		set(value) {
			_view = value
		}

	private val navigationStackHandler: EventHandler<NavigationStackEvent> = {
		if (it.navigationStack == navigationStack) {
			view.update()
		}
	}

	private val nameChangeHandler: EventHandler<NameChangedEvent> = {
		if (navigationStack.rootEntry != null && navigationStack.rootEntry!!.content.drawing.graph === it.owner) {
			view.update()
		}
	}

	init {
		eventBus.register(NavigationStackEvent::class, navigationStackHandler)
		eventBus.register(NameChangedEvent::class, nameChangeHandler)
	}

	fun dispose() {
		eventBus.unregister(navigationStackHandler)
		eventBus.unregister(nameChangeHandler)
		navigationStack.dispose()
	}
}