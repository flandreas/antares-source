package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.draw.DrawableAdapter
import io.antarescircuit.jabbah.draw.DrawableEvent
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.SelectionModel

/**
 * [AbstractSelectionModel] is a convenient base class for implementing [SelectionModel]s.
 *
 * It stores the [Component] that this [SelectionModel] currently selects. It listens for geometrical
 * updates of the [Component] and calls [componentUpdated] which must be implemented by inheriting
 * classes in order to update the geometry of this [SelectionModel] accordingly.
 *
 * [SelectionModel]s are designed to make instances reusable.
 */
abstract class AbstractSelectionModel<T : Component>(
	override val component: T
) : AbstractDrawable(), SelectionModel<T> {

	// Kotlin bug KT-14888 forces this property to be non-private
	val geometryUpdateListener = object : DrawableAdapter() {
		// private val geometryUpdateListener = object : DrawableAdapter() {
		override fun drawableUpdated(event: DrawableEvent) {
			componentUpdated()
		}
	}

    override fun setup() {
        component.addDrawableListener(geometryUpdateListener)
        componentUpdated()
    }

    override fun dispose() {
        component.removeDrawableListener(geometryUpdateListener)
    }

    override fun notifyAdded(view: DrawingView<*>) { }

    override fun notifyRemoved(view: DrawingView<*>) { }
}