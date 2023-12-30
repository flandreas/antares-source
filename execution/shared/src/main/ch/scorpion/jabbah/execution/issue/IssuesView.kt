package ch.scorpion.jabbah.execution.issue

import ch.scorpion.jabbah.base.Issue
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.module.ExecutionModule

interface IssuesView : UIView {

	/** Informs this [IssuesView] that new [Issue]s have arrived.*/
	fun notifyNewIssues()
}

class IssuesViewController(
	val issueCollector: IssueCollector = ExecutionModule.issueCollector,
	val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<IssuesView>() {

	val clearAction: Action = ClearIssuesViewAction()

	private val issueCollectorEventHandler: EventHandler<IssueCollectorEvent> = {
		view.notifyNewIssues()
	}

	init {
		eventBus.register(IssueCollectorEvent::class, issueCollectorEventHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(issueCollectorEventHandler)
	}

	fun clearIssues() {
		issueCollector.clear()
	}

	private inner class ClearIssuesViewAction
		: AbstractAction(baseName = "graph.action.clearIssuesPanel","/img/trash-16.png") {

		override fun execute(event: ActionEvent) {
			clearIssues()
		}
	}
}