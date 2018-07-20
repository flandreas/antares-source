package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.draw.View
import javax.swing.JPopupMenu
import ch.scorpion.jabbah.draw.Drawable

/**
 * Provides the content of [JPopupMenu] when the user requests it within a [View].
 */
interface ContextMenuProvider {

	/**
	 * Fills the specified [JPopupMenu] requested at a particular location within a [View].
	 * Note that the location coordinates are in model space to ease finding the target [Drawable].
	 *
	 * @param view the [View] in which the context menu is requested
	 * @param x the x coordinate in model space where the context menu is requested
	 * @param y the y coordinate in model space where the context menu is requested
	 * @param menu the [JPopupMenu] to be filled by this [ContextMenuProvider]
	 */
	fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu)
}