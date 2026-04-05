package io.antarescircuit.jabbah.draw.container

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableBag

open class DrawableBagImpl<T : Drawable>(
	override var location: Point2D = Point2D.ZERO,
	override val useLocation: Boolean = false,
	override var rotation: Rotation = Rotation.R0
	) : DrawableBag<T> {

	private val _drawables: MutableList<T> by lazy { mutableListOf() }

	override val drawables: List<T> get() = _drawables

	override fun clear(): DrawableBag<T> {
		_drawables.clear()
		return this
	}

	override fun add(drawable: T, index: Int): DrawableBag<T> {
		_drawables.add(index, drawable)
		return this
	}

	override fun remove(drawable: Drawable): DrawableBag<T> {
		_drawables.remove(drawable)
		return this
	}
}