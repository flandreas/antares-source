package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

enum class GraphViewAnimationType(val customName: String) {
	None("none"),
	Animation("animation");

	companion object {
		/** The name of the [GraphViewAnimationType] property in [Properties]. */
		const val PROP_GRAPH_VIEW_ANIMATION_TYPE = "graph.view.graphViewAnimationType"

		fun withName(customName: String): GraphViewAnimationType {
			return GraphViewAnimationType.values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown type '$customName'")
		}
	}
}

data class CurrentGraphAnimationTypeEvent(val graphViewAnimationType: GraphViewAnimationType)

class CurrentGraphViewAnimationType(
	initGraphViewAnimationType: GraphViewAnimationType,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	constructor() : this(GraphViewAnimationType.withName(BaseModule.settings.getString(
		GraphViewAnimationType.PROP_GRAPH_VIEW_ANIMATION_TYPE, GraphViewAnimationType.None.customName)))

	var graphViewAnimationType: GraphViewAnimationType = initGraphViewAnimationType
		set(value) {
			if (field == value) {
				return
			}
			BaseModule.settings.set(GraphViewAnimationType.PROP_GRAPH_VIEW_ANIMATION_TYPE, value.customName)
			field = value
			eventBus.post(CurrentGraphAnimationTypeEvent(field))
		}
}
