package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.preferences.BooleanPreference
import ch.scorpion.jabbah.base.preferences.FloatPreference
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.polyline.PolylineShape
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ZoomPanController

/**
 * Module definitions for the [ch.scorpion.jabbah.draw] package.
 */
object DrawModule : AbstractModule() {

	const val PREF_TREE_RENDERING = "draw.preferences.group.rendering"
	const val PREF_TREE_VIEW = "draw.preferences.group.view"

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

	    Translations.addBundle("jabbah-draw")
	    buildPropertyTree(BaseModule.preferencesTree)

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
			context.g.fillOval((locatable.location.x - 2).toInt(), (locatable.location.y - 2).toInt(), 4, 4)
		}
	}

	private fun buildPropertyTree(root: PreferenceGroup) {
		root.add(PreferenceGroup(PREF_TREE_RENDERING))
		root.add(PreferenceGroup(PREF_TREE_VIEW))

		root.getGroup(PREF_TREE_RENDERING).add(BooleanPreference(
			id = DropShadow.PROP_SHADOW,
			nameKey = "draw.preferences.DropShadow.enable"))

		root.getGroup(PREF_TREE_RENDERING).add(IntPreference(
			id = DropShadow.PROP_OFFSET,
			nameKey = "draw.preferences.DropShadow.offset",
			minValue = 0,
			maxValue = 10))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = AbstractViewAction.PROP_ZOOM_STEP,
			nameKey = "draw.preferences.ViewAction.zoomStep",
			minValue = 1.01f))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = ZoomPanController.PROP_WHEEL_ZOOM_STEP,
			nameKey = "draw.preferences.ZoomPanController.wheelZoomStep",
			minValue = 1.01f))

		root.getGroup(PREF_TREE_VIEW).add(IntPreference(
			id = ZoomPanController.PROP_WHEEL_PAN_STEP,
			nameKey = "draw.preferences.ZoomPanController.wheelPanStep",
			minValue = 1))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = View.PROP_MIN_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.minZoomFactor"))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = View.PROP_MAX_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.maxZoomFactor"))
	}
}