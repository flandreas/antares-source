package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.AbstractComponent
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