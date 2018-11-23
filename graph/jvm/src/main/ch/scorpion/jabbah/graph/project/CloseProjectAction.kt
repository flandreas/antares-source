package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Closes the currently open [Project].
 */
class CloseProjectAction(
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService,
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("project.action.close") {

	init {
		eventBus.register(CurrentProjectEvent::class) { updateEnabledness() }
		updateEnabledness()
	}

	private fun updateEnabledness() {
		enabled = projectHolder.p != null
	}

	override fun execute(event: ActionEvent) {
		managementService.close()
	}
}