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
    val type: io.antarescircuit.antares.LibraryTreeNodeType,
    val children: Array<LibraryTreeNodeJS>,
    val id: String?,
)

fun createLibraryTreeJS(library: io.antarescircuit.jabbah.graph.library.Library): io.antarescircuit.antares.LibraryTreeNodeJS =
    _root_ide_package_.io.antarescircuit.antares.createLibraryTreeNode(
        "Desktop",
        _root_ide_package_.io.antarescircuit.antares.LibraryTreeNodeType.Desktop,
        null,
        library.expandedImports.libraries
    )!!

private fun createLibraryTreeNode(item: io.antarescircuit.jabbah.graph.library.LibraryItem): io.antarescircuit.antares.LibraryTreeNodeJS? {
    return when (item) {
        is io.antarescircuit.jabbah.graph.library.Library -> {
            _root_ide_package_.io.antarescircuit.antares.createLibraryTreeNode(
                item.name.value,
                _root_ide_package_.io.antarescircuit.antares.LibraryTreeNodeType.Library,
                item.uuid.id,
                item.getItems()
            )
        }

        is io.antarescircuit.jabbah.graph.library.LibraryDirectory -> {
            _root_ide_package_.io.antarescircuit.antares.createLibraryTreeNode(
                item.name.value,
                _root_ide_package_.io.antarescircuit.antares.LibraryTreeNodeType.Folder,
                null,
                item.getItems()
            )
        }

        is io.antarescircuit.jabbah.graph.library.ContainerLibraryElement -> {
            _root_ide_package_.io.antarescircuit.antares.LibraryTreeNodeJS(
                _root_ide_package_.io.antarescircuit.jabbah.base.richtext.RichText.stripToPlainText(
                    item.name.value
                ),
                _root_ide_package_.io.antarescircuit.antares.LibraryTreeNodeType.MetaGraph,
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
    type: io.antarescircuit.antares.LibraryTreeNodeType,
    id: String?,
    items: List<io.antarescircuit.jabbah.graph.library.LibraryItem>
): io.antarescircuit.antares.LibraryTreeNodeJS? {
    val children = items.mapNotNull { _root_ide_package_.io.antarescircuit.antares.createLibraryTreeNode(it) }
    return if (type == _root_ide_package_.io.antarescircuit.antares.LibraryTreeNodeType.Folder && children.isEmpty()) {
        null
    } else {
        _root_ide_package_.io.antarescircuit.antares.LibraryTreeNodeJS(
            name,
            type,
            items.mapNotNull { _root_ide_package_.io.antarescircuit.antares.createLibraryTreeNode(it) }.toTypedArray(),
            id
        )
    }
}