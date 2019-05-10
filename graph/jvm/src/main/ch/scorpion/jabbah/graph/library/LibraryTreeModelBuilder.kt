package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.project.Project
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

/** Builds the [TreeModel] for displaying a [Library] and an optional [Project] as a tree.*/
class LibraryTreeModelBuilder(
	private val library: Library,
	private val project: Project?
) {
	companion object {

		/** Recursively creates [TreeNode]s for every item in [directory] and adds them to [parent].*/
		fun addItems(parent: DefaultMutableTreeNode, directory: LibraryDirectory) {
			for (item in directory.getItems()) {
				val node = DefaultMutableTreeNode(item)
				parent.add(node)
				if (item is LibraryDirectory) {
					addItems(node, item)
				}
			}
		}
	}

	fun build(): TreeModel {
		val root = DefaultMutableTreeNode(Translations.getString("graph.desktop.name"))

		if (project != null) {
			val projectNode = DefaultMutableTreeNode(project)
			addItems(projectNode, project)
			root.add(projectNode)
		}

		val libraryNode = DefaultMutableTreeNode(library)
		addItems(libraryNode, library)
		root.add(libraryNode)

		return DefaultTreeModel(root)
	}
}