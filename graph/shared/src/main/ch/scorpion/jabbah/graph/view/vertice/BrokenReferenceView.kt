package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef

/**
 * A [Drawable] that represent a [SubGraphVerticeRef] with a broken [MetaGraph] reference.
 */
class BrokenReferenceView(
	private val styleProvider: StyleProvider
) : AbstractRectangle(1.5 * EXPECTED_PORT_VIEW_SIZE, -EXPECTED_VERTICAL_INSET, SIZE, SIZE) {

	companion object {
		private const val SIZE = 40.0
		private const val EXPECTED_PORT_VIEW_SIZE = 14.0
		private const val EXPECTED_VERTICAL_INSET = 7.0
	}

	private val stroke: Stroke get() = styleProvider.getStyle(StyleType.FIGURE).stroke

	private val font: Font get() = styleProvider.getStyle(StyleType.FIGURE).font

	private val foreground: Color get() = styleProvider.getStyle(StyleType.FIGURE).color.foregroundColor

	private val background: Color get() = styleProvider.getStyle(StyleType.BACKGROUND).color.foregroundColor

	private val label: Label = Label(
		text ="?",
		font = font,
		color = foreground,
		location = Point2D(bounds.centerX, bounds.centerY)
	)

	override fun draw(context: DrawContext) {
		drawRectangle(
			context,
			if (context.useContextColors) context.color!!.foregroundColor else foreground,
			if (context.useContextColors) context.color!!.backgroundColor else background,
			stroke)
		label.draw(context)
	}

	override val lineWidth: Double get() = stroke.width.toDouble()
}