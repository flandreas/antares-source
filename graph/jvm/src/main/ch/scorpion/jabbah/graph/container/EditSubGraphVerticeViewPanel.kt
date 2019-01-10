package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel


/**
 * A [JPanel] for editing the look of an individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor].
 */
class EditSubGraphVerticeViewPanel(
	private val metaGraphRepository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
    private val containerPanel: ContainerPanel,
    private val subGraphVerticeView: SubGraphVerticeView<*>,
    private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke()
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
			storableCloner: StorableCloner
		): Boolean {
			val panel = EditSubGraphVerticeViewPanel(metaGraphRepository, containerPanel, subGraphVerticeView, storableCloner)
			return JOptionPane.showConfirmDialog(
				parent,
				panel,
				Translations.getString("graph.action.editSubGraphVerticeView.name"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
			) == JOptionPane.OK_OPTION
		}
	}

	private val resetAction = ResetAction()

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
    }

    private fun fill() {
        val libraryGraph = metaGraphRepository.getMetaGraph(subGraphVerticeView.subGraphVertice!!.graphUUID!!)
        containerPanel.setData(
	        libraryGraph.graph.graphView,
            storableCloner.clone(subGraphVerticeView.getEditableContainerDrawing()) as ContainerDrawing)
    }

	private fun createToolBarPanel(): JPanel {
		val toolbarPanel = JPanel()
		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		containerPanel.createToolbars(separator = false).forEach { toolbarPanel.add(it) }
		toolbarPanel.add(createLocalToolbar())
		return toolbarPanel
	}

	private fun createLocalToolbar(): JPanel {
		val toolbar = ToolBar()
		toolbar.addSeparator()
		toolbar.add(JButton(ActionWrapperSwing(resetAction)))
		return toolbar
	}

	private fun reset() {
		LOG.debug("Resetting custom view of SubGraphVerticeView")
		subGraphVerticeView.setEditedContainerDrawing(null)
		fill()
	}

	private inner class ResetAction : AbstractAction("graph.action.resetCustomView") {
		override fun execute(event: ActionEvent) {
			if (JOptionPane.showConfirmDialog(
				this@EditSubGraphVerticeViewPanel,
				Translations.getString("graph.action.resetCustomView.msg"),
				name,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE

			) == JOptionPane.YES_OPTION) {
				reset()
			}
		}
	}
}