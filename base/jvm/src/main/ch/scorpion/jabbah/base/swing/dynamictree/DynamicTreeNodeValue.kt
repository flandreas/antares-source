package ch.scorpion.jabbah.base.swing.dynamictree

class DynamicTreeNodeValue(val value: Any, private val hasChildren: Boolean) {

	fun hasChildren(): Boolean {
		return this.hasChildren
	}
}
