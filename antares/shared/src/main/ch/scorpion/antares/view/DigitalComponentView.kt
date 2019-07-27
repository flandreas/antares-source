package ch.scorpion.antares.view

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView

/**
 * The base class for implementing rectangular views of digital components.
 *
 * The geometry of digital component views is organized by a basic model grid, which is scaled to view coordinates for
 * rendering. The geometry is therefore expressed by units of this grid instead of view coordinates.
 * @param T the type of [Vertice] displayed by this view
 */
open class DigitalComponentView<T : Vertice>(
    styleProvider: StyleProvider,
    model: T?
) : AbstractRectangularVerticeView<T>(styleProvider, model) {

    companion object {

        /**
         * Transforms a width in basic model grid coordinates to view coordinates.
         * @param value the value in basic model grid coordinates
         * @return the transformed width in view coordinates.
         */
        fun w(value: Int): Double {
            return (value * Look.SCALE).toDouble()
        }

        /**
         * Transforms a height in basic model grid coordinates to view coordinates.
         * @param value the value in basic model grid coordinates
         * @return the transformed height in view coordinates.
         */
        fun h(value: Int): Double {
            return (value * Look.SCALE).toDouble()
        }
    }

	protected val propertiesBackgroundColor get() = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor

	/** ---- Properties to be edited by the User */

    open var orientation: Direction
        get() = Direction.of(rotation)
        set(value) {
            rotation = value.rotation
        }
}