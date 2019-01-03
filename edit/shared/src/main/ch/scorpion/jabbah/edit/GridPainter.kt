package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * An interface for pluggable classes that actually paint a [Grid] within the shape of a rectangle.
 * [GridPainter]s use the foreground color of [StyleType.BACKGROUND] for painting.
 */
interface GridPainter {

	/** The name with which this [GridPainter] gets registered in [GridPainterRegistry].*/
	val name: String

    /** The distance of the painted grid dots in x direction. */
    var distanceX: Double

    /** The distance of the painted grid dots in y direction. */
    var distanceY: Double

    /** The zoom properties to be used for painting.*/
    var zoomPan: ZoomPan?

    /** Paints the [Grid] within the specified rectangular bounds.*/
    fun paint(context: DrawContext, rect: Rectangle2D)

}

typealias GridPainterFactory = (StyleProvider) -> GridPainter

object GridPainterRegistry {

	/** Maps the name of a [GridPainter] to the factory that creates such a [GridPainter].*/
	private val factories: MutableMap<String,GridPainterFactory> = mutableMapOf()

	fun register(name: String, factory: GridPainterFactory) {
		factories[name] = factory
	}

	fun get(name: String): GridPainterFactory {
		return factories[name]!!
	}
}