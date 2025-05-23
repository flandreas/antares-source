package ch.scorpion.antares.view

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBusStatistics
import ch.scorpion.jabbah.base.invocation.UnexpectedErrorServiceImpl
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.ui.Toast
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AbstractCommand
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.view.GraphProperties
import ch.scorpion.jabbah.graph.view.EdgeView
import org.drjekyll.fontchooser.FontDialog
import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import java.net.URL
import javax.swing.JOptionPane
import javax.swing.WindowConstants

class TestAction(
	private val editor: Editor
) : AbstractViewAction("view.action.test") {

	companion object {
		private val LOG by logger(TestAction::class)
	}

	private var eventBusStatistic: EventBusStatistics? = null

	override fun execute(event: ActionEvent) {
		(BaseModuleJvm.unexpectedErrorService as UnexpectedErrorServiceImpl).baseUrl = URL("https://api.antarescircuit.io/api")
		throwException()
	}

	private fun throwException() {
		throw Exception("Test Exception")
	}

	private fun printEventBusStatistic() {
		val newEventBusStatistic = BaseModule.eventBus.createStatistics()

		println(newEventBusStatistic.print())
		if (eventBusStatistic != null) {
			println()
			println(newEventBusStatistic.printExpansion(eventBusStatistic!!))
		}

		eventBusStatistic = newEventBusStatistic
	}

	private fun showToast() {
		Toast.show("Hallo Antares!")
	}

	private fun showComponentMessage() {
		BaseModule.eventBus.post(
			ComponentMessage(ComponentMessageType.Info, null, "element.property.transistorSymbol.desc")
		)
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

	/**
	 * Creates (an otherwise no-op) Command that forces [Net.BROKEN_REF_DESIGN_ERROR]
	 * on the [Net] of the currently selected [EdgeView]. Only used for testing.
	 */
	private fun forceBrokenRefError() {
		if (editor.view.selectionManager.selectionCount != 1) {
			return
		}
		val component = editor.view.selectionManager.selection.first()
		if (component !is EdgeView<*>) {
			return
		}
		LOG.debug("Forcing BrokenRefError on EdgeView ${component.id}")
		editor.commandManager.execute(ForceBrokenRefErrorCommand(component.id, editor.view))
	}

	private class ForceBrokenRefErrorCommand(
		private val componentId: Int,
		private val drawingView: DrawingView<Drawing<Component>>
	) : AbstractCommand("view.action.test.name") {

		private val edgeView: EdgeView<*> get() = drawingView.drawing.getWithId(componentId) as EdgeView<*>

		override fun execute() {
			if (edgeView.origin != null && edgeView.origin!!.port != null) {
				edgeView.net!!.unconnect(edgeView.origin!!.port!!)
				return
			}
			if (edgeView.destination != null && edgeView.destination!!.port != null) {
				edgeView.net!!.unconnect(edgeView.destination!!.port!!)
				return
			}
		}
	}
}