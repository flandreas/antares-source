package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.project.Project

interface BasicLibraryTreeView : UIView {

    fun openMainLibrary(library: Library)

    fun closeMainLibrary()

    fun refresh()

    fun reload()

    /**
     * Expand the tree to the node that contains the opened [ContainerLibraryElement].
     * This is primarily needed when the request originates from opening a [Project].
     */
    fun expandTo(element: ContainerLibraryElement)

    /**
     * Expands the tree to (and inclusive) the [LibraryFolder] with the specified english name.
     * Does nothing if the displayed [Library] doesn't contain such a folder.
     */
    fun expandFolder(folderName: String)

    fun expandToCurrentSavable()

    fun expandAllFromSelection()

    fun collapseAtSelection()
}

open class BasicLibraryTreeViewController<T: BasicLibraryTreeView>(
    val type: LibraryTreeViewType,
    library: Library?,
    val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<T>() {

    companion object {
        private val LOG by logger(BasicLibraryTreeViewController::class)
    }

    private val preferencesChangedHandler: EventHandler<PreferencesChangedEvent> = { view.refresh() }

    private val currentSavableHandler: EventHandler<CurrentSavableEvent> = {
        currentSavable = if (it.savable is AbstractLibraryItemSavable) {
            it.savable
        } else {
            null
        }
    }

    var currentSavable: Savable? = null
        private set(value) {
            if (field != value) {
                field = value
                view.refresh()
            }
        }

    /** Holds the [Library] to display.*/
    var library: Library? = library
        set(value) {
            if (field !== value) {
                LOG.debug("Set library")
                field = value
                if (field != null) {
                    view.openMainLibrary(value!!)
                } else {
                    view.closeMainLibrary()
                }
            }
        }

    /** Set by [LibraryTreeView] whenever the selection has changed. */
    var selectedItem: LibraryItem? = null
        set(value) {
            if (field !== value) {
                field = value
                LOG.trace("Selected TreeNode '${field.toString()}'")
                eventBus.post(LibrarySelectionChangedEvent(this))
            }
        }

    init {
        eventBus.register(PreferencesChangedEvent::class, preferencesChangedHandler)
        eventBus.register(CurrentSavableEvent::class, currentSavableHandler)
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(preferencesChangedHandler)
        eventBus.unregister(currentSavableHandler)
    }

    fun expandTo(metaGraph: UUID) {
        LibraryModule.libraryHolder.getContainerLibraryElement(metaGraph)?.let {
            view.expandTo(it)
        }
    }

    fun isCurrentItem(item: LibraryItem): Boolean =
        currentSavable is AbstractLibraryItemSavable && (currentSavable as AbstractLibraryItemSavable).item == item

    fun isDefaultElement(element: ContainerLibraryElement): Boolean =
        element.library?.defaultElementUUID == element.uuid

    protected fun displaysLibrary(library: Library?): Boolean =
        library != null && this.library?.expandedImports?.contains(library.uuid) == true
}