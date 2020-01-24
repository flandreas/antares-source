package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import java.awt.Frame
import javax.swing.JOptionPane

/**
 * Creates a new [ContainerLibraryElement] with an empty [MetaGraph] as a child of the currently selected [LibraryDirectory].
 */
class NewGraphAction(
	libraryTreeView: LibraryTreeView,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction("library.action.newGraph", libraryTreeView, eventBus) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    val name = JOptionPane.showInputDialog(
		    Frame.getFrames()[0],
		    Translations.getString("library.action.newGraph.question"),
		    name,
		    JOptionPane.QUESTION_MESSAGE
	    )
	    if (StringUtils.isEmpty(name)) {
		    return
	    }
        val directory = libraryTreeView.getSelectedItem() as LibraryDirectory
	    val library = directory.library!!
	    val metaGraph = MetaGraph.withName(name)

	    val element = library.libraryService.addContainerLibraryElement(library, metaGraph, directory)
		eventBus.post(OpenContainerLibraryElementRequest(element))
    }

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser && super.calculateEnabledness()
	}
}
