package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Closes the currently open [Project].
 */
class CloseProjectAction(
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService.invoke(),
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("project.action.close") {

	private val currentProjectHandler: EventHandler<CurrentProjectEvent> = { updateEnabledness() }

	init {
		eventBus.register(CurrentProjectEvent::class, currentProjectHandler)
		updateEnabledness()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(currentProjectHandler)
	}

	private fun updateEnabledness() {
		enabled = projectHolder.p != null
	}

	override fun execute(event: ActionEvent) {
		managementService.close()
	}
}