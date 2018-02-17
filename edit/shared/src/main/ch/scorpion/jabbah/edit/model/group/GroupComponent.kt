package ch.scorpion.jabbah.edit.model.group

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Represents a collection of [Component]s that have been "grouped together" to form a single [Component].
 *
 * The users benefit of this class is to manipulate the grouped [Component]s as a whole. For example,
 * by moving the [GroupComponent], all contained child [Component]s are moved by the same offset.
 *
 * The location of this [GroupComponent] is defined as the upper left corner of the combined bounding box.
 */
class GroupComponent(components: List<Component>) : AbstractComponent() {

	/** Parameterless constructor used for deserialization.*/
	@Suppress("unused")
	constructor(): this(listOf<Component>())

	private val _components = mutableListOf<Component>()

	private val _boundingBox = Rectangle2D()

	init {
		_components.addAll(components)
		updateBoundingBox()
	}

	val components: List<Component> get() = _components

	/** ---- [Component] */

	override val type: String? get() = Translations.getString("edit.component.group")

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

	override fun getStorableChildren(): Iterator<Storable> = components.iterator()

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeStorables("component", components.iterator())
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		_components.clear()
		_components.addAll(reader.readStorables("component").map { it as Component })
		updateBoundingBox()
	}

	/** ---- [GroupComponent] */

	private fun updateBoundingBox() {
		if (!components.isEmpty()) {
			_boundingBox.setFrame(components[0].boundingBox)
			for (i in 1 until components.size) {
				_boundingBox.add(components[i].boundingBox)
			}
		}
	}
}