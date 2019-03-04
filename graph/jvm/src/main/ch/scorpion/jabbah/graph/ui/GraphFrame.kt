package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.app.action.AbstractApplicationAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.container.ContainerPanel
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.BorderLayout
import javax.swing.*


/**
 * The main [AbstractApplicationFrame] of a graph [Application] that allows to switch between
 * a [GraphDesktop] and a [ContainerPanel] for editing the outside view of the main [GraphView].
 */
class GraphFrame(
	application: DesktopApplication,
	val graphPanel: GraphPanel,
	private val eventBus: EventBus,
	val viewManager: ViewManager
) : AbstractApplicationFrame(application) {

	enum class DisplayedView {
		Desktop, Container
	}

	private val desktopAction = ViewDesktopAction(application, eventBus)

	private val containerAction = ViewContainerAction(application, eventBus)

	private val mainToolBar: ToolBar

	private val statusBar = StatusBar()

	private val toolbarPanel: JPanel = JPanel()

	private var displayedView: DisplayedView = DisplayedView.Container

	private val containerPanel = ContainerPanel(GraphViewModule.containerEditorFactory.invoke(eventBus), viewManager)

	init {
		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		mainToolBar = createMainToolBar()

		showDesktop()
	}

	override val editor: Editor
		get() = graphPanel.editor

	override fun dispose() {
		super.dispose()
		graphPanel.dispose()
		containerPanel.dispose()
	}

	/** ---- [AbstractApplicationFrame] */

	/** The application data has changed if there are undoable [Command]s in the [CommandManager].*/
	override val applicationDataChanged: Boolean
		get() = graphPanel.editor.commandManager.applicationDataChanged

	/** ---- [GraphFrame] */

	fun showDesktop() {
		if (displayedView == DisplayedView.Desktop) {
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

			displayedView = DisplayedView.Desktop
			viewManager.activeView = graphPanel.editor.view
			eventBus.post(GraphFrameEvent(this, displayedView))
		}
	}

	fun showContainer() {
		if (displayedView == DisplayedView.Container) {
			return
		}

		SwingUtilities.invokeLater {
			contentPane.removeAll()
			fillToolbarPanel(containerPanel.createToolbars())
			contentPane.add(toolbarPanel, BorderLayout.NORTH)
			contentPane.add(containerPanel, BorderLayout.CENTER)
			contentPane.add(statusBar, BorderLayout.SOUTH)
			invalidate()
			revalidate()
			repaint()

			containerPanel.initialize()

			displayedView = DisplayedView.Container
			viewManager.activeView = containerPanel.editor.view
			containerPanel.activated()
			eventBus.post(GraphFrameEvent(this, displayedView))
		}
	}

	private fun createMainToolBar(): ToolBar {
		val toolbar = ToolBar()
		toolbar.isFloatable = false

		val viewDesktopButton = JToggleButton(ActionWrapperSwing(desktopAction))
		viewDesktopButton.icon = ImageIcon(GraphFrame::class.java.getResource("/img/drawing-24.png"))
		viewDesktopButton.text = null
		viewDesktopButton.toolTipText = Translations.getString("graph.action.showDesktop.name")
		viewDesktopButton.addActionListener(MainToolBarActionListener(viewDesktopButton, DisplayedView.Desktop))
		toolbar.add(viewDesktopButton)

		val viewContainerButton = JToggleButton(ActionWrapperSwing(containerAction))
		viewContainerButton.icon = ImageIcon(GraphFrame::class.java.getResource("/img/container-24.png"))
		viewContainerButton.text = null
		viewContainerButton.toolTipText = Translations.getString("graph.action.showContainer.name")
		viewContainerButton.addActionListener(MainToolBarActionListener(viewContainerButton, DisplayedView.Container))
		toolbar.add(viewContainerButton)

		return toolbar
	}

	/**
	 * Establishes a [JToggleButton] with [JRadioButton] behaviour (i.e. cannot be deselected)
	 * by listening for [java.awt.event.ActionEvent]s and selecting it again, if necessary.
	 */
	private inner class MainToolBarActionListener(private val button: JToggleButton, private val targetDisplayedView: DisplayedView) : java.awt.event.ActionListener {

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

	private inner class ViewDesktopAction(
		app: DesktopApplication,
		eventBus: EventBus
	) : AbstractApplicationAction("view.action.desktop", app) {

		init {
			eventBus.register(GraphFrameEvent::class) { update() }
			eventBus.register(ApplicationModeEvent::class) { update() }
			update()
		}

		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			showDesktop()
		}

		private fun update() {
			selected = displayedView == DisplayedView.Desktop
			enabled = graphPanel.currentMode.isEdit()
		}
	}

	private inner class ViewContainerAction(
		app: DesktopApplication,
		eventBus: EventBus
	) : AbstractApplicationAction("view.action.container", app) {

		init {
			eventBus.register(GraphFrameEvent::class) { update() }
			eventBus.register(ApplicationModeEvent::class) { update() }
			update()
		}

		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			showContainer()
		}

		private fun update() {
			selected = displayedView == DisplayedView.Container
			enabled = graphPanel.currentMode.isEdit()
		}
	}
}

/** Posted by [GraphFrame] on [EventBus] when [GraphFrame.DisplayedView] has changed.*/
data class GraphFrameEvent(val graphFrame: GraphFrame, val displayedView: GraphFrame.DisplayedView)