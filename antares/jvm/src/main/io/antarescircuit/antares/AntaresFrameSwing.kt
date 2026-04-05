package io.antarescircuit.antares

import io.antarescircuit.antares.model.NetSignalApplierFailure
import io.antarescircuit.antares.model.addressable.MemoryLibraryItem
import io.antarescircuit.antares.model.expression.BooleanExpressionLibraryItem
import io.antarescircuit.antares.model.fsm.FSMLibraryItem
import io.antarescircuit.antares.model.testcase.TestcaseViewController
import io.antarescircuit.antares.model.testcase.TestcaseViewSwing
import io.antarescircuit.antares.model.testcase.result.TestRunResultsPanel
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.antares.view.AntaresFrame
import io.antarescircuit.antares.view.AntaresFrameController
import io.antarescircuit.antares.view.addressable.AddressableContentGraphDesktopItemSwing
import io.antarescircuit.antares.view.addressable.AddressableContentsPanel
import io.antarescircuit.antares.view.addressable.MemoryStorableGraphDesktopItemSwing
import io.antarescircuit.antares.view.addressable.OpenMemoryContentsRequest
import io.antarescircuit.antares.view.expression.BooleanExpressionDesktopItemSwing
import io.antarescircuit.antares.view.fsm.FSMGraphDesktopItemSwing
import io.antarescircuit.antares.view.truthtable.TruthTableDesktopItemSwing
import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.SidebarPaneContentImpl
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.model.image.ImageGraphDesktopItemSwing
import io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement
import io.antarescircuit.jabbah.graph.ui.GraphFrame
import io.antarescircuit.jabbah.graph.ui.GraphFrameActions
import io.antarescircuit.jabbah.graph.ui.GraphFrameController
import io.antarescircuit.jabbah.graph.ui.GraphFrameSwing
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.ui.documentation.DocumentationDesktopViewItemSwing
import io.antarescircuit.jabbah.graph.view.GraphView
import java.awt.Frame
import java.awt.Toolkit
import javax.swing.JOptionPane

class AntaresFrameSwing(
    controller: io.antarescircuit.antares.view.AntaresFrameController,
    application: io.antarescircuit.jabbah.app.DesktopApplication,
    viewManager: io.antarescircuit.jabbah.draw.view.ContentViewManager,
    actions: io.antarescircuit.jabbah.graph.ui.GraphFrameActions,
    private val eventBus: io.antarescircuit.jabbah.base.event.EventBus = _root_ide_package_.io.antarescircuit.jabbah.base.module.BaseModule.eventBus
) : io.antarescircuit.jabbah.graph.ui.GraphFrameSwing(controller as io.antarescircuit.jabbah.graph.ui.GraphFrameController<io.antarescircuit.jabbah.graph.ui.GraphFrame>, application, viewManager, actions),
    io.antarescircuit.antares.view.AntaresFrame {

	private val testcaseViewController =
        _root_ide_package_.io.antarescircuit.antares.model.testcase.TestcaseViewController(
            controller.graphPanelViewController.editor,
            application.controller,
            controller.applicationContextHolder,
            controller.applicationModeHolder
        )

	private val testcasesView =
        _root_ide_package_.io.antarescircuit.antares.model.testcase.TestcaseViewSwing(testcaseViewController)

	private val testResultsPanel =
        _root_ide_package_.io.antarescircuit.antares.model.testcase.result.TestRunResultsPanel()

	private val netSignalApplierFailureHandler: io.antarescircuit.jabbah.base.event.EventHandler<io.antarescircuit.antares.model.NetSignalApplierFailure> = { handle(it) }

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