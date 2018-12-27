package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCloner
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JPanel


/**
 * A [JPanel] for editing the look of an individual [SubGraphVerticeView] by overwriting the standard
 * [ContainerDrawing] using a [ContainerEditor].
 */
class EditSubGraphVerticeViewPanel(
	private val metaGraphRepository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
    private val containerPanel: ContainerPanel,
    private val subGraphVerticeView: SubGraphVerticeView<*>,
    private val closeCallback: (EditSubGraphVerticeViewPanel) -> Unit,
    private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke()
) : JPanel() {

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
        add(containerPanel, BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        buttonPanel.add(JButton(OkAction()))
        buttonPanel.add(JButton(CancelAction()))
        add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun fill() {
        val libraryGraph = metaGraphRepository.getMetaGraph(subGraphVerticeView.subGraphVertice!!.graphUUID!!)
        containerPanel.setData(
	        libraryGraph.graph.graphView,
            storableCloner.clone(subGraphVerticeView.getEditableContainerDrawing()) as ContainerDrawing)
    }

    private inner class OkAction : AbstractAction(Translations.getString("base.action.ok.name")) {
        override fun actionPerformed(e: ActionEvent?) {
            subGraphVerticeView.setEditedContainerDrawing(containerPanel.editor.drawing as ContainerDrawing)
            closeCallback.invoke(this@EditSubGraphVerticeViewPanel)
        }
    }

    private inner class CancelAction : AbstractAction(Translations.getString("base.action.cancel.name")) {
        override fun actionPerformed(e: ActionEvent?) {
            closeCallback.invoke(this@EditSubGraphVerticeViewPanel)
        }
    }
}