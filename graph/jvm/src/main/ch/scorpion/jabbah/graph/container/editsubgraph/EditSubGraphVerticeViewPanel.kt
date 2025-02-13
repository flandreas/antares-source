package ch.scorpion.jabbah.graph.container.editsubgraph

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.UndoableDataHolder
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ContainerEditor
import ch.scorpion.jabbah.graph.container.ContainerPanelSwing
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCloner
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel


/**
 * A [JPanel] for editing the look of an individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor].
 */
class EditSubGraphVerticeViewPanel(
	private val metaGraphRepository: MetaGraphRepository = LibraryModule.libraryHolder,
	private val containerPanel: ContainerPanelSwing,
	private val subGraphVerticeView: SubGraphVerticeView<*>,
	private var closeHandler: () -> Unit = {}
) : JPanel(), UndoableDataHolder {

	companion object {

		/**
		 * Shows an [EditSubGraphVerticeViewPanel] within a modal dialog.
		 * @return the edited [ContainerDrawing] if the user closed the dialog with OK, `null` otherwise
		 */
		fun showAsDialog(
			title: String,
			parent: Frame = Frame.getFrames()[0],
			metaGraphRepository: MetaGraphRepository,
			containerPanel: ContainerPanelSwing,
			subGraphVerticeView: SubGraphVerticeView<*>,
			commandManager: CommandManager
		): ContainerDrawing? {
			val panel = EditSubGraphVerticeViewPanel(metaGraphRepository, containerPanel, subGraphVerticeView)
			val dialog = DialogBuilder<EditSubGraphVerticeViewPanel>(parent)
				.content { dialog ->
					panel.closeHandler = { dialog.dispose() }
					panel
				}
				.title(title)
				.defaultButton { it.cancelButton }
				.menu(panel.menuBar)
				.resizable()

			try {
				commandManager.openCheckpoint("subgraphContainerView", panel)
				dialog.show()
			} finally {
				commandManager.closeCheckpoint()
				panel.dispose()
			}

			return if (panel.okPressed) {
				panel.containerPanel.controller.editor.drawing as ContainerDrawing
			} else {
				null
			}
		}
	}

	val cancelButton = JButton(ActionWrapperSwing(CancelAction()))

	val menuBar = EditSubGraphVerticeViewMenu()

	var okPressed: Boolean = false
		private set

	init {
		buildUI()
	}

	fun dispose() {
		menuBar.dispose()
	}

	/** ---- [UndoableDataHolder] interface*/

	override fun getUndoableState(): Storable =
		containerPanel.controller.editor.drawing

	override fun setUndoableState(state: Storable) {}

	override fun undoableStateEstablished(state: Storable) {
		containerPanel.controller.updateData(state as ContainerDrawing)
	}

	/** ---- [EditSubGraphVerticeViewPanel] */

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
		containerPanel.controller.setData(
			graphView = libraryGraph.graph.graphView,
			containerDrawing = StorableCloner.clone(subGraphVerticeView.getEditableContainerDrawing()),
			editable = true,
			isManualContainer = true)
	}

	private fun createToolBarPanel(): JPanel {
		val toolbarPanel = JPanel()
		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		GraphViewModuleJvm.containerToolBarBuilderFactory().buildToolBars(null, containerPanel.controller.editor, separator = false).forEach { toolbarPanel.add(it) }
		return toolbarPanel
	}

	private fun createButtonPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
		panel.border = UIBasics.createDialogBorder()
		panel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(panel, JButton(ActionWrapperSwing(OKAction())), cancelButton)

		return panel
	}

	private inner class OKAction : AbstractAction("base.action.ok") {
		override fun execute(event: ActionEvent) {
			okPressed = true
			closeHandler.invoke()
		}
	}

	private inner class CancelAction : AbstractAction("base.action.cancel") {
		override fun execute(event: ActionEvent) {
			okPressed = false
			closeHandler.invoke()
		}
	}

}