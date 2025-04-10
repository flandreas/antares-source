package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.model.graph.GraphNameCommand
import ch.scorpion.jabbah.graph.view.GraphView

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