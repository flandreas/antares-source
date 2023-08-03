package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.edit.model.text.NamableTreeNode
import ch.scorpion.jabbah.graph.project.Project
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/** Builds the [TreeModel] for displaying a [Library] and an optional [Project] as a tree.*/
class LibraryTreeModelBuilderSwing(
	private val library: Library?,
	private val filter: LibraryFilter? =  null
) {
	companion object {

		private fun addItems(
			parentSwingNode: DefaultMutableTreeNode,
			parentNode: LibraryTreeNode,
			font: Font
		) {
			for (node in parentNode.children) {
				val swingNode = NamableTreeNode(node.item, font)
				if (node.children.size > 0) {
					addItems(swingNode, node, font)
				}
				parentSwingNode.add(swingNode)
			}
		}

		private fun addLibrary(
			parentSwingNode: NamableTreeNode,
			font: Font,
			library: Library,
			filter: LibraryFilter? = null
		) {
			addItems(parentSwingNode, LibraryDirectoryTreeModelBuilder(library, filter).build(), font)
		}
	}

	fun build(font: Font): TreeModel {
		val root = DefaultMutableTreeNode(Translations.getString("graph.desktop.name"))

		if (library != null) {
			for (l in library.expandedImports.libraries) {
				val node = NamableTreeNode(l, font)
				addLibrary(node, font, l, filter)
				root.add(node)
			}
		}

		return DefaultTreeModel(root)
	}
}