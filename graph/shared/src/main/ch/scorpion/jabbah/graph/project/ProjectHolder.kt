package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/** Holds the one and only [Project].*/
class ProjectHolder(
	p: Project? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	var p: Project? = p
		set(value) {
			field?.dispose()
			field = value
			eventBus.post(ProjectEvent(field))
		}

	val project: Project? get() = p
}

/**
 * Posted on an [EventBus] when the current project has changed.
 */
data class ProjectEvent(val project: Project?)