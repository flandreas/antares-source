package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import kotlin.reflect.KClass

/**
 * An object that combines multiple selected [Components][Component] of the same type in order to allow
 * the user to edit one property for all these objects in one go.
 */
data class MultiSelection(
	val selection: Collection<Component>,
	val commonType: KClass<*>
) : AbstractComponent(), Bean {

	init {
		require(selection.isNotEmpty())
	}

	override val boundingBox: RectangularShape = Rectangle2D()

	override fun draw(context: DrawContext) { }

	override fun contains(x: Double, y: Double): Boolean = false

	override var location: Point2D = Point2D.ZERO

	override val type: String get() = selection.first().type
}