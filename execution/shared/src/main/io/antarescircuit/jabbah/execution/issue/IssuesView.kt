package io.antarescircuit.jabbah.execution.issue

import io.antarescircuit.jabbah.base.Issue
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.module.ExecutionModule

interface IssuesView : UIView {

	fun refresh()

	/** Informs this [IssuesView] that new [Issue]s have arrived.*/
	fun notifyNewIssues()

	fun showCannotOpenMessage()
}

class IssuesViewController(
	val issueCollector: IssueCollector = ExecutionModule.issueCollector,
	val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<IssuesView>() {

	val clearAction: Action = ClearIssuesViewAction()

	val openAction: Action = OpenAction()

	private val issueCollectorEventHandler: EventHandler<IssueCollectorEvent> = {
		if (it.issue != null) {
			view.notifyNewIssues()
		} else {
			view.refresh()
		}
	}

	init {
		openAction.enabled = false
		eventBus.register(IssueCollectorEvent::class, issueCollectorEventHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(issueCollectorEventHandler)
	}

	/** Set by [IssuesView] if the selection changes. */
	var selectedIssue: Issue? = null
		set(value) {
			field = value
			updateActions()
		}

	fun clearIssues() {
		issueCollector.clear()
		updateActions()
	}

	private fun updateActions() {
		openAction.enabled = selectedIssue != null
	}

	fun openSelectedIssue() {
		if (selectedIssue != null) {
			if (selectedIssue!!.actionHandler == null) {
				view.showCannotOpenMessage()
			} else {
				selectedIssue!!.actionHandler!!.invoke(selectedIssue!!)
			}
		}
	}

	private inner class ClearIssuesViewAction : AbstractAction(baseName = "execution.action.issue.clear", "/img/trash-16.png") {
		override fun execute(event: ActionEvent) {
			clearIssues()
		}
	}

	private inner class OpenAction : AbstractAction(baseName = "execution.action.issue.open", "/img/openInPopup-20.png") {
		override fun execute(event: ActionEvent) {
			openSelectedIssue()
		}
	}
}