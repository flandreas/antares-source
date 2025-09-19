package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.Mirrorable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef

/**
 * A [Drawable] that represent a [SubGraphVerticeRef] with a broken [MetaGraph] reference.
 */
class BrokenReferenceView(
	ownerRotation: Rotation = Rotation.R0,
	private val styleProvider: StyleProvider
) : AbstractRectangle(1.5 * EXPECTED_PORT_VIEW_SIZE, -EXPECTED_VERTICAL_INSET, SIZE, SIZE), Labeled, Mirrorable {

	companion object {
		private const val SIZE = 40.0
		private const val EXPECTED_PORT_VIEW_SIZE = 14.0
		private const val EXPECTED_VERTICAL_INSET = 7.0

		val NAME = TranslatableText(listOf(
			Translation(System.currentLanguage(), Translations.getString("graph.element.brokenRef.name"))
		))
	}

	private val stroke: Stroke get() = styleProvider.getStyle(StyleType.FIGURE).stroke

	private val font: Font get() = styleProvider.getStyle(StyleType.FIGURE).font

	private val foregroundColor: Color get() = styleProvider.getStyle(StyleType.FIGURE).color.foregroundColor

	private val backgroundColor: Color get() = styleProvider.getStyle(StyleType.BACKGROUND).color.foregroundColor

	private val textColor: Color get() = styleProvider.getStyle(StyleType.FIGURE).color.textColor

	override val label: Label = Label(
		text = "?",
		font = font,
		color = textColor,
		location = Point2D(bounds.centerX, bounds.centerY),
		ownerRotation = ownerRotation,
		rotationDisplayStrategy = RotationDisplayStrategy.KEEP_HORIZONTAL
	)

	override fun draw(context: DrawContext) {
		drawRectangle(
			context,
			if (context.useContextColors) context.color!!.foregroundColor else foregroundColor,
			if (context.useContextColors) context.color!!.backgroundColor else backgroundColor,
			stroke)
		label.color = if (context.useContextColors) context.color!!.textColor else textColor
		label.draw(context)
	}

	override val lineWidth: Double get() = stroke.width.toDouble()

	/** ---- [Mirrorable] interface */

	override fun mirrorHorizontally(x: Double) {
		setBounds(Point2D(this.x + width, this.y).mirrorHorizontally(x).x, this.y, width, height)
	}

	override fun mirrorVertically(y: Double) {
		setBounds(this.x, Point2D(this.x, this.y + height).mirrorVertically(y).y, width, height)
	}
}