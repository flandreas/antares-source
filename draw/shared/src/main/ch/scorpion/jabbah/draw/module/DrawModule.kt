package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawProperties
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.polyline.PolylineShape
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

    /**
     * Creates a [PolylineShape] for the specified [Point2D]s. Must be implemented platform-specifically.
     */
    var polylineShapeFactory: (List<Point2D>?) -> PolylineShape = { throw UnsupportedOperationException() }

    /**
     * Creates a [TextRenderInfo] for a particular text [String] and the [Font] to be used for rendering.
     * Must be implemented platform-specifically.
     */
    var textRenderInfoFactory: TextRenderInfoFactory = object : TextRenderInfoFactory {
        override fun measureHtmlText(text: String, font: Font, width: Int): TextRenderInfo {
            throw UnsupportedOperationException("not implemented")
        }
        override fun measureSingleLineText(text: String, font: Font): TextRenderInfo {
            throw UnsupportedOperationException("not implemented")
        }
    }

    /** Loads an [Image] from the specified path. Must be implemented platform-specifically. */
    var imageLoader: ImageLoader = { throw UnsupportedOperationException() }

    override fun initialize() {
        BaseModule.require()
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

	fun drawLocatableDebugBoundingBox(locatable: Locatable, g: Graphics2D, color: Color = DEBUG_BBOX_COLOR) {
		if (debugGfx) {
			drawDebugBoundingBox(locatable, g, color)
			g.fillOval((locatable.location.x - 2).toInt(), (locatable.location.y - 2).toInt(), 4, 4)

		}
	}
}