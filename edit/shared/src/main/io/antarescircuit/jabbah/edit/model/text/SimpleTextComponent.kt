package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType

/**
 * A simple, non-editable [TextComponent] that uses a [RichTextDrawable] for text rendering.
 */
class SimpleTextComponent(
	text: TranslatableText = TranslatableText(),
	location: Point2D = Point2D.ZERO,
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractTextComponent(
	styleType = styleType,
	styleProvider = styleProvider,
	shape = Rectangle2D(location.x, location.y, 100.0, 50.0)
), TextComponent, Transparent {

	companion object {

		/**
		 * [TextComponent] were up to now positioned by the user on the JVM platform, where a JTextPane
		 * is used for drawing the text. JTextPane introduced some margin between the [TextComponent]'s location
		 * and the actual text rendered, especially (or exclusively) on the y-axis. This magic number has
		 * been evaluated empirically.
		 */
		const val JVM_OFFSET_Y = 16
	}

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
		context.translated(x + INSET_X, y + INSET_Y + JVM_OFFSET_Y) {
			multilineText.draw(it)
		}
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

	private var multilineText = createMultilineText()

	init {
		updateMultilineText()
	}

	private fun updateMultilineText() {
		multilineText = createMultilineText()
	}

	private fun createMultilineText(): RichTextDrawable =
		RichTextDrawable.multiline(displayedText, font, (width.toInt() - 2 * INSET_X).toDouble())
}