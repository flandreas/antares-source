package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.app.ApplicationMode

/**
 * A graph application sets an instance of [GraphApplicationContext] as the application context
 * in the [View]s it displays.
 */
data class GraphApplicationContext(
	val mode: ApplicationMode = ApplicationMode.EDIT,
	val systemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
	val isPausing: Boolean = false
) {
	val isExecute: Boolean get() = mode.isExecute()
}