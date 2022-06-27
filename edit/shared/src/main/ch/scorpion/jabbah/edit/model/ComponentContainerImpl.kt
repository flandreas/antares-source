package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentContainer
import ch.scorpion.jabbah.edit.StackingOrderPosition
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of a [ComponentContainer].
 */
open class ComponentContainerImpl<T: Component> : DrawableContainerImpl<T>(), ComponentContainer<T> {

    /** Determines whether this [Storable] is currently resolving child objects from persistent store */
    protected var resolvingFromStore: Boolean = false

    override fun add(drawable: T, index: Int): DrawableContainer<T> {
        if (!contains(drawable)) {
            if (!resolvingFromStore) {
                drawable.id = getMaxId() + 1
            }
            return super.add(drawable, index)
        }
        return this
    }

    /** ---- [ComponentContainer] interface */

    override fun getWithId(id: Int): T? = drawables.firstOrNull { it.id == id }

	override fun getStackingOrderPosition(componentId: Int): Int {
		val component = getWithId(componentId)
		val position = children.indexOf(component)
		if (position < 0) {
			throw NoSuchElementException("component not contained")
		}
		return position
	}

	override fun setStackingOrderPosition(position: Int, componentId: Int) {
		val component = getWithId(componentId)!!
		val currentPosition = children.indexOf(component)
		if (currentPosition < 0) {
			throw NoSuchElementException("component not contained")
		}
		if (position < 0 || position >= drawables.size) {
			throw IndexOutOfBoundsException("position $position out of bounds")
		}
		if (position == currentPosition) {
			return
		}

		children.remove(component)
		children.add(position, component)

		component.invalidate()
	}

	override fun getStackingOrderPositions(componentIds: Collection<Int>): List<StackingOrderPosition> {
		val positions = mutableListOf<StackingOrderPosition>()
		componentIds.forEach { componentId -> positions.add(StackingOrderPosition(getStackingOrderPosition(componentId), componentId)) }
		positions.sort()
		return positions
	}

    /** ---- [Storable] interface */

    override var isReading: Boolean = false

    override fun write(writer: StoreWriter) {
        writer.writeStorables("components", backToFrontIterator())
    }

    override fun read(reader: StoreReader) {
        clear()
        for (storable in reader.readStorables<Component>("components")) {
            reader.requestResolution(this, Reference(
                name = "component",
                additionalInfo = storable,
                resolveAfter = listOf(reader.getGlobalId(storable))
            ))
        }
    }

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		if (reference.name == "component") {
			try {
				resolvingFromStore = true
				@Suppress("UNCHECKED_CAST")
				add(reference.additionalInfo as T)
			} finally {
				resolvingFromStore = false
			}
		}
	}

	override fun allResolutionDone() {
		super.allResolutionDone()
		fixMissingComponentIds()
		updateBoundingBox()
	}

	/**
	 * Due du bug #401 (Auto-generating symbols incompatible with already created symbols),
	 * some [Component]s didn't store an ID, leading to possibly multiple [Component]s with ID 0.
	 */
	private fun fixMissingComponentIds() {
		drawables.filter { it.id == 0 }.forEach { it.id = getMaxId() + 1 }
	}

    /** ---- [ComponentContainerImpl] */

    /** Returns the maximum of the identifications of all contained [Component]s.*/
    private fun getMaxId(): Int {
        if (drawables.isEmpty()) {
            return 0
        }
        return drawables.maxByOrNull { it.id }!!.id
    }
}