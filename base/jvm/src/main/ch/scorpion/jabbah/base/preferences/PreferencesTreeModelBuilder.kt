package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.module.BaseModule
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel

/** Builds a [TreeModel] of a [PreferenceGroup] tree.*/
class PreferencesTreeModelBuilder(
	private val preferencesTree: PreferenceGroup = BaseModule.preferencesTree
) {

	fun build(): TreeModel {
		return DefaultTreeModel(buildNode(preferencesTree))
	}

	private fun buildNode(group: PreferenceGroup): MutableTreeNode {
		val node = DefaultMutableTreeNode(group)
		for (child in group.children) {
			node.add(buildNode(child))
		}
		return node
	}
}