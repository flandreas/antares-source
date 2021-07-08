package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.draw.view.*
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.app.*
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.StorableCloner
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*


/**
 * A [JPanel] for editing the look of an individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor].
 */
class EditSubGraphVerticeViewPanel(
	private val metaGraphRepository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val containerPanel: ContainerPanel,
	private val subGraphVerticeView: SubGraphVerticeView<*>,
	private val closeHandler: (Boolean) -> Unit
) : JPanel() {

	companion object {

		private val LOG by logger(EditSubGraphVerticeViewPanel::class)
		private val actions = mutableListOf<Action>()

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
			var okPressed = false
			actions.clear()

			var dialog = DialogBuilder<EditSubGraphVerticeViewPanel>(parent)
				.content { dialog ->
					EditSubGraphVerticeViewPanel(metaGraphRepository, containerPanel, subGraphVerticeView) {
						okPressed = true
						dialog.dispose()
					}
				}
				.title(Translations.getString("graph.action.editSubGraphVerticeView.name"))
				.defaultButton { it.cancelButton }
				.menu(createMenuBar())
				.resizable()

			try {
				commandManager.openCheckpoint("subgraphContainerView")
				dialog.show()
			} finally {
				commandManager.closeCheckpoint()
				actions.forEach { it.dispose() }
			}

			return okPressed
		}

		private fun createMenuBar(): JMenuBar {
			val menuBar = JMenuBar()

			val editMenu = JMenu(Translations.getString("application.menu.edit"))
			editMenu.add(JMenuItem(ActionWrapperSwing(register(UndoAction()))))
			editMenu.add(JMenuItem(ActionWrapperSwing(register(RedoAction()))))
			editMenu.addSeparator()
			editMenu.add(JMenuItem(ActionWrapperSwing(register(DeleteAction()))))
			editMenu.add(JMenuItem(ActionWrapperSwing(register(RotateAction()))))
			editMenu.add(JMenuItem(ActionWrapperSwing(register(GroupComponentsAction()))))
			editMenu.add(JMenuItem(ActionWrapperSwing(register(UngroupComponentsAction()))))
			editMenu.addSeparator()
			editMenu.add(JMenuItem(ActionWrapperSwing(register(SelectAllAction()))))
			editMenu.addSeparator()
			val arrangeMenu = JMenu(Translations.getString("edit.action.stackingOrder.name"))
			arrangeMenu.add(JMenuItem(ActionWrapperSwing(register(ToFrontAction()))))
			arrangeMenu.add(JMenuItem(ActionWrapperSwing(register(OneUpAction()))))
			arrangeMenu.add(JMenuItem(ActionWrapperSwing(register(OneDownAction()))))
			arrangeMenu.add(JMenuItem(ActionWrapperSwing(register(ToBackAction()))))
			editMenu.add(arrangeMenu)
			editMenu.addSeparator()
			editMenu.add(JMenuItem(ActionWrapperSwing(register(CutAction()))))
			editMenu.add(JMenuItem(ActionWrapperSwing(register(CopyAction()))))
			editMenu.add(JMenuItem(ActionWrapperSwing(register(PasteAction()))))

			menuBar.add(editMenu)

			val viewMenu = JMenu(Translations.getString("application.menu.view"))
			viewMenu.add(JMenuItem(ActionWrapperSwing(register(ZoomInAction()))))
			viewMenu.add(JMenuItem(ActionWrapperSwing(register(ZoomNormalAction()))))
			viewMenu.add(JMenuItem(ActionWrapperSwing(register(ZoomOutAction()))))
			viewMenu.add(JMenuItem(ActionWrapperSwing(register(ZoomCenterAction()))))
			viewMenu.add(JMenuItem(ActionWrapperSwing(register(ZoomFitAction()))))
			viewMenu.addSeparator()
			viewMenu.add(JCheckBoxMenuItem(ActionWrapperSwing(register(GridAction()))))
			menuBar.add(viewMenu)

			return menuBar
		}

		private fun register(action: Action): Action {
			actions.add(action)
			return action
		}
	}

	val cancelButton = JButton(ActionWrapperSwing(CancelAction()))

	init {
		buildUI()
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
			graphView = libraryGraph.graph.graphView,
			containerDrawing = StorableCloner.clone(subGraphVerticeView.getEditableContainerDrawing()),
			editable = true)
	}

	private fun createToolBarPanel(): JPanel {
		val toolbarPanel = JPanel()
		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		GraphViewModuleJvm.containerToolBarBuilderFactory().buildToolBars(containerPanel.editor, separator = false).forEach { toolbarPanel.add(it) }
		return toolbarPanel
	}

	private fun createButtonPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
		panel.add(Box.createHorizontalGlue())
		panel.add(JButton(ActionWrapperSwing(OKAction())))
		panel.add(Box.createHorizontalStrut(2))
		panel.add(cancelButton)
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