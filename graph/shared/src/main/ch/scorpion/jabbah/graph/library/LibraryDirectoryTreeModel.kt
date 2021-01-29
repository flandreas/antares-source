package ch.scorpion.jabbah.graph.library

/** Determines whether a [LibraryItem] should be contained in a [LibraryDirectoryTreeNode]. */
typealias LibraryFilter = (LibraryItem) -> Boolean

class LibraryDirectoryTreeNode(
	val item: LibraryItem,
	val children: MutableList<LibraryDirectoryTreeNode> = mutableListOf()
)

/**
 * Builds a platform-independent tree representation of a [LibraryDirectory].
 *
 * @param directory the [LibraryDirectory] to be represented
 * @param filter allows to filter [LibraryDirectoryTreeNode]s of interest. The resulting tree contains only
 * [LibraryItem]s that pass this filter, including all parents needed to maintain the tree structure of
 * the filtered [LibraryItem]s
 */
class LibraryDirectoryTreeModelBuilder(
	val directory: LibraryDirectory,
	val filter: LibraryFilter? = null
) {

	/** Builds the tree for [directory] and returns the root node of the built tree.*/
	fun build(): LibraryDirectoryTreeNode {
		val node = LibraryDirectoryTreeNode(directory)
		addItems(node, directory)
		return node
	}

	private fun addItems(parent: LibraryDirectoryTreeNode, directory: LibraryDirectory) {
		for (item in directory.getItems()) {
			if ((item is LibraryFolder) || filter == null || filter.invoke(item)) {
				val node = LibraryDirectoryTreeNode(item)
				if (item is LibraryDirectory) {
					addItems(node, item)
				}
				if (filter == null || (item !is LibraryFolder) || node.children.size > 0) {
					parent.children.add(node)
				}
			}
		}
	}
}