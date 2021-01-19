package ch.scorpion.jabbah.edit.snap

import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.Grid.Companion.PROP_SNAP_ENABLED
import kotlin.math.floor

/**
 * A standard, simple implementation of a grid that defines a two dimensional array of points that are used for snapping
 * in terms of a [Snapper].
 *
 * [GridImpl] delegates drawing of the grid points to a pluggable [GridPainter]. If the distance of these
 * points falls below a certain minimum distance because of zooming, [GridImpl] doesn't draw these points any more.
 */
class GridImpl(
	private val styleProvider: StyleProvider = StyleRepository.INSTANCE,
	chosenDistance: Double? = null,
	chosenPaintFactor: Int? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractSnapper(BaseModule.settings.getBoolean(PROP_SNAP_ENABLED, true)), Grid {

	companion object {
		private val LOG by logger(GridImpl::class)
	}

	/**
	 * A buffer object for requesting the current clip rectangle when drawing the grid. Used to avoid creating the
	 * [Rectangle2D] whenever a part of the [GridImpl] is redrawn.
	 */
	private val clipBuffer: Rectangle2D = Rectangle2D()

	private val configuredGridPainter get() = GridPainterRegistry.get(BaseModule.properties.getString(Grid.PROP_GRID_PAINTER)).invoke(styleProvider)

	/** The object that actually paints the grid dots.*/
	override var gridPainter: GridPainter = configuredGridPainter
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGridPainterProperties()
				invalidate()
				validate()
			}
		}

	override var paintFactor: Int
		get() = chosenPaintFactor ?: BaseModule.properties.getInt(Grid.PROP_GRID_DEFAULT_PAINT_FACTOR)
		set(value) {
			chosenPaintFactor = value
			updateGridPainterProperties()
		}

	override var distance: Double
		get() = chosenDistance ?: BaseModule.properties.getInt(Grid.PROP_GRID_DEFAULT_DISTANCE).toDouble()
		set(value) {
			chosenDistance = value
			updateGridPainterProperties()
		}

	private var chosenPaintFactor: Int? = chosenPaintFactor

	private var chosenDistance: Double? = chosenDistance

	private val preferencesHandler: (PreferencesChangedEvent) -> Unit = {
		gridPainter = configuredGridPainter
		updateGridPainterProperties()
		invalidate()
		requestRedraw()
	}

	init {
		eventBus.register(PreferencesChangedEvent::class, preferencesHandler)
		updateGridPainterProperties()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(PreferencesChangedEvent::class, preferencesHandler)
	}

	/** ---- [Grid] interface */

	override var view: View<EditInputEventContext>? = null
		set(value) {
			if (value == null) {
				throw IllegalArgumentException("view must not be null")
			}
			field = value
			zoomPan = field!!.zoomPan
		}

	override var zoomPan: ZoomPan? = null
		set(value) {
			field = value
			invalidate()
			updateGridPainterProperties()
			invalidate()
		}

	/** ---- [Drawable] */

	override val boundingBox: Rectangle2D
		get() {
			if (view == null) {
				return Rectangle2D()
			}
			return Rectangle2D(0.0, 0.0, view!!.width.toDouble(), view!!.height.toDouble())
		}

	override fun contains(x: Double, y: Double): Boolean = true

	override fun draw(context: DrawContext) {
		if (zoomPan!!.zoomFactor * distance * paintFactor < BaseModule.properties.getInt(Grid.PROP_GRID_MIN_DISTANCE)) {
			// Don't paint the grid if it is too dense
			return
		}
		if (context.g.supportClipping) {
			context.g.getClipBounds(clipBuffer)
			gridPainter.paint(context, clipBuffer)
		} else {
			if (view != null) {
				gridPainter.paint(context, Rectangle2D(0, 0, view!!.width, view!!.height))
			}
		}
	}


	/** ---- [AbstractSnapper] */

	override var snapEnabled: Boolean
		get() = super.snapEnabled
		set(value) {
			if (value != snapEnabled) {
				super.snapEnabled = value
				BaseModule.settings.set(PROP_SNAP_ENABLED, value)
			}
		}

	override fun doSnapX(initSnappableX: SnappableX, initDx: Double): Double = snapValue(initSnappableX.x + initDx)

	override fun doSnapY(initSnappableY: SnappableY, initDy: Double): Double = snapValue(initSnappableY.y + initDy)

	/** ---- [GridImpl] */

	/**
	 * Snaps the specified number by calculating the minimum of the distances from the floor or ceiling grid point.
	 * @param d the number to be snapped
	 * @return the snapped number, which is a multiple of the current distance of the grid points
	 */
	private fun snapValue(d: Double): Double {
		val q = d / distance
		val floor = floor(q)
		val ceil = floor + 1

		if (q - floor < ceil - q) {
			return floor * distance
		}
		return ceil * distance
	}

	private fun updateGridPainterProperties() {
		gridPainter.distanceX = distance * paintFactor
		gridPainter.distanceY = distance * paintFactor
		gridPainter.zoomPan = zoomPan
	}
}