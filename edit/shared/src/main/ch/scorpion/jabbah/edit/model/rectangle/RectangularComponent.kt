package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.figure.Figure
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.observableDescription
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [RectangularComponent] is an [AbstractRectangularComponent] with a singe line text label
 * whose vertical alignment relative to the rectangle box can be chosen.
 */
abstract class RectangularComponent(
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	shape: MutableRectangularShape,
	labelRotation: Rotation = Rotation.R0,
	labelRotationDisplayStrategy: RotationDisplayStrategy = RotationDisplayStrategy.IGNORE
) : AbstractRectangularComponent(styleType, styleProvider, shape), Transparent, Describable, Labeled, Figure {

	companion object {
		// The distance between the rectangle border and the text box (if at top or at bottom)
		private const val TEXT_INSET: Int = 10
	}

	constructor(x: Double, y: Double, w: Double, h: Double) : this(shape = Rectangle2D(x, y, w, h))

	override val label = Label("", font, rotation = labelRotation, rotationDisplayStrategy = labelRotationDisplayStrategy)

	override var description: Description by observableDescription(Description(""))

	/** ---- Editable properties */

	var text: TranslatableText = TranslatableText()
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				label.text = if (field.isEmpty) "" else field.getTranslation()
				invalidate()
				update()
			}
		}

	var verticalAlignment: VerticalAlignment
		get() = label.verticalAlignment
		set(value) {
			if (label.verticalAlignment != value) {
				invalidate()
				label.verticalAlignment = value
				updateLabelLocation()
				invalidate()
			}
		}

	var horizontalAlignment: HorizontalAlignment
		get() = label.horizontalAlignment
		set(value) {
			if (label.horizontalAlignment != value) {
				invalidate()
				label.horizontalAlignment = value
				updateLabelLocation()
				invalidate()
			}
		}

	init {
		updateLabelLocation()
	}

	/** ---- [AbstractComponent] interface */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		label.ownerRotation = rotation
	}

	/** ---- [Transparent] interface */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
		}

	/** ---- [RectangularShape] */

	override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
		super.setFrame(x, y, width, height)
		updateLabelLocation()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (!text.isEmpty) {
			writer.writeStorables("text", text.allTranslations())
		}
		if (verticalAlignment != VerticalAlignment.CENTER) {
			writer.writeString("vAlign", verticalAlignment.customName)
		}
		if (horizontalAlignment != HorizontalAlignment.CENTER) {
			writer.writeString("hAlign", horizontalAlignment.customName)
		}
		description.write("desc", writer)
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
		if (reader.hasAttribute("vAlign")) {
			verticalAlignment = VerticalAlignment.withName(reader.readString("vAlign"))
		}
		if (reader.hasAttribute("hAlign")) {
			horizontalAlignment = HorizontalAlignment.withName(reader.readString("hAlign"))
		}
		description = Description.read("desc", reader)
	}

	/** ---- [Drawable] interface */

	override val boundingBox: RectangularShape get() =
		Rectangle2D(super.boundingBox)
			.add(Rectangle2D(label.boundingBox)
			.moveBy(location)) as Rectangle2D

	override fun draw(context: DrawContext) {
		if (context.useContextColors) {
			drawImpl(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
		} else {
			drawImpl(context, if (stroked) transparent.applyTo(foregroundColor) else null, if (filled) transparent.applyTo(backgroundColor) else null)
		}
	}

	protected fun drawImpl(context: DrawContext, strokeColor: Color?, fillColor: Color?) {
		val oldColor = context.g.color

		drawShadow(context, strokeColor, fillColor)
		drawShape(context, strokeColor, fillColor)
		context.g.color = transparent.applyTo(textColor)
		drawText(context)

		DrawModule.drawLocatableDebugBoundingBox(this, context)

		context.g.color = oldColor
	}

	protected open fun drawShape(context: DrawContext, strokeColor: Color?, fillColor: Color?) {
		drawFill(context, shapeToDraw, fillColor)
		drawStroke(context, shapeToDraw, strokeColor, stroke)
	}

	protected open fun drawShadow(context: DrawContext, strokeColor: Color?, fillColor: Color?) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				if (fillColor != null) {
					drawFill(context, shapeToDraw, context.choose(Themes.get<DrawTheme>().shadow).foregroundColor)
				}
				if (strokeColor != null) {
					drawStroke(context, shapeToDraw, context.choose(Themes.get<DrawTheme>().shadow).foregroundColor, stroke)
				}
			}
		}
	}

	override fun drawText(context: DrawContext) {
		label.font = font
		context.translated(location) {
			label.draw(it)
		}
	}

	override fun <T: InputEventContext> getTooltip(context: T): Tooltip? {
		if (text.isNotEmpty && label.contains(context.location.subtract(location))) {
			return buildToolTipText(title = null, text = description.value, subText = null)?.let {
				Tooltip(it, Rectangle2D(label.boundingBox).moveBy(location))
			}
		}
		return null
	}

	/** ---- [RectangularComponent] */

	private fun updateLabelLocation() {
		val y: Double = when (verticalAlignment) {
			VerticalAlignment.BOTTOM -> height - TEXT_INSET
			VerticalAlignment.CENTER -> height / 2
			VerticalAlignment.TOP -> TEXT_INSET.toDouble()
		}
		val x: Double = when (horizontalAlignment) {
			HorizontalAlignment.LEFT -> TEXT_INSET.toDouble()
			HorizontalAlignment.CENTER -> width / 2
			HorizontalAlignment.RIGHT -> width - TEXT_INSET
		}
		label.location = Point2D(x, y)
	}
}

open class RectangleComponent(
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	shape: Rectangle2D = Rectangle2D(0.0, 0.0, 0.0, 0.0)
) : RectangularComponent(styleType, styleProvider, shape) {

	companion object {
		val TYPE = Translations.getString("edit.component.rectangle")
	}

	constructor(x: Double, y: Double, w: Double, h: Double) : this(shape = Rectangle2D(x, y, w, h))

	override val type: String get() = TYPE
}

class RoundRectangleComponent(
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	shape: RoundRectangle2D = RoundRectangle2D(0.0, 0.0, 0.0, 0.0, 10.0, 10.0)
) : RectangularComponent(styleType, styleProvider, shape) {

	companion object {
		private val TYPE = Translations.getString("edit.component.roundrect")
	}

	constructor(x: Double, y: Double, w: Double, h: Double, arcW: Double, arcH: Double) : this(shape = RoundRectangle2D(x, y, w, h, arcW, arcH))

	override val type: String get() = TYPE
}


class EllipseComponent(
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	shape: Ellipse2D = Ellipse2D(0.0, 0.0, 0.0, 0.0)
) : RectangularComponent(styleType, styleProvider, shape) {

	companion object {
		val TYPE = Translations.getString("edit.component.ellipse")
	}

	constructor(x: Double, y: Double, w: Double, h: Double) : this(shape = Ellipse2D(x, y, w, h))

	override val type: String = TYPE
}