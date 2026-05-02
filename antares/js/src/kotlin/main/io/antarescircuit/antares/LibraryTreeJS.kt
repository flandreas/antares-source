package io.antarescircuit.antares

import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.library.LibraryItem

@JsExport
enum class LibraryTreeNodeType {
    Desktop,
    Project,
    Library,
    Folder,
    MetaGraph
}

@JsExport
data class LibraryTreeNodeJS(
    val name: String,
    val type: LibraryTreeNodeType,
    val children: Array<LibraryTreeNodeJS>,
    val id: String?,
)

fun createLibraryTreeJS(library: Library): LibraryTreeNodeJS =
    createLibraryTreeNode(
        "Desktop",
        LibraryTreeNodeType.Desktop,
        null,
        library.expandedImports.libraries
    )!!

private fun createLibraryTreeNode(item: LibraryItem): LibraryTreeNodeJS? {
    return when (item) {
        is Library -> {
            createLibraryTreeNode(
                item.name.value,
                LibraryTreeNodeType.Library,
                item.uuid.id,
                item.getItems()
            )
        }

        is LibraryDirectory -> {
            createLibraryTreeNode(
                item.name.value,
                LibraryTreeNodeType.Folder,
                null,
                item.getItems()
            )
        }

        is ContainerLibraryElement -> {
            LibraryTreeNodeJS(
                RichText.stripToPlainText(
                    item.name.value
                ),
                LibraryTreeNodeType.MetaGraph,
                emptyArray(),
                item.uuid.id
            )
        }

        else -> {
            null
        }
    }
}

private fun createLibraryTreeNode(
    name: String,
    type: LibraryTreeNodeType,
    id: String?,
    items: List<LibraryItem>
): LibraryTreeNodeJS? {
    val children = items.mapNotNull { createLibraryTreeNode(it) }
    return if (type == LibraryTreeNodeType.Folder && children.isEmpty()) {
        null
    } else {
        LibraryTreeNodeJS(
            name,
            type,
            items.mapNotNull { createLibraryTreeNode(it) }.toTypedArray(),
            id
        )
    }
}