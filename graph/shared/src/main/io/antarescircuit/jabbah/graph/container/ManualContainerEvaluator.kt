package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.graph.ui.GraphFrameController

fun isManualContainer(isManualContainerOrig: Boolean, cmdManager: CommandManager): Boolean {
	var isManualContainer = isManualContainerOrig
	val commands = cmdManager.iterator()
	while (commands.hasNext()) {
		if (commands.next().hasTag(GraphFrameController.EDIT_CONTAINER_TAG)) {
			isManualContainer = true
		}
	}
	return isManualContainer
}