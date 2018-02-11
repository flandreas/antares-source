package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import javax.swing.Action

/**
 * Abstract base class for implementing [Action]s that are enabled when a [LibraryDirectory]
 * is currently selected in the [LibraryTreeView].
 */
abstract class AbstractLibraryFolderAction(
        actionBaseName: String,
        eventBus: EventBus
) : AbstractAction(actionBaseName) {

    protected constructor(actionBaseName: String): this(actionBaseName, BaseModule.eventBus)

    protected var libraryTreeView: LibraryTreeView? = null
        private set

    init {
        enabled = false
        eventBus.register(LibrarySelectionChangedEvent::class, {
            libraryTreeView = it.libraryTreeView
            enabled = libraryTreeView!!.getSelectedItem() is LibraryDirectory
        })
    }
}