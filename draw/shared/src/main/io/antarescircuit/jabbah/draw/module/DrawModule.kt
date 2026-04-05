package io.antarescircuit.jabbah.draw.module

import io.antarescircuit.jabbah.animation.AnimationModule
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.container.QuadTree
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.draw.graphics.*
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.view.AbstractZoomPanAction
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.draw.view.PanMethod

/**
 * Module definitions for the [io.antarescircuit.jabbah.draw] package.
 */
object DrawModule : AbstractModule() {

	/** Flag for enabling debug graphics.*/
    var debugGfx = false

	private val DEBUG_BBOX_COLOR = Color.RED
	private val DEBUG_QUAD_TRE_COLOR = Color.YELLOW
	val DEBUG_BBOX_COLOR_SECONDARY = Color.BLUE
	val DEBUG_STROKE = Stroke(0.5f)

	var properties: DrawProperties = DrawProperties(BaseModule.properties)

	/** Creates a raster image. Must be implemented platform-specifically. */
	var rasterImageFactory: RasterImageFactory = { _, _ -> throw UnsupportedOperationException() }

	var imageLoader: ImageLoader = object : ImageLoader {
		override fun loadSystemImage(path: String, type: ImageType): Image {
			throw UnsupportedOperationException()
		}

		override fun loadUserImage(path: String, type: ImageType): Image {
			throw UnsupportedOperationException()
		}
	}

	var drawContextFactory: DrawContextFactory = { g, mc, appContext -> DrawContext(g, mc, appContext) }

	var mouseWheelPanModifier: Int = KeyEvent.VK_ALT

    override fun initialize() {
        BaseModule.require()

	    Translations.addBundle("jabbah-draw")

        DrawGraphicsModule.require()
        DrawStyleModule.require()
        DrawViewModule.require()
        AnimationModule.require()

	    fillProperties(properties)
    }

	override fun resetDependencies() {
		BaseModule.reset()
		DrawGraphicsModule.reset()
		DrawStyleModule.reset()
		DrawViewModule.reset()
		AnimationModule.reset()
	}

	private fun fillProperties(properties: Properties) {
		properties.set(AbstractZoomPanAction.PROP_ZOOM_STEP, 1.5f)
		properties.set(PanMethod.PROP_PAN_METHOD, PanMethod.MiddleMouseButton.customName)
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

	fun <T: Drawable> drawQuadTree(container: DrawableContainer<T>, context: DrawContext) {
		context.g.color = DEBUG_QUAD_TRE_COLOR
		context.g.stroke = DEBUG_STROKE
		val quadTree = QuadTree<Drawable>(container.boundingBox)
		for (e in container.frontToBackIterator()) {
			quadTree.add(e)
		}
		quadTree.drawGrid(context)
	}
}