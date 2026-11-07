package io.antarescircuit.jabbah.draw.graphics

/**
 * An image that renders itself on an AWT graphics context.
 * Backend consumers can implement this with their own SVG renderer without depending on UI modules.
 */
interface DrawableImageJvm : Image {

	fun draw(g: java.awt.Graphics2D)
}
