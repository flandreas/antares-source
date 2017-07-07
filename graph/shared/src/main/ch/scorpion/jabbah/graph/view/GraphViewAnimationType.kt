package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

enum class GraphViewAnimationType(val customName: String) {
    None("none"),
    Animation("animation");

    companion object {
        /** The name of the [GraphViewAnimationType] property in [Properties]. */
        val PROP_GRAPH_VIEW_ANIMATION_TYPE = "graph.view.graphViewAnimationType"

        fun withName(customName: String): GraphViewAnimationType {
            return GraphViewAnimationType.values().firstOrNull { it.customName == customName } ?: throw IllegalArgumentException("unknown type '$customName'")
        }
    }
}

data class CurrentGraphAnimationTypeEvent(val graphViewAnimationType: GraphViewAnimationType)

class CurrentGraphViewAnimationType(
        initGraphViewAnimationType: GraphViewAnimationType,
        private val eventBus: EventBus
) {

    companion object {
        private val PROP_NAME = "antares.view.CurrentGraphViewAnimationType"
    }

    constructor(initGraphViewAnimationType: GraphViewAnimationType): this(initGraphViewAnimationType, BaseModule.eventBus)
    constructor(): this(GraphViewAnimationType.withName(BaseModule.properties.getString(PROP_NAME, GraphViewAnimationType.None.customName)))

    var graphViewAnimationType: GraphViewAnimationType = initGraphViewAnimationType
        set(value) {
            if (field == value) {
                return
            }
            BaseModule.properties.set(PROP_NAME, value.customName)
            field = value
            eventBus.post(CurrentGraphAnimationTypeEvent(field))
        }
}
