package io.antarescircuit.antares.view

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidthGraphParamType
import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.WelcomePanel
import io.antarescircuit.jabbah.base.IssueImpl
import io.antarescircuit.jabbah.base.IssueSeverity
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBusStatistics
import io.antarescircuit.jabbah.base.invocation.UnexpectedErrorServiceImpl
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.draw.ui.Toast
import io.antarescircuit.jabbah.draw.view.AbstractViewAction
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.command.AbstractCommand
import io.antarescircuit.jabbah.edit.command.SourcingCommandManager
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinition
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinitions
import io.antarescircuit.jabbah.graph.view.GraphProperties
import io.antarescircuit.jabbah.graph.view.EdgeView
import org.drjekyll.fontchooser.FontDialog
import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import java.net.URL
import javax.swing.JOptionPane
import javax.swing.WindowConstants

class TestAction(
	private val application: Application,
	private val editor: Editor
) : AbstractViewAction("view.action.test") {

	companion object {
		private val LOG by logger(TestAction::class)
	}

	private var eventBusStatistic: EventBusStatistics? = null

	override fun execute(event: ActionEvent) {
		throwException()
	}

	private fun postIssue() {
		BaseModule.eventBus.post(IssueImpl(
			IssueSeverity.Warning,
			"Name",
			"Description",
			"Origin",
			"Context"
		))
	}

	private fun showWelcomeMessage() {
		(BaseModuleJvm.unexpectedErrorService as UnexpectedErrorServiceImpl).baseUrl = URL("https://api.antarescircuit.io/api")
		WelcomePanel.showAsDialog(application)
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