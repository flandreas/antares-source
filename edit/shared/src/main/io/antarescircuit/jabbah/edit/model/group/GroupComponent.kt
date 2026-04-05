package io.antarescircuit.jabbah.edit.model.group

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Represents a collection of [Component]s that have been "grouped together" to form a single [Component].
 *
 * The users benefit of this class is to manipulate the grouped [Component]s as a whole. For example,
 * by moving the [GroupComponent], all contained child [Component]s are moved by the same offset.
 *
 * The location of this [GroupComponent] is defined as the upper left corner of the combined bounding box.
 */
class GroupComponent(components: List<Component> = listOf()) : AbstractComponent() {

	companion object {
		private val TYPE = Translations.getString("edit.component.group")
	}

	private val _components = mutableListOf<Component>()

	private val _boundingBox = Rectangle2D()

	init {
		_components.addAll(components)
		updateBoundingBox()
	}

	val components: List<Component> get() = _components

	/** ---- [Component] */

	override val type: String get() = TYPE

	/** ---- [Locatable] */

	override var location: Point2D
		get() = Point2D(_boundingBox.x, _boundingBox.y)
		set(value) {
			invalidate()

			val offsetX = value.x - _boundingBox.x
			val offsetY = value.y - _boundingBox.y

			_boundingBox.setFrame(
				value.x, value.y,
				_boundingBox.width, _boundingBox.height
			)

			components.forEach { it.location = it.location.add(offsetX, offsetY) }

			invalidate()
			update()
		}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape get() = Rectangle2D(_boundingBox)

	override fun draw(context: DrawContext) {
		components.asReversed().forEach { it.draw(context) }
	}

	override fun contains(x: Double, y: Double): Boolean = components.any { it.contains(x, y) }

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeStorables("component", components.iterator())
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		_components.clear()
		_components.addAll(reader.readStorables("component"))
		updateBoundingBox()
	}

	/** ---- [GroupComponent] */

	private fun updateBoundingBox() {
		if (components.isNotEmpty()) {
			_boundingBox.setFrame(components[0].boundingBox)
			for (i in 1 until components.size) {
				_boundingBox.add(components[i].boundingBox)
			}
		}
	}
}