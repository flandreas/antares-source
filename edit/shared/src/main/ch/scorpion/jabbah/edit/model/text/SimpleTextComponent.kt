package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.MultilineText
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A simple, non-editable [TextComponent] that uses a [MultilineText] for text rendering.
 */
class SimpleTextComponent(
	text: TranslatableText = TranslatableText(),
	location: Point2D = Point2D.ZERO,
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularComponent(
	styleType = styleType,
	styleProvider = styleProvider,
	shape = Rectangle2D(location.x, location.y, 100.0, 50.0)
), TextComponent, Transparent {

	private companion object {

		private val TYPE = Translations.getString("edit.component.text")

		/** The horizontal inset between the bounding box and the text.  */
		private const val INSET_X = 10

		/** The vertical inset between the bounding box and the text.  */
		private const val INSET_Y = 10
	}

	/** ---- [Transparent] interface */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
		}

	override var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				update()
			}
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (!text.isEmpty) {
			writer.writeStorables("text", text.allTranslations())
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("text")) {
			// Backward compatibility
			text = TranslatableText(reader.readString("text"))
		}
		if (reader.hasElement("text")) {
			text = TranslatableText(reader.readStorables("text"))
		}
	}

	/** ---- [Component] interface */

	override val type: String get() = TYPE

	/** ---- [TextComponent] interface */

	override var text: Translatable = text
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateMultilineText()
				invalidate()
				update()
				validate()
			}
		}

	/** ---- [Drawable] */

	override fun contains(x: Double, y: Double): Boolean = super<AbstractRectangularComponent>.contains(x, y)

	override fun contains(p: Point2D): Boolean = super<AbstractRectangularComponent>.contains(p)

	override fun intersects(rect: RectangularShape): Boolean = super<AbstractRectangularComponent>.intersects(rect)

	override fun draw(context: DrawContext) {
		if (filled) {
			decorator.drawBackground(this, context)
		}

		context.g.font = font
		context.g.color = textColor

		// TODO: Implement clipping in JavaScript platform
		// val b = shape
		// val oldClip = context.g.getClipBounds()
		//(context.g as Graphics2DJvm).g.setClip(b.x.toInt(), b.y.toInt(), b.width.toInt(), b.height.toInt())
		context.g.translate(x + INSET_X, y + INSET_Y)
		multilineText.draw(context)
		context.g.translate(-(x + INSET_X), -(y + INSET_Y))
		//(context.g as Graphics2DJvm).g.setClip(oldClip.x.toInt(), oldClip.y.toInt(), oldClip.width.toInt(), oldClip.height.toInt())

		if (stroked) {
			decorator.drawForeground(this, context)
		}
	}

	override fun update() {
		updateMultilineText()
		super.update()
	}

	/** ---- [SimpleTextComponent] */

	private val displayedText: String get() = if (text.isEmpty) "" else text.getTranslation()

	private var multilineText = MultilineText(displayedText, font, (width.toInt() - 2 * INSET_X).toDouble())

	private var decorator: TextComponentDecorator = RectangularShapeTextComponentDecorator(
		shape = RoundRectangle2D(0.0, 0.0, 0.0, 0.0, 20.0, 20.0),
		stylable = this,
		transparent = transparent
	)

	init {
		updateMultilineText()
	}

	private fun updateMultilineText() {
		multilineText = MultilineText(displayedText, font, (width.toInt() - 2 * INSET_X).toDouble())
	}
}