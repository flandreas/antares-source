package io.antarescircuit.jabbah.edit.figure

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

abstract class AbstractPathFigure(
    private val path: Path,
    override val type: String
) : AbstractComponent(), Figure {

	/** ---- [io.antarescircuit.jabbah.draw.Drawable] */

	override val boundingBox: RectangularShape
		get() = Rectangle2D(path.boundingBox).also {
		it.setFrame(location.x + it.x, location.y + it.y, it.width, it.height)
		it.expandBy(stroke.width.toDouble())
	}

	override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)

	override fun draw(context: DrawContext) {
		context.g.translate(location.x, location.y)

		if (shadow) {
			DropShadow.draw(context, Transparent.Companion.FULLY_OPAQUE) {
				context.g.fill(path)
			}
		}

		context.g.color = if (context.useContextColors) {
			context.color!!.backgroundColor
		} else {
			if (Look.FILL_BASIC_COMPONENTS) backgroundColor else DrawStyleModule.styleProvider.getStyle(StyleType.Companion.BACKGROUND).color.backgroundColor
		}
		context.g.fill(path)

		context.g.color = context.chooseForeground(foregroundColor)
		context.g.stroke = stroke
		context.g.draw(path)

		context.g.translate(-location.x, -location.y)
	}

	/** ---- [io.antarescircuit.jabbah.draw.drawable.Locatable] */

	override var location: Point2D = Point2D.Companion.ZERO
		set(value) {
			invalidate()
			field = value
			invalidate()
			update()
		}

	/** ---- [io.antarescircuit.jabbah.io.Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writePoint("location", location)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		location = reader.readPoint("location")
	}

	/** ---- [io.antarescircuit.jabbah.draw.drawable.Mirrorable] */

	override fun mirrorHorizontally(x: Double) {
		val transform = System.createAffineTransform()
		transform.scale(-1.0, 1.0)
		transform.translate(-x, 0.0)
		path.transform(transform)
		location = location.mirrorHorizontally(x)
	}

	override fun mirrorVertically(y: Double) {
		val transform = System.createAffineTransform()
		transform.scale(1.0, -1.0)
		transform.translate(0.0, -y)
		path.transform(transform)
		location = location.mirrorVertically(y)
	}
}