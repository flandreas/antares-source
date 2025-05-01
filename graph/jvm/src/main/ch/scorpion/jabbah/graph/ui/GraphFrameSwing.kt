package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.AbstractApplicationFrame
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.StatusBar
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.container.ContainerPanelSwing
import ch.scorpion.jabbah.graph.ui.documentation.DocumentationPanelSwing
import ch.scorpion.jabbah.graph.login.LoginLogoutAction
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import java.awt.BorderLayout
import javax.swing.*


/**
 * A Java Swing implementation of the [GraphFrame] interface as an [AbstractApplicationFrame].
 */
open class GraphFrameSwing(
	val controller: GraphFrameController<GraphFrame>,
	application: DesktopApplication,
	val viewManager: ContentViewManager,
	val actions: GraphFrameActions
) : AbstractApplicationFrame(application), GraphFrame {

	/** Contains the buttons for switching between [graphPanel] and [containerPanel]. */
	private val mainToolBar: ToolBar

	/** Contains the tools displayed for both [graphPanel] and [containerPanel]. */
	private val commonToolBar: ToolBar

	private val statusBar = StatusBar()

	private val toolbarPanel: JPanel = JPanel()

	val graphPanel = GraphPanelViewSwing(controller.graphPanelViewController, viewManager = viewManager, application = application)

	private val containerPanel = ContainerPanelSwing(controller.containerPanelController, application)

	private val documentationPanel = DocumentationPanelSwing(controller.documentationPanelController, application)

	val loginLogoutAction = LoginLogoutAction()

	init {
		controller.view = this

		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		mainToolBar = createMainToolBar(actions.viewDesktopAction, actions.viewContainerAction, actions.viewDocumentationAction)
		commonToolBar = createCommonToolbar()
	}

	/** ---- [GraphFrame] */

	override val applicationMode: ApplicationMode get() = controller.applicationModeHolder.currentMode

	override val desktopView: View<*> get() = editor.view

	override val containerView: View<*> get() = controller.containerPanelController.drawingView

	override val desktopViewShowsNavigationRoot: Boolean get() = graphPanel.showsNavigationRoot

	override fun notifyDisplayedView() {
		when (controller.displayedView) {
			GraphFrameController.DisplayedView.Desktop -> showDesktop()
			GraphFrameController.DisplayedView.Container -> showContainer()
			GraphFrameController.DisplayedView.Documentation -> showDocumentation()
		}
	}

	private fun fillContentPane(toolbars: List<JComponent>, mainPanel: JComponent) {
		contentPane.removeAll()
		fillToolbarPanel(toolbars)
		contentPane.add(toolbarPanel, BorderLayout.NORTH)
		contentPane.add(mainPanel, BorderLayout.CENTER)
		contentPane.add(statusBar, BorderLayout.SOUTH)
		invalidate()
		revalidate()
		repaint()
	}

	private fun showDesktop() {
		SwingUtilities.invokeLater {
			fillContentPane(graphPanel.toolbars, graphPanel)

			editor.view.initialize()

			if (controller.graphPanelViewController.desktopController.mainDesktopViewItem == null) {
				viewManager.activeView = null
				controller.graphPanelViewController.editor.active = false
			} else {
				viewManager.activeView = controller.graphPanelViewController.view.graphEditView
				controller.graphPanelViewController.editor.active = true
			}
			controller.containerPanelController.active = false
		}
	}

	private fun showContainer() {
		SwingUtilities.invokeLater {
			fillContentPane(containerPanel.toolbars, containerPanel)

			containerPanel.initialize()

			viewManager.activeView = controller.containerPanelController.drawingView
			controller.graphPanelViewController.editor.active = false
			controller.containerPanelController.active = true
		}
	}

	private fun showDocumentation() {
		SwingUtilities.invokeLater {
			fillContentPane(documentationPanel.toolbars, documentationPanel)
			viewManager.activeView = null
			controller.graphPanelViewController.editor.active = false
			controller.containerPanelController.active = false
		}
	}

	/** ---- [AbstractApplicationFrame] */

	override val editor: Editor get() = controller.graphPanelViewController.editor

	override fun dispose() {
		super.dispose()
		graphPanel.dispose()
		containerPanel.dispose()
		statusBar.dispose()
	}

	/** The application data has changed if there are undoable [Command]s in the [CommandManager].*/
	override val applicationDataChanged: Boolean
		get() = editor.commandManager.canUndo()

	/** ---- [GraphFrameSwing] */

	private fun createMainToolBar(
		viewDesktopAction: Action,
		viewContainerAction: Action,
		viewDocumentationAction: Action
	): ToolBar {
		val toolbar = ToolBar()
		toolbar.isFloatable = false

		val viewDesktopButton = JToggleButton(ActionWrapperSwing(viewDesktopAction))
		viewDesktopAction.imagePath?.let { viewDesktopButton.icon = UiUtil.themedIcon(it) }
		viewDesktopButton.text = null
		viewDesktopButton.toolTipText = viewDesktopAction.name
		viewDesktopButton.addActionListener(MainToolBarActionListener(viewDesktopButton, GraphFrameController.DisplayedView.Desktop))
		toolbar.add(viewDesktopButton)

		val viewContainerButton = JToggleButton(ActionWrapperSwing(viewContainerAction))
		viewContainerAction.imagePath?.let { viewContainerButton.icon = UiUtil.themedIcon(it) }
		viewContainerButton.text = null
		viewContainerButton.toolTipText = viewContainerAction.name
		viewContainerButton.addActionListener(MainToolBarActionListener(viewContainerButton, GraphFrameController.DisplayedView.Container))
		toolbar.add(viewContainerButton)

		val viewDocumentationButton = JToggleButton(ActionWrapperSwing(viewDocumentationAction))
		viewDocumentationAction.imagePath?.let { viewDocumentationButton.icon = UiUtil.themedIcon(it) }
		viewDocumentationButton.text = null
		viewDocumentationButton.toolTipText = viewDocumentationAction.name
		viewDocumentationButton.addActionListener(MainToolBarActionListener(viewDocumentationButton, GraphFrameController.DisplayedView.Documentation))
		toolbar.add(viewDocumentationButton)

		return toolbar
	}

	private fun createCommonToolbar(): ToolBar {
		val toolbar = ToolBar()
		toolbar.isFloatable = false

		if (GraphModuleJvm.supportWeb) {
			toolbar.add(JButton(ActionWrapperSwing(loginLogoutAction, suppressOpenDialogIndicator = true)))
		}

		return toolbar
	}

	/**
	 * Establishes a [JToggleButton] with [JRadioButton] behaviour (i.e. cannot be deselected)
	 * by listening for [java.awt.event.ActionEvent]s and selecting it again, if necessary.
	 */
	private inner class MainToolBarActionListener(private val button: JToggleButton, private val targetDisplayedView: GraphFrameController.DisplayedView) : java.awt.event.ActionListener {
		override fun actionPerformed(e: java.awt.event.ActionEvent?) {
			if (!button.isSelected && controller.displayedView == targetDisplayedView) {
				button.doClick()
				button.requestFocus()
			}
		}
	}

	private fun fillToolbarPanel(toolbars: List<JComponent>) {
		toolbarPanel.removeAll()
		toolbarPanel.add(mainToolBar)
		toolbars.forEach { toolbarPanel.add(it) }
		toolbarPanel.add(Box.createHorizontalGlue())
		toolbarPanel.add(commonToolBar)
	}
}
