package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.edit.model.text.NamableTreeNode
import ch.scorpion.jabbah.graph.project.Project
import javax.swing.UIManager
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

/** Builds the [TreeModel] for displaying a [Library] and an optional [Project] as a tree.*/
class LibraryTreeModelBuilderSwing(
	private val library: Library?,
	private val filter: LibraryFilter? =  null,
	private val includeImports: Boolean = true
) {
	companion object {

		private fun addItems(
			parentSwingNode: DefaultMutableTreeNode,
			parentNode: LibraryTreeNode,
			font: Font
		) {
			for (node in parentNode.children) {
				val swingNode = NamableTreeNode(node.item, font)
				if (node.children.isNotEmpty()) {
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

	private var font: Font = Graphics2DJvm.fromAwtFont(UIManager.getFont("Tree.font"))

	fun withFont(font: Font): LibraryTreeModelBuilderSwing {
		this.font = font
		return this
	}

	fun build(): TreeModel {
		val root = DefaultMutableTreeNode(Translations.getString("graph.desktop.name"))

		if (library != null) {
			if (includeImports) {
				for (l in library.expandedImports.libraries) {
					val node = NamableTreeNode(l, font)
					addLibrary(node, font, l, filter)
					root.add(node)
				}
			} else {
				val node = NamableTreeNode(library, font)
				addLibrary(node, font, library, filter)
				root.add(node)
			}
		}

		return DefaultTreeModel(root)
	}
}