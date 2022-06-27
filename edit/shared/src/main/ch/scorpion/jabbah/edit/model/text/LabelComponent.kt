package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [RectangularComponent] that contains a [Label] drawable.
 */
class LabelComponent(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	override val label: Label = createLabel(DEFAULT_TEXT, styleProvider),
	inverse: Boolean = false
) : AbstractRectangularComponent(styleType = StyleType.FIGURE, styleProvider = styleProvider), TextComponent, Transparent, Labeled {

	constructor(
		text: String,
		inverse: Boolean = false,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	): this(label = createLabel(text, styleProvider), inverse = inverse)

	companion object {
		private val TYPE = Translations.getString("edit.component.label")
		const val DEFAULT_TEXT = "text"

		private fun createLabel(text: String, styleProvider: StyleProvider): Label {
			return Label(
				text = text,
				font = styleProvider.getStyle(StyleType.TEXT).font,
				horizontalAlignment = HorizontalAlignment.CENTER,
				verticalAlignment = VerticalAlignment.CENTER,
				location = Point2D.ZERO,
				rotationDisplayStrategy = RotationDisplayStrategy.IGNORE)
		}
	}

	/** If `true`, the text is drawn in background color and the bounds are filled in foreground color.*/
	var inverse: Boolean = inverse
		set(value) {
			field = value
			label.inverse = field
		}

	/**
	 * The optional width and height of this [LabelComponent]. Overrides the default bounding box determined
	 * by the size of the text. Typically used along [inverse] to choose the size of the "background" box.
	 */
	var dimension: Dimension2D? = null

	init {
		DrawableOwner(this, label)
	}

	/** ---- [TextComponent] */

	override var text: Translatable = TranslatableText(label.text)
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				label.text = if (text.isEmpty) "" else (text.getTranslation())
				setFrame(label.boundingBox)
				invalidate()
				update()
			}
		}

	override var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				update()
			}
		}

	/** ---- [Transparent] interface */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
		}

	/** ---- [Snappable] interface */

	override val snappableX: Array<SnappableX>
		get() = arrayOf(SnappableXCoordinate(location.x))

	override val snappableY: Array<SnappableY>
		get() = arrayOf(SnappableYCoordinate(location.y))

	/** ---- [Drawable] */

	override val boundingBox: Rectangle2D get() {
		val bbox = if (dimension == null) {
			label.boundingBox
		} else {
			Rectangle2D.withCenter(location, dimension!!.width, dimension!!.height).expandBy(stroke.width / 2.0) as Rectangle2D
		}
		return if (rotation == Rotation.R0 || rotation == Rotation.R180) {
			bbox
		} else {
			rotation.rotateRectangleAround(bbox.center, bbox)
		}
	}

	override fun contains(x: Double, y: Double): Boolean = label.contains(x, y)

	override fun contains(p: Point2D): Boolean = label.contains(p)

	override fun intersects(rect: RectangularShape): Boolean = label.intersects(rect)

	override fun draw(context: DrawContext) {
		context.g.translate(location.x, location.y)
		context.g.rotate(rotation.angle)
		context.g.translate(-location.x, -location.y)


		if (inverse && dimension != null) {
			context.g.color = if (context.useContextColors) {
				context.color!!.foregroundColor
			} else {
				transparent.applyTo(foregroundColor)
			}
			val rect = Rectangle2D.withCenter(location, dimension!!.width, dimension!!.height)
			context.g.fill(rect)
			context.g.stroke = stroke
			context.g.draw(rect)
		}

		if (!context.useContextColors) {
			context.g.color = if (inverse && dimension != null) {
				transparent.applyTo(backgroundColor)
			} else {
				transparent.applyTo(foregroundColor)
			}
		}

		label.draw(context)

		context.g.translate(location.x, location.y)
		context.g.rotate(-rotation.angle)
		context.g.translate(-location.x, -location.y)
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
		get() = label.location
		set(value) {
			label.location = value
		}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		if (!text.isEmpty) {
			writer.writeStorables("text", text.allTranslations())
		}
		writer.writePoint("location", label.location)
		writer.writeString("rot", rotation.customName)
		if (inverse) {
			writer.writeBoolean("inverse", inverse)
		}
		dimension?.let {
			writer.writeDouble("w", it.width)
			writer.writeDouble("h", it.height)
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("id")) {
			id = reader.readInt("id")
		}
		if (reader.hasAttribute("text")) {
			// Backward compatibility
			text = TranslatableText(reader.readString("text"))
		}
		if (reader.hasElement("text")) {
			text = TranslatableText(reader.readStorables("text"))
		}
		location = reader.readPoint("location")
		if (reader.hasAttribute("rot")) {
			// Backward compatibility
			rotation = Rotation.withName(reader.readString("rot"))
		}
		if (reader.hasAttribute("inverse")) {
			inverse = reader.readBoolean("inverse")
		}
		if (reader.hasAttribute("w") && reader.hasAttribute("h")) {
			dimension = Dimension2D(reader.readDouble("w"), reader.readDouble("h"))
		}
	}

	/** ---- [Component] */

	override val type: String = TYPE

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}

	override val useRotation: Boolean get() = true

	override fun rotationChanged(newRotation: Rotation) {
		label.ownerRotation = rotation
	}
}