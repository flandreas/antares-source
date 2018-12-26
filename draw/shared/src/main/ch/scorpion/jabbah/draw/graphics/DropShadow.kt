package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Geometry
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes

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

	fun draw(context: DrawContext, drawer: (DrawContext) -> Unit) {
		if (useShadow) {
			begin(context)
			context.g.color = context.choose(Themes.get<DrawTheme>().shadow).foregroundColor
			drawer.invoke(context)
			end(context)
		}
	}

	fun expand(rect: RectangularShape): RectangularShape {
		if (useShadow) {
			rect.expandBy(0.0, 0.0, offset, offset)
		}
		return rect
	}
}