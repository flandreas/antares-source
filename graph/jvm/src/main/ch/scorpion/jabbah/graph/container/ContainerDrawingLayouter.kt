package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Updates the layout of a [ContainerDrawing] after relevant elements
 * have been added, removed or changed.
 */
enum class ContainerDrawingLayouter(
	override val customName: String
) : EnumProperty<ContainerDrawingLayouter> {

	None("none") {
		override val doesLayout: Boolean get() = false
		override fun layout(graphView: GraphView, containerDrawing: ContainerDrawing, addLabel: Boolean) { }
	},

	Narrow("narrow") {
		override val doesLayout: Boolean get() = true

		override fun layout(graphView: GraphView, containerDrawing: ContainerDrawing, addLabel: Boolean) {
			NarrowContainerDrawingFiller(graphView, containerDrawing, addLabel).fill()
		}
	},

	Wide("wide") {
		override val doesLayout: Boolean get() = true

		override fun layout(graphView: GraphView, containerDrawing: ContainerDrawing, addLabel: Boolean) {
			WideContainerDrawingFiller(graphView, containerDrawing).fill()
		}
	};

	companion object {
		const val PROP_CONTAINER_DRAWING_LAYOUTER = "graph.containerDrawingLayouter"

		fun withName(customName: String): ContainerDrawingLayouter =
			values().firstOrNull() { it.customName == customName }
				?: throw IllegalArgumentException("unknown ContainerDrawingLayouter '$customName'")
	}

	abstract val doesLayout: Boolean

	abstract fun layout(graphView: GraphView, containerDrawing: ContainerDrawing, addLabel: Boolean = false)

	override fun toString(): String {
		return when (this) {
			None -> Translations.getString("graph.containerLayout.none")
			Narrow -> Translations.getString("graph.containerLayout.narrow")
			Wide -> Translations.getString("graph.containerLayout.wide")
		}
	}
}

object CurrentContainerDrawingLayouter {

	var value: ContainerDrawingLayouter = fromProperties

	private val fromProperties: ContainerDrawingLayouter get() =
		ContainerDrawingLayouter.withName(BaseModule.properties.getString(ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER))

	init {
		BaseModule.eventBus.register(PreferencesChangedEvent::class) { value = fromProperties }
	}
}