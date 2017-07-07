package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.StorableCreator
import kotlin.reflect.KClass

interface LibraryDirectory : LibraryItem {

    fun add(item: LibraryItem)

    fun remove(item: LibraryItem)

    fun contains(item: LibraryItem): Boolean

    fun get(name: String): LibraryItem?

    fun getItems(): ImmutableList<LibraryItem>

    /** Creates a new [LibraryDirectory] with the specified name and adds it to this [LibraryDirectory].*/
    //fun addDirectory(name: String): LibraryDirectory

    fun addFolder(name: String): LibraryFolder

    /** Adds a new [ContainerLibraryElement] in this [LibraryDirectory] that contains the specified [MetaGraph].*/
    fun addContainerElement(metaGraph: MetaGraph): ContainerLibraryElement

    fun addBaseElement(
        name: String,
        translationKey: String,
        iconPath: String?,
        storableCreator: StorableCreator?,
        clazz: KClass<out GraphElementView<*>>
    ): BaseLibraryElement

    fun addBaseElement(
        name: String,
        translationKey: String,
        iconPath: String?,
        supplier: () -> GraphElementView<out GraphElement>
    ): BaseLibraryElement
}

/** Posted on [EventBus] when a [LibraryItem] has been added to a [LibraryDirectory].*/
data class LibraryItemAddedEvent(
    val parent: LibraryDirectory,
    val item: LibraryItem
)

/** Posted on [EventBus] when a [LibraryItem] has been removed from a [LibraryDirectory].*/
data class LibraryItemRemovedEvent(
    val parent: LibraryDirectory,
    val item: LibraryItem
)

/** Posted on [EventBus] when a [LibraryItem] has been updated in a [LibraryDirectory].*/
data class LibraryItemUpdatedEvent(
    val library: Library,
    val item: LibraryItem
)