package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A breadcrumb-like view of a [NavigationStack<GraphView>].
 */
interface NavigationStackView {
	fun update()
}

interface NavigationStackViewActions {
	val navigationStack: NavigationStack<GraphView>
}

class NavigationStackViewController(
	initView: NavigationStackView? = null,
	private val eventBus: EventBus = BaseModule.eventBus,
	override val navigationStack: NavigationStack<GraphView> = NavigationStack(eventBus)
) : NavigationStackViewActions {

	private var _view: NavigationStackView? = initView

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
		if (navigationStack.rootEntry != null && navigationStack.rootEntry!!.graphName === it.name) {
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