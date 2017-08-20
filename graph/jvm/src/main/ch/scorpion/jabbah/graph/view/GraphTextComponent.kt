package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.text.TextComponent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.model.text.TextComponentJvm
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver

/**
 * Extends [TextComponent] to restrict visibility to a particular [Scenario] or [ScenarioStep].
 */
class GraphTextComponent(
    text: String = "",
    location: Point2D = Point2D(),
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    eventBus: EventBus = BaseModule.eventBus
) : TextComponentJvm(text, location, GraphStyleType.EXPLANATION, styleProvider) {

    /**
     * If present, this [GraphTextComponent] is only visible if the [Scenario] is the same as the current
     * [Scenario] of the [Graph] of the [GraphView] that contains this [GraphTextComponent].
     */
    var scenario: Scenario? = null
        set(value) {
            invalidate()
            field = value
            validate()
        }

    /**
     * If present, this [GraphTextComponent] is only visible if the [ScenarioStep] is the same as the current
     * [ScenarioStep] of the [Graph] of the [GraphView] that contains this [GraphTextComponent].
     */
    var scenarioStep: ScenarioStep? = null
        set(value) {
            invalidate()
            field = value
            validate()
        }

    init {
        eventBus.register(ScenarioStepEvent::class, {
            if (it.graphView === parent) {
                invalidate()
                validate()
            }
        })
    }

    /** ---- [Drawable] */

    override var visible: Boolean
        get() {
            if (scenario == null && scenarioStep == null) {
                return super.visible
            }
            val graphView = parent as GraphView<*>?
            if (graphView != null) {
                return graphView.currentScenarioStep != null && graphView.currentScenarioStep === scenarioStep
                        || graphView.currentScenario != null && graphView.currentScenario === scenario
            }
            return super.visible
        }
        set(value) { super.visible = value}

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (scenario != null) {
            writer.writeInt("scenario", writer.provideIdentity(scenario!!))
        }
        if (scenarioStep != null) {
            writer.writeInt("scenarioStep", writer.provideIdentity(scenarioStep!!))
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("scenario")) {
            reader.requestResolution(this, Reference(
                name = "scenario",
                referenceId = reader.readInt("scenario")
            ))
        }
        if (reader.hasAttribute("scenarioStep")) {
            reader.requestResolution(this, Reference(
                name = "scenarioStep",
                referenceId = reader.readInt("scenarioStep")
            ))
        }
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        super.resolve(reference, referenceResolver)
        if (reference.name == "scenario") {
            scenario = referenceResolver.getStorable(reference.referenceId) as Scenario
        }
        if (reference.name == "scenarioStep") {
            scenarioStep = referenceResolver.getStorable(reference.referenceId) as ScenarioStep
        }
    }
}