package ch.scorpion.antares.view

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.ui.Toast
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.view.GraphProperties
import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import javax.swing.JOptionPane

class TestAction(
	private val editor: Editor
) : AbstractViewAction("view.action.test") {

	override fun execute(event: ActionEvent) {
		showToast()
	}

	private fun showToast() {
		Toast.show("Hallo Antares!")
	}

	private fun setBitWidthGraphParam() {
		val property = GraphProperties.graphParamDefinitions()
		property.bind(editor, listOf())

		val param = GraphParamDefinition.create(
			name = "test",
			type = BitWidthGraphParamType,
			defaultValue = BitWidth.BW_4
		)

		val paramDefs = GraphParamDefinitions().withDefinition(param)

		property.value = paramDefs
		property.writeToBean()
	}

	private fun windowSize16to9() {
		val frame = Frame.getFrames()[0]
		frame.size = Dimension(1920, 1080)
		frame.location = Point(0, 0)
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