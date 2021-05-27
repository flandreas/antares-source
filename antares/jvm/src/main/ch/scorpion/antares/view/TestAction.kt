package ch.scorpion.antares.view

import ch.scorpion.antares.view.net.DigitalNodeView
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.view.GraphElementView
import java.awt.Frame
import javax.swing.JOptionPane

class TestAction : AbstractViewAction("view.action.test") {

	override fun execute(event: ActionEvent) {
		val nodeView = DigitalNodeView()
		val drawing = (view as DrawingView<Drawing<GraphElementView<*>>>).drawing

		drawing.add(nodeView)
		drawing.validate()
	}

	private fun noAction() {
		JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			"Currently no test action implemented",
			name,
			JOptionPane.DEFAULT_OPTION
		)
	}
}