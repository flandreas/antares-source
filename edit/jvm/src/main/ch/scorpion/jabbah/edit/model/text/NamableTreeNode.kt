package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.resettableLazy
import javax.swing.tree.TreeNode
import javax.swing.tree.DefaultMutableTreeNode
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.edit.model.text.description.Namable

/**
 * A [TreeNode] with a [Namable] as user object that holds a [RichTextDrawable] to render
 * the name as [RichText].
 */
class NamableTreeNode(
	namable: Namable,
	private val font: Font
) : DefaultMutableTreeNode(namable) {

	val richTextName = resettableLazy {
		RichTextDrawable.of((userObject as Namable).name.value, font)
	}
}