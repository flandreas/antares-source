package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import java.awt.event.ActionEvent

/**
 * Adds the current [MetaGraph] as a [ContainerLibraryElement] to the currently selected [LibraryDirectory].
 */
class AddGraphToLibraryAction(eventBus: EventBus) : AbstractLibraryFolderAction("library.action.addToLibrary", eventBus) {

    constructor(): this(BaseModule.eventBus)

    private var metaGraph: MetaGraph? = null

    init {
        eventBus.register(ApplicationDataEvent::class, {
            metaGraph = it.newData as MetaGraph
        })
    }

    override fun actionPerformed(e: ActionEvent?) {
        val directory = libraryTreeView!!.getSelectedItem() as LibraryDirectory
        directory.addContainerElement(metaGraph!!)
    }
}