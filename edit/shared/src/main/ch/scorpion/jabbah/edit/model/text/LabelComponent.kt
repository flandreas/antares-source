package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.FontStyle
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent

/**
 * A [RectangularComponent] that contains a [Label] drawable.
 */
class LabelComponent(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	val label: Label = Label(
		text = DEFAULT_TEXT,
		font = LabelComponent.DEFAULT_FONT,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER,
		location = Point2D.ZERO,
		rotationDisplayStrategy = Label.RotationDisplayStrategy.ROTATE_HALF)
) : AbstractRectangularComponent(styleType = StyleType.FIGURE, styleProvider = styleProvider), TextComponent, Transparent {

	companion object {
		const val DEFAULT_TEXT = "text"
		val DEFAULT_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 14)
	}

	init {
		DrawableOwner(this, label)
	}

	/** ---- UI properties */

	override var text: String
		get() = label.text
		set(value) {
			label.text = value
			setFrame(label.boundingBox)
		}

	/** ---- [Transparent] interface */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
		}

	/** ---- [Drawable] */

	override val boundingBox: Rectangle2D get() = label.boundingBox

	override fun contains(x: Double, y: Double): Boolean = label.contains(x, y)

	override fun contains(p: Point2D): Boolean = label.contains(p)

	override fun draw(context: DrawContext) {
		if (!context.useContextColors) {
			context.g.color = transparent.applyTo(foregroundColor)
		}
		label.draw(context)
	}

	override fun mirrorHorizontally(x: Double) {
		super.mirrorHorizontally(x)
		label.mirrorHorizontally(x)
	}

	override fun mirrorVertically(y: Double) {
		super.mirrorVertically(y)
		label.mirrorVertically(y)
	}

	override var location: Point2D
		get() = Point2D(label.location)
		set(value) {
			label.location = value
		}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		writer.writeString("text", text)
		writer.writePoint("location", label.location)
	}

	override fun read(reader: StoreReader) {
		text = reader.readString("text")
		location = reader.readPoint("location")
	}

	/** ---- [Component] */

	override val type: String? get() = Translations.getString("edit.component.label")

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}

}