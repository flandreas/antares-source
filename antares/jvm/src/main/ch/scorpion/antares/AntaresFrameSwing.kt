package ch.scorpion.antares

import ch.scorpion.antares.model.NetSignalApplierFailure
import ch.scorpion.antares.model.addressable.MemoryLibraryItem
import ch.scorpion.antares.model.expression.BooleanExpressionLibraryItem
import ch.scorpion.antares.model.fsm.FSMLibraryItem
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
import ch.scorpion.antares.view.fsm.FSMGraphDesktopItemSwing
import ch.scorpion.antares.view.truthtable.TruthTableDesktopItemSwing
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.SidebarPaneContentImpl
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.model.image.ImageGraphDesktopItemSwing
import ch.scorpion.jabbah.graph.model.image.ImageLibraryElement
import ch.scorpion.jabbah.graph.ui.GraphFrame
import ch.scorpion.jabbah.graph.ui.GraphFrameActions
import ch.scorpion.jabbah.graph.ui.GraphFrameController
import ch.scorpion.jabbah.graph.ui.GraphFrameSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.documentation.DocumentationDesktopViewItemSwing
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.Frame
import java.awt.Toolkit
import javax.swing.JOptionPane

class AntaresFrameSwing(
	controller: AntaresFrameController,
	application: DesktopApplication,
	viewManager: ContentViewManager,
	actions: GraphFrameActions,
	private val eventBus: EventBus = BaseModule.eventBus
) : GraphFrameSwing(controller as GraphFrameController<GraphFrame>, application, viewManager, actions), AntaresFrame {

	private val testcaseViewController = TestcaseViewController(
		controller.graphPanelViewController.editor,
		application.controller,
		controller.applicationContextHolder,
		controller.applicationModeHolder)

	private val testcasesView = TestcaseViewSwing(testcaseViewController)

	private val testResultsPanel =  TestRunResultsPanel()

	private val netSignalApplierFailureHandler: EventHandler<NetSignalApplierFailure> = { handle(it) }

	init {
		iconImage = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource(AntaresSwing.ICON_PATH))
		eventBus.register(NetSignalApplierFailure::class, netSignalApplierFailureHandler)
		addTestcaseView()
		addTestRunResultsView()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(netSignalApplierFailureHandler)
		testcasesView.dispose()
		testResultsPanel.dispose()
	}

	private fun addTestcaseView() {
		graphPanel.graphEditView.add(
			SidebarPaneContentImpl(
				Translations.getString("antares.testcases.title"),
				Translations.getString("antares.testcases.desc"),
				UiUtil.themedIcon("/img/testcase.png"),
				testcasesView,
				listOf(testcasesView.runAction, testcasesView.controller.metaAddAction, testcasesView.helpAction)))
	}

	private fun addTestRunResultsView() {
		graphPanel.addBottom(
			SidebarPaneContentImpl(
				Translations.getString("antares.testcase.results.title"),
				Translations.getString("antares.testcase.results.desc"),
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

	override fun createFSMDesktopViewItem(item: FSMLibraryItem): GraphDesktopViewItem =
		FSMGraphDesktopItemSwing(item, application.controller)

	override fun createDocumentationDesktopViewItem(documentation: Document, metaGraphName: String): GraphDesktopViewItem =
        DocumentationDesktopViewItemSwing(documentation, metaGraphName = metaGraphName)

	override fun showMemoryContents(request: OpenMemoryContentsRequest) {
		AddressableContentsPanel.showAsDialog(
			parent = Frame.getFrames()[0],
			view = request.drawingView,
			applicationContextHolder = controller.applicationContextHolder,
			name = request.name,
			link = request.link,
			cmdManager = controller.graphPanelViewController.editor.commandManager)
	}

	private fun handle(failure: NetSignalApplierFailure) {
		if (JOptionPane.showConfirmDialog(
			this,
				Translations.getString("antares.netSignalApplierFailure.msg"),
			Translations.getString("antares.netSignalApplierFailure.title"),
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.ERROR_MESSAGE
		) == JOptionPane.OK_OPTION) {
			val edgeViews = (controller.editor.drawing as GraphView).getEdgeViews()
				.filter { failure.nets.contains(it.net) }
				.toList()
			controller.editor.view.content.selectionManager.select(edgeViews)
		}
	}
}