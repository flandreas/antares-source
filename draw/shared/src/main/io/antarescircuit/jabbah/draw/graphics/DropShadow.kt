package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.geom.Geometry
import io.antarescircuit.jabbah.base.geom.MutableRectangularShape
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.Themes

/** A utility object for drawing drop shadows behind [Drawable]s.*/
object DropShadow {

	/** The name of the shadow offset [Int] in [Properties].*/
	const val PROP_OFFSET = "draw.graphics.dropShadow.offset"

	/** The name of the default shadow [Boolean] property in [Properties].*/
	const val PROP_SHADOW = "draw.graphics.shadow"

	val offset: Double get() = BaseModule.properties.getInt(PROP_OFFSET).toDouble()

	private val useShadow: Boolean get() = BaseModule.properties.getBoolean(PROP_SHADOW)

	fun begin(context: DrawContext) {
		if (useShadow) {
			val p = Geometry.rotate(offset, offset, -context.g.rotationAngle)
			context.g.translate(p.x, p.y)
		}
	}

	fun end(context: DrawContext) {
		if (useShadow) {
			val p = Geometry.rotate(offset, offset, -context.g.rotationAngle)
			context.g.translate(-p.x, -p.y)
		}
	}

	fun draw(context: DrawContext, transparency: Int, drawer: (DrawContext) -> Unit) {
		if (useShadow && transparency == Transparent.FULLY_OPAQUE) {
			begin(context)
			context.g.color = context.choose(Themes.get<DrawTheme>().shadow).foregroundColor
			drawer.invoke(context)
			end(context)
		}
	}

	fun expand(rect: MutableRectangularShape, rotation: Rotation) {
		if (useShadow) {
			when (rotation) {
				Rotation.R0 -> rect.expandBy(0.0, 0.0, offset, offset)
				Rotation.R90 -> rect.expandBy(0.0, offset, offset, 0.0)
				Rotation.R180 -> rect.expandBy(offset, offset, 0.0, 0.0)
				Rotation.R270 -> rect.expandBy(offset, 0.0, 0.0, offset)
			}
		}
	}
}