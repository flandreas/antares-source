package io.antarescircuit.jabbah.graph.library

/** Determines whether a [LibraryItem] should be contained in a [LibraryTreeNode]. */
typealias LibraryFilter = (LibraryItem) -> Boolean

class LibraryTreeNode(
	val item: LibraryItem,
	val children: MutableList<LibraryTreeNode> = mutableListOf()
)

/**
 * Builds a platform-independent tree representation of a [LibraryDirectory].
 *
 * @param directory the [LibraryDirectory] to be represented
 * @param filter allows to filter [LibraryTreeNode]s of interest. The resulting tree contains only
 * [LibraryItem]s that pass this filter, including all parents needed to maintain the tree structure of
 * the filtered [LibraryItem]s
 */
class LibraryDirectoryTreeModelBuilder(
	val directory: LibraryDirectory,
	val filter: LibraryFilter? = null
) {

	/** Builds the tree for [directory] and returns the root node of the built tree.*/
	fun build(): LibraryTreeNode {
		val node = LibraryTreeNode(directory)
		addItems(node, directory)
		return node
	}

	private fun addItems(parent: LibraryTreeNode, directory: LibraryDirectory) {
		for (item in directory.getItems()) {
			if ((item is LibraryFolder) || filter == null || filter.invoke(item)) {
				val node = LibraryTreeNode(item)
				if (item is LibraryDirectory) {
					addItems(node, item)
				}
				if (filter == null || (item !is LibraryFolder) || node.children.isNotEmpty()) {
					parent.children.add(node)
				}
			}
		}
	}
}