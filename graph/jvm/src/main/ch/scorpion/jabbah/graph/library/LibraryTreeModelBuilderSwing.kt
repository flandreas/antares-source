package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.project.Project
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/** Builds the [TreeModel] for displaying a [Library] and an optional [Project] as a tree.*/
class LibraryTreeModelBuilderSwing(
	private val library: Library,
	private val project: Project?,
	private val filter: LibraryFilter? =  null
) {
	companion object {

		private fun addItems(
			parentSwingNode: DefaultMutableTreeNode,
			parentNode: LibraryDirectoryTreeNode
		) {
			for (node in parentNode.children) {
				val swingNode = DefaultMutableTreeNode(node.item)
				if (node.children.size > 0) {
					addItems(swingNode, node)
				}
				parentSwingNode.add(swingNode)
			}
		}

		fun addLibrary(parentSwingNode: DefaultMutableTreeNode, library: Library, filter: LibraryFilter? = null) {
			addItems(parentSwingNode, LibraryDirectoryTreeModelBuilder(library, filter).build())
		}
	}

	fun build(): TreeModel {
		val root = DefaultMutableTreeNode(Translations.getString("graph.desktop.name"))

		project?.let {
			val node = DefaultMutableTreeNode(it)
			addLibrary(node, it, filter)
			root.add(node)
		}

		val node = DefaultMutableTreeNode(library)
		addLibrary(node, library, filter)
		root.add(node)

		return DefaultTreeModel(root)
	}
}