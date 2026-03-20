package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.geom.MutableRectangularShape
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.drawable.Orientable
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView

/**
 * The base class for implementing rectangular views of digital components.
 *
 * The geometry of digital component views is organized by a basic model grid, which is scaled to view coordinates for
 * rendering. The geometry is therefore expressed by units of this grid instead of view coordinates.
 * @param T the type of [ch.scorpion.jabbah.graph.model.Vertice] displayed by this view
 */
open class OrientableRectangularVerticeView<T : Vertice>(
    styleProvider: StyleProvider,
    model: T,
	rectangle: MutableRectangularShape = Rectangle2D()
) : AbstractRectangularVerticeView<T>(styleProvider, model, rectangle), Orientable {

	companion object {

		/**
		 * Transforms a width in basic model grid coordinates to view coordinates.
		 * @param value the value in basic model grid coordinates
		 * @return the transformed width in view coordinates.
		 */
		fun w(value: Int): Double = (value * Look.SCALE).toDouble()
		fun w(value: Double): Double = value * Look.SCALE
		fun w(value: Float): Double = (value * Look.SCALE).toDouble()

		fun wInt(value: Int): Int = value * Look.SCALE

		/**
		 * Transforms a height in basic model grid coordinates to view coordinates.
		 * @param value the value in basic model grid coordinates
		 * @return the transformed height in view coordinates.
		 */
		fun h(value: Int): Double = (value * Look.SCALE).toDouble()
		fun h(value: Double): Double = value * Look.SCALE
		fun h(value: Float): Double = (value * Look.SCALE).toDouble()

		fun hInt(value: Int): Int = value * Look.SCALE
	}

	protected val propertiesBackgroundColor
		get() = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else styleProvider.getStyle(
			StyleType.Companion.BACKGROUND
		).color.backgroundColor
}