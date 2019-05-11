package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.project.Project
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode

/** Builds the [TreeModel] for displaying a [Library] and an optional [Project] as a tree.*/
class LibraryTreeModelBuilder(
	private val library: Library,
	private val project: Project?,
	private val filter: ((LibraryItem) -> Boolean)? =  null
) {
	companion object {

		/** Recursively creates [TreeNode]s for every matching item in [directory] and adds them to [parent].*/
		fun addItems(
			parent: DefaultMutableTreeNode,
			directory: LibraryDirectory,
			filter: ((LibraryItem) -> Boolean)? = null
		) {
			for (item in directory.getItems()) {
				if ((item is LibraryFolder) || filter == null || filter.invoke(item)) {
					val node = DefaultMutableTreeNode(item)
					if (item is LibraryDirectory) {
						addItems(node, item, filter)
					}
					if (filter == null || (item !is LibraryFolder) || node.childCount > 0) {
						parent.add(node)
					}
				}
			}
		}
	}

	fun build(): TreeModel {
		val root = DefaultMutableTreeNode(Translations.getString("graph.desktop.name"))

		if (project != null) {
			val projectNode = DefaultMutableTreeNode(project)
			addItems(projectNode, project, filter)
			root.add(projectNode)
		}

		val libraryNode = DefaultMutableTreeNode(library)
		addItems(libraryNode, library, filter)
		root.add(libraryNode)

		return DefaultTreeModel(root)
	}
}