package ch.scorpion.antares.view

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.ui.Toast
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.view.GraphProperties
import org.drjekyll.fontchooser.FontDialog
import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import javax.swing.JOptionPane
import javax.swing.WindowConstants

class TestAction(
	private val editor: Editor
) : AbstractViewAction("view.action.test") {

	companion object {
		private val LOG by logger(TestAction::class)
	}

	override fun execute(event: ActionEvent) {
		showFontChooser()
	}

	private fun showToast() {
		Toast.show("Hallo Antares!")
	}

	private fun createCommandSnapshot() {
		LOG.info("Create new snapshot")
		(editor.commandManager as SourcingCommandManager).addSnapshot()
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
		property.writeToBeans()
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

	private fun showFontChooser() {
		val dialog = FontDialog(Frame.getFrames()[0], "Main Font", true)
		dialog.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE;
		dialog.isVisible = true
		if (!dialog.isCancelSelected) {
			println("Selected font is: ${dialog.selectedFont}")
		}
	}
}