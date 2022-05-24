package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.command.AbstractCommand

class GenerateContainerCommand(
	private val panel: ContainerPanelSwing
) : AbstractCommand("graph.action.containerLayout.name") {
	override fun execute() {
		panel.generateContainerDrawing()
	}
}