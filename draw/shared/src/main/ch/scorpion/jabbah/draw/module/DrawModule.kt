package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.view.DrawViewModule

/**
 * Module definitions for the [ch.scorpion.jabbah.draw] package.
 */
object DrawModule : AbstractModule() {

	/** Flag for enabling debug graphics.*/
    var debugGfx = false

	private val DEBUG_BBOX_COLOR = Color.RED
	val DEBUG_BBOX_COLOR_SECONDARY = Color.BLUE
	val DEBUG_STROKE = Stroke(0.5f)

	var properties: DrawProperties = DrawProperties(BaseModule.properties)

    /** Loads an [Image] from the specified path. Must be implemented platform-specifically. */
    var imageLoader: ImageLoader = { throw UnsupportedOperationException() }

    override fun initialize() {
        BaseModule.require()

	    Translations.addBundle("jabbah-draw")

        DrawGraphicsModule.require()
        DrawStyleModule.require()
        DrawViewModule.require()
        AnimationModule.require()
    }

	fun drawDebugBoundingBox(drawable: Drawable, g: Graphics2D, color : Color = DEBUG_BBOX_COLOR) {
		if (debugGfx) {
			g.color = color
			g.stroke = DEBUG_STROKE
			g.draw(drawable.boundingBox)

		}
	}

	fun drawLocatableDebugBoundingBox(locatable: Locatable, context: DrawContext, color: Color = DEBUG_BBOX_COLOR) {
		if (debugGfx) {
			drawDebugBoundingBox(locatable, context.g, color)
			drawDebugBoundingBoxLocation(locatable.location, context, color)
		}
	}

	fun drawDebugBoundingBoxLocation(location: Point2D, context: DrawContext, color: Color = DEBUG_BBOX_COLOR) {
		if (debugGfx) {
			context.g.color = color
			context.g.fillOval((location.x - 2).toInt(), (location.y - 2).toInt(), 4, 4)
		}
	}
}