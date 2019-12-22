package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.view.*
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.app.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.ui.CopyAction
import ch.scorpion.jabbah.graph.ui.CutAction
import ch.scorpion.jabbah.graph.ui.PasteAction
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*


/**
 * A [JPanel] for editing the look of an individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor].
 */
class EditSubGraphVerticeViewPanel(
	private val metaGraphRepository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val containerPanel: ContainerPanel,
	private val subGraphVerticeView: SubGraphVerticeView<*>,
	private val commandManager: CommandManager = EditModule.commandManager,
	private val closeHandler: (Boolean) -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(EditSubGraphVerticeViewPanel::class)

		/**
		 * Shows an [EditSubGraphVerticeViewPanel] within a modal dialog.
		 * @return `true` if the user confirmed his changed by clicking "OK", `false` if he clicked "Cancel".
		 */
		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			metaGraphRepository: MetaGraphRepository,
			containerPanel: ContainerPanel,
			subGraphVerticeView: SubGraphVerticeView<*>,
			commandManager: CommandManager
		): Boolean {
			val dialog = JDialog(parent, true)
			var okPressed = false
			BusyHandler.register(dialog, null)

			dialog.title = Translations.getString("graph.action.editSubGraphVerticeView.name")
			dialog.rootPane.jMenuBar = createMenuBar()
			dialog.contentPane.add(EditSubGraphVerticeViewPanel(
				metaGraphRepository,
				containerPanel,
				subGraphVerticeView
			) {
				okPressed = it
				dialog.isVisible = false
				dialog.dispose()
			})
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.addWindowListener(object : WindowAdapter() {
				override fun windowClosed(e: WindowEvent?) {
					BusyHandler.deregister(dialog)
				}
			})

			try {
				commandManager.openCheckpoint("subgraphContainerView")
				dialog.isVisible = true
			} finally {
				commandManager.closeCheckpoint()
			}

			return okPressed
		}

		private fun createMenuBar(): JMenuBar {
			val menuBar = JMenuBar()

			val editMenu = JMenu(Translations.getString("application.menu.edit"))
			editMenu.add(JMenuItem(ActionWrapperSwing(UndoAction())))
			editMenu.add(JMenuItem(ActionWrapperSwing(RedoAction())))
			editMenu.addSeparator()
			editMenu.add(JMenuItem(ActionWrapperSwing(DeleteAction())))
			editMenu.add(JMenuItem(ActionWrapperSwing(RotateAction())))
			editMenu.add(JMenuItem(ActionWrapperSwing(GroupComponentsAction())))
			editMenu.add(JMenuItem(ActionWrapperSwing(UngroupComponentsAction())))
			editMenu.addSeparator()
			editMenu.add(JMenuItem(ActionWrapperSwing(SelectAllAction())))
			editMenu.addSeparator()
			val arrangeMenu = JMenu(Translations.getString("edit.action.stackingOrder.name"))
			arrangeMenu.add(JMenuItem(ActionWrapperSwing(ToFrontAction())))
			arrangeMenu.add(JMenuItem(ActionWrapperSwing(OneUpAction())))
			arrangeMenu.add(JMenuItem(ActionWrapperSwing(OneDownAction())))
			arrangeMenu.add(JMenuItem(ActionWrapperSwing(ToBackAction())))
			editMenu.add(arrangeMenu)
			editMenu.addSeparator()
			editMenu.add(JMenuItem(ActionWrapperSwing(CutAction())))
			editMenu.add(JMenuItem(ActionWrapperSwing(CopyAction())))
			editMenu.add(JMenuItem(ActionWrapperSwing(PasteAction())))

			menuBar.add(editMenu)

			val viewMenu = JMenu(Translations.getString("application.menu.view"))
			viewMenu.add(JMenuItem(ActionWrapperSwing(ZoomInAction())))
			viewMenu.add(JMenuItem(ActionWrapperSwing(ZoomNormalAction())))
			viewMenu.add(JMenuItem(ActionWrapperSwing(ZoomOutAction())))
			viewMenu.add(JMenuItem(ActionWrapperSwing(ZoomCenterAction())))
			viewMenu.add(JMenuItem(ActionWrapperSwing(ZoomFitAction())))
			viewMenu.addSeparator()
			viewMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(GridAction())))
			menuBar.add(viewMenu)

			return menuBar
		}
	}

	init {
		buildUI()
	}

	fun initialize() {
		containerPanel.initialize()
	}

	private fun buildUI() {
		layout = BorderLayout()

		fill()

		containerPanel.preferredSize = Dimension(1000, 800)

		add(createToolBarPanel(), BorderLayout.NORTH)
		add(containerPanel, BorderLayout.CENTER)
		add(createButtonPanel(), BorderLayout.SOUTH)
	}

	private fun fill() {
		val libraryGraph = metaGraphRepository.getMetaGraph(subGraphVerticeView.subGraphVertice!!.graphUUID!!)
		containerPanel.setData(
			libraryGraph.graph.graphView,
			StorableCloner.clone(subGraphVerticeView.getEditableContainerDrawing()) as ContainerDrawing)
	}

	private fun createToolBarPanel(): JPanel {
		val toolbarPanel = JPanel()
		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		containerPanel.createToolbars(separator = false).forEach { toolbarPanel.add(it) }
		return toolbarPanel
	}

	private fun createButtonPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
		panel.add(Box.createHorizontalGlue())
		panel.add(JButton(ActionWrapperSwing(CancelAction())))
		panel.add(JButton(ActionWrapperSwing(OKAction())))
		panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		return panel
	}

	private inner class OKAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke(true)
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke(false)
		}
	}
}