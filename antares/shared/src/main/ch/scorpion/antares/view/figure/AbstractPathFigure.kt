package ch.scorpion.antares.view.figure

import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.figure.Figure
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

abstract class AbstractPathFigure(
	private val path: Path,
	override val type: String
) : AbstractComponent(), Figure {

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape
		get() = Rectangle2D(path.boundingBox).also {
		it.setFrame(location.x + it.x, location.y + it.y, it.width, it.height)
		it.expandBy(stroke.width.toDouble())
	}

	override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)

	override fun draw(context: DrawContext) {
		context.g.translate(location.x, location.y)

		if (shadow) {
			DropShadow.draw(context, Transparent.FULLY_OPAQUE) {
				context.g.fill(path)
			}
		}

		context.g.color = if (context.useContextColors) {
			context.color!!.backgroundColor
		} else {
			if (Look.FILL_BASIC_COMPONENTS) backgroundColor else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
		}
		context.g.fill(path)

		context.g.color = context.choose(color).foregroundColor
		context.g.stroke = stroke
		context.g.draw(path)

		context.g.translate(-location.x, -location.y)
	}

	/** ---- [Locatable] */

	override var location: Point2D = Point2D.ZERO
		set(value) {
			invalidate()
			field = value
			invalidate()
			update()
		}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writePoint("location", location)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		location = reader.readPoint("location")
	}
}