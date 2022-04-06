package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.AbstractApplicationFrame
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.StatusBar
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ContainerPanelSwing
import ch.scorpion.jabbah.graph.container.ContainerToolBarBuilder
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.BorderLayout
import javax.swing.*


/**
 * A Java Swing implementation of the [GraphFrame] interface as an [AbstractApplicationFrame].
 */
open class GraphFrameSwing(
	val controller: GraphFrameController<GraphFrame>,
	application: DesktopApplication,
	private val eventBus: EventBus,
	val viewManager: ContentViewManager,
	val actions: GraphFrameActions
) : AbstractApplicationFrame(application), GraphFrame {

	private val mainToolBar: ToolBar

	private val statusBar = StatusBar()

	private val toolbarPanel: JPanel = JPanel()

	val graphPanel = GraphPanelViewSwing(controller.graphPanelViewController, viewManager = viewManager, application = application)

	private val containerDrawingView = EditModule.drawingViewFactory.create(ContainerDrawing(), controller.applicationContextHolder, displayGlobalMessages = true)

	private val containerPanel = ContainerPanelSwing(application, GraphViewModule.containerEditorFactory.invoke(containerDrawingView), viewManager)

	init {
		controller.view = this

		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		mainToolBar = createMainToolBar(actions.viewDesktopAction, actions.viewContainerAction)

		showDesktop()
	}

	/** ---- [GraphFrame] */

	override var displayedView: GraphFrame.DisplayedView = GraphFrame.DisplayedView.Container

	override val applicationMode: ApplicationMode get() = controller.applicationModeHolder.currentMode

	override val desktopView: View<*> get() = editor.view

	override val containerView: View<*> get() = containerPanel.editor.view

	override val desktopViewShowsNavigationRoot: Boolean get() = graphPanel.showsNavigationRoot

	override fun showDesktop() {
		if (displayedView == GraphFrame.DisplayedView.Desktop) {
			return
		}
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

			displayedView = GraphFrame.DisplayedView.Desktop
			containerPanel.active = false
			eventBus.post(GraphFrameEvent(this, displayedView))
		}
	}

	override fun showContainer() {
		if (displayedView == GraphFrame.DisplayedView.Container) {
			return
		}

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

			displayedView = GraphFrame.DisplayedView.Container
			viewManager.activeView = containerPanel.editor.view
			containerPanel.active = true
			eventBus.post(GraphFrameEvent(this, displayedView))
		}
	}

	protected open fun createContainerToolBarBuilder(): ContainerToolBarBuilder = ContainerToolBarBuilder()

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
		viewDesktopButton.addActionListener(MainToolBarActionListener(viewDesktopButton, GraphFrame.DisplayedView.Desktop))
		toolbar.add(viewDesktopButton)

		val viewContainerButton = JToggleButton(ActionWrapperSwing(viewContainerAction))
		viewContainerAction.imagePath?.let { viewContainerButton.icon = UiUtil.themedIcon(it) }
		viewContainerButton.text = null
		viewContainerButton.toolTipText = viewContainerAction.name
		viewContainerButton.addActionListener(MainToolBarActionListener(viewContainerButton, GraphFrame.DisplayedView.Container))
		toolbar.add(viewContainerButton)

		return toolbar
	}

	/**
	 * Establishes a [JToggleButton] with [JRadioButton] behaviour (i.e. cannot be deselected)
	 * by listening for [java.awt.event.ActionEvent]s and selecting it again, if necessary.
	 */
	private inner class MainToolBarActionListener(private val button: JToggleButton, private val targetDisplayedView: GraphFrame.DisplayedView) : java.awt.event.ActionListener {
		override fun actionPerformed(e: java.awt.event.ActionEvent?) {
			if (!button.isSelected && displayedView == targetDisplayedView) {
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
	}
}
