package ch.scorpion.antares.view

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.view.GraphProperties
import java.awt.Frame
import javax.swing.JOptionPane

class TestAction(
	private val editor: Editor
) : AbstractViewAction("view.action.test") {

	override fun execute(event: ActionEvent) {
		setBitWidthGraphParam()
	}

	private fun setBitWidthGraphParam() {
		val property = GraphProperties.graphParamDefinitions()
		property.bind(editor, listOf())

		val param = GraphParamDefinition.create(
			name = "test",
			type = BitWidthGraphParamType,
			defaultValue = BitWidth.BW_4
		)

		val paramDefs = GraphParamDefinitions()
		paramDefs.add(param)

		property.value = paramDefs
		property.writeToBean()
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