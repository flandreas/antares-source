package ch.scorpion.antares

import ch.scorpion.antares.view.AntaresFrame
import ch.scorpion.antares.view.AntaresFrameController
import ch.scorpion.antares.view.addressable.AddressableContentGraphDesktopItem
import ch.scorpion.antares.view.addressable.AddressableContentsPanel
import ch.scorpion.antares.view.addressable.OpenMemoryContentsRequest
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.graph.ui.*
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

	override fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor): GraphDesktopViewItem {
		return AddressableContentGraphDesktopItem(
			controller = application.controller,
			addressable = request.addressable,
			title = request.name,
			applicationContextHolder = controller.applicationContextHolder,
			cmdManager = controller.graphPanelViewController.editor.commandManager,
			readonly = request.readonly,
			contextColor = contextColor)
	}

	override fun showMemoryContents(request: OpenMemoryContentsRequest) {
		AddressableContentsPanel.showAsDialog(
			parent = Frame.getFrames()[0],
			controller = application.controller,
			applicationContextHolder = controller.applicationContextHolder,
			name = request.name,
			addressable = request.addressable,
			cmdManager = controller.graphPanelViewController.editor.commandManager,
			readonly = request.readonly)
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