package ch.scorpion.antares

import ch.scorpion.antares.model.truthtable.ShowTruthTableItemRequest
import ch.scorpion.antares.view.AntaresFrame
import ch.scorpion.antares.view.AntaresFrameController
import ch.scorpion.antares.view.addressable.AddressableContentGraphDesktopItemSwing
import ch.scorpion.antares.view.addressable.AddressableContentsPanel
import ch.scorpion.antares.view.addressable.OpenMemoryContentsRequest
import ch.scorpion.antares.view.truthtable.TruthTableDesktopItemSwing
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import java.awt.Frame
import java.awt.Toolkit
import javax.swing.JOptionPane

class AntaresFrameSwing(
	controller: AntaresFrameController,
	application: DesktopApplication,
	eventBus: EventBus,
	viewManager: ViewManager,
	actions: GraphFrameActions
) : GraphFrameSwing(controller as GraphFrameController<GraphFrame>, application, eventBus, viewManager, actions), AntaresFrame {

	init {
		iconImage = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource(AntaresSwing.ICON_PATH))
	}

	override fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor): GraphDesktopViewItem =
		AddressableContentGraphDesktopItemSwing(
			drawingView = request.drawingView,
			addressableId = request.addressable.id,
			title = request.name,
			applicationContextHolder = controller.applicationContextHolder,
			cmdManager = controller.graphPanelViewController.editor.commandManager,
			contextColor = contextColor)

	override fun createTruthTableDesktopViewItem(request: ShowTruthTableItemRequest): GraphDesktopViewItem =
		TruthTableDesktopItemSwing(request.item, editor.commandManager)

	override fun showMemoryContents(request: OpenMemoryContentsRequest) {
		AddressableContentsPanel.showAsDialog(
			parent = Frame.getFrames()[0],
			view = request.drawingView,
			applicationContextHolder = controller.applicationContextHolder,
			name = request.name,
			addressableId = request.addressable.id,
			cmdManager = controller.graphPanelViewController.editor.commandManager)
	}

	override fun shouldReplaceLightColor(): Boolean {
		return JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("antares.action.replaceLightColor.question"),
			Translations.getString("antares.action.replaceLightColor.name"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION
	}
}