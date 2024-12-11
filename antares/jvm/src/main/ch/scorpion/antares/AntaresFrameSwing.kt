package ch.scorpion.antares

import ch.scorpion.antares.model.addressable.MemoryLibraryItem
import ch.scorpion.antares.model.expression.BooleanExpressionLibraryItem
import ch.scorpion.antares.model.testcase.TestcaseViewController
import ch.scorpion.antares.model.testcase.TestcaseViewSwing
import ch.scorpion.antares.model.testcase.result.TestRunResultsPanel
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.antares.view.AntaresFrame
import ch.scorpion.antares.view.AntaresFrameController
import ch.scorpion.antares.view.addressable.AddressableContentGraphDesktopItemSwing
import ch.scorpion.antares.view.addressable.AddressableContentsPanel
import ch.scorpion.antares.view.addressable.MemoryStorableGraphDesktopItemSwing
import ch.scorpion.antares.view.addressable.OpenMemoryContentsRequest
import ch.scorpion.antares.view.expression.BooleanExpressionDesktopItemSwing
import ch.scorpion.antares.view.truthtable.TruthTableDesktopItemSwing
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.SidebarPaneContentImpl
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.graph.model.image.ImageGraphDesktopItemSwing
import ch.scorpion.jabbah.graph.model.image.ImageLibraryElement
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphFrameActions
import ch.scorpion.jabbah.graph.ui.GraphFrameController
import ch.scorpion.jabbah.graph.ui.GraphFrameSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import java.awt.Frame
import java.awt.Toolkit
import javax.swing.JOptionPane

class AntaresFrameSwing(
	controller: AntaresFrameController,
	application: DesktopApplication,
	viewManager: ContentViewManager,
	actions: GraphFrameActions
) : GraphFrameSwing(controller as GraphFrameController<GraphFrame>, application, viewManager, actions), AntaresFrame {

	private val testcaseViewController = TestcaseViewController(
		controller.graphPanelViewController.editor,
		controller.applicationContextHolder,
		controller.applicationModeHolder)

	private val testcasesView = TestcaseViewSwing(testcaseViewController, application, controller.applicationModeHolder)

	private val testResultsPanel =  TestRunResultsPanel()

	init {
		iconImage = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource(AntaresSwing.ICON_PATH))
		addTestcaseView(application)
		addTestRunResultsView()
	}

	override fun dispose() {
		super.dispose()
		testcasesView.dispose()
		testResultsPanel.dispose()
	}

	private fun addTestcaseView(application: Application) {
		graphPanel.graphEditView.add(
			SidebarPaneContentImpl(
				Translations.getString("antares.testcases.title"),
				UiUtil.themedIcon("/img/testcase.png"),
				testcasesView,
				listOf(testcasesView.runAction, testcasesView.helpAction)))
	}

	private fun addTestRunResultsView() {
		graphPanel.addBottom(
			SidebarPaneContentImpl(
				Translations.getString("antares.testcase.results.title"),
				UiUtil.themedIcon("/img/testcase.png"),
				testResultsPanel,
				listOf(testResultsPanel.clearAction))
		)
	}

	override fun createMemoryContentsDesktopViewItem(request: OpenMemoryContentsRequest, contextColor: CompositeColor): GraphDesktopViewItem =
		AddressableContentGraphDesktopItemSwing(
			drawingView = request.drawingView,
			link = request.link,
			title = request.name,
			applicationContextHolder = controller.applicationContextHolder,
			cmdManager = controller.graphPanelViewController.editor.commandManager,
			contextColor = contextColor)

	override fun createTruthTableDesktopViewItem(item: TruthTableLibraryItem): GraphDesktopViewItem =
		TruthTableDesktopItemSwing(item, application.controller, commandManager = editor.commandManager)

	override fun createBooleanExpressionDesktopViewItem(item: BooleanExpressionLibraryItem): GraphDesktopViewItem =
		BooleanExpressionDesktopItemSwing(item, application.controller, commandManager = editor.commandManager)

	override fun createMemoryStorableGraphDesktopViewItem(item: MemoryLibraryItem): GraphDesktopViewItem =
		MemoryStorableGraphDesktopItemSwing(item, application.controller, controller.applicationContextHolder, editor.commandManager)

	override fun createImageGraphDesktopViewItem(element: ImageLibraryElement): GraphDesktopViewItem =
		ImageGraphDesktopItemSwing(element, application.controller)

	override fun showMemoryContents(request: OpenMemoryContentsRequest) {
		AddressableContentsPanel.showAsDialog(
			parent = Frame.getFrames()[0],
			view = request.drawingView,
			applicationContextHolder = controller.applicationContextHolder,
			name = request.name,
			link = request.link,
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