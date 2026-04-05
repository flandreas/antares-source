package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.graph.ui.GraphFrameController

fun isManualContainer(isManualContainerOrig: Boolean, cmdManager: CommandManager): Boolean {
	var isManualContainer = isManualContainerOrig
	val commands = cmdManager.iterator()
	while (commands.hasNext()) {
		val cmd = commands.next()

		// Note that GenerateContainerCommand also has the EDIT_CONTAINER_TAG, so check GENERATE first
		if (cmd.hasTag(GraphFrameController.GENERATE_CONTAINER_TAG)) {
			isManualContainer = false
		} else if (cmd.hasTag(GraphFrameController.EDIT_CONTAINER_TAG)) {
			isManualContainer = true
		}
	}
	return isManualContainer
}