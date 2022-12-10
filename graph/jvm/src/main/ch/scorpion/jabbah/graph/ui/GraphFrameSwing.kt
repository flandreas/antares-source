package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.AbstractApplicationFrame
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.StatusBar
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.auth0.LoginLogoutAction
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.container.ContainerPanelSwing
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

	val loginLogoutAction = LoginLogoutAction()

	init {
		controller.view = this

		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		mainToolBar = createMainToolBar(actions.viewDesktopAction, actions.viewContainerAction)
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
		}
	}

	private fun showDesktop() {
		SwingUtilities.invokeLater {
			contentPane.removeAll()
			fillToolbarPanel(graphPanel.toolbars)
			contentPane.add(toolbarPanel, BorderLayout.NORTH)
			contentPane.add(graphPanel, BorderLayout.CENTER)
			contentPane.add(statusBar, BorderLayout.SOUTH)
			invalidate()
			revalidate()
			repaint()

			editor.view.initialize()

			controller.containerPanelController.active = false
		}
	}

	private fun showContainer() {
		SwingUtilities.invokeLater {
			contentPane.removeAll()
			fillToolbarPanel(containerPanel.toolbars)
			contentPane.add(toolbarPanel, BorderLayout.NORTH)
			contentPane.add(containerPanel, BorderLayout.CENTER)
			contentPane.add(statusBar, BorderLayout.SOUTH)
			invalidate()
			revalidate()
			repaint()

			containerPanel.initialize()

			viewManager.activeView = controller.containerPanelController.drawingView
			controller.containerPanelController.active = true
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

	private fun createMainToolBar(viewDesktopAction: Action, viewContainerAction: Action): ToolBar {
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

		return toolbar
	}

	private fun createCommonToolbar(): ToolBar {
		val toolbar = ToolBar()
		toolbar.isFloatable = false

		if (GraphModuleJvm.supportWeb) {
			toolbar.add(JButton(ActionWrapperSwing(loginLogoutAction)))
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

	private fun fillToolbarPanel(toolbars: List<ToolBar>) {
		toolbarPanel.removeAll()
		toolbarPanel.add(mainToolBar)
		toolbars.forEach { toolbarPanel.add(it) }
		toolbarPanel.add(Box.createHorizontalGlue())
		toolbarPanel.add(commonToolBar)
	}
}
