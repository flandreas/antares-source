package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.base.resettableLazy
import javax.swing.tree.TreeNode
import javax.swing.tree.DefaultMutableTreeNode
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.edit.model.text.description.Namable
import javax.swing.UIManager

/**
 * A [TreeNode] with a [Namable] as user object that holds a [RichTextDrawable] to render
 * the name as [RichText].
 */
class NamableTreeNode(
	namable: Namable,
	private val font: Font = DEF_FONT
) : DefaultMutableTreeNode(namable) {

	companion object {
		private val DEF_FONT: Font = Graphics2DJvm.fromAwtFont(UIManager.getFont("Tree.font"))
	}

	val richTextName = resettableLazy {
		RichTextDrawable.of((userObject as Namable).name.value, font)
	}
}