package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.Properties

enum class GraphViewAnimationType(val customName: String) {
	None("none"),
	Animation("animation");

	companion object {

		/** The name of the [String] property in [Properties] containing the name of [GraphViewAnimationType]. */
		const val PROP_GRAPH_VIEW_ANIMATION_TYPE = "graph.view.graphViewAnimationType"

		fun withName(customName: String): GraphViewAnimationType {
			return values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown GraphViewAnimationType '$customName'")
		}
	}
}

data class CurrentGraphAnimationTypeEvent(val graphViewAnimationType: GraphViewAnimationType)

class CurrentGraphViewAnimationType(
	initGraphViewAnimationType: GraphViewAnimationType,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	constructor() : this(GraphViewAnimationType.withName(BaseModule.properties.getString(
		GraphViewAnimationType.PROP_GRAPH_VIEW_ANIMATION_TYPE)))

	var graphViewAnimationType: GraphViewAnimationType = initGraphViewAnimationType
		set(value) {
			if (field == value) {
				return
			}
			BaseModule.properties.customize(GraphViewAnimationType.PROP_GRAPH_VIEW_ANIMATION_TYPE, value.customName)
			field = value
			eventBus.post(CurrentGraphAnimationTypeEvent(field))
		}
}
