package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.QuadTree
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.view.AbstractZoomPanAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.PanMethod

/**
 * Module definitions for the [ch.scorpion.jabbah.draw] package.
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