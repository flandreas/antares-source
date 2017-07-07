package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableAdapter
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.base.geom.Rectangle2D

/**
 * [AbstractSelectionModel] is a convenient base class for implementing [SelectionModel]s.
 *
 * It stores the [Component] that this [SelectionModel] currently selects. It listens for geometrical
 * updates of the [Component] and calls [componentUpdated] which must be implemented by inheriting
 * classes in order to update the geometry of this [SelectionModel] accordingly.
 *
 * [SelectionModel]s are designed to make instances reusable.
 */
abstract class AbstractSelectionModel<T : Component>(component: T) : AbstractDrawable(), SelectionModel<T> {

    override val component: T = component

    // Kotlin bug KT-14888 forces this property to be non-private
    // TODO Make private again after upgrade to Kotlin 1.1-M04
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

    override fun notifyAdded(view: DrawingView<*>) {
        // empty
    }

    override fun notifyRemoved(view: DrawingView<*>) {
        // empty
    }

    /**
     * This method is automatically called by this [AbstractSelectionModel] whenever the underlying
     * [Component] geometry has been changed.
     *
     * Inheriting classes implement this method to update the shape of this [SelectionModel] according to the new
     * geometry of the [Component].
     */
    abstract fun componentUpdated()

}