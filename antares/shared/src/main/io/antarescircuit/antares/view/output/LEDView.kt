package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.model.output.LED
import io.antarescircuit.antares.model.output.LightEmitterModel
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.help.HelpIdProvider
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.ControlViewSourceProperty
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class LEDView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: LED = LED(),
    lightColor: LightColor = DEFAULT_LIGHT_COLOR,
    ledShape: LEDShape = LEDShape.Circle,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractLEDView<LED>(styleProvider, model, ledShape, eventBus), LightEmitter, HelpIdProvider {

    companion object {
        const val PROP_ICON_PATH = "io.antarescircuit.antares.view.output.LEDView.iconPath"
        private val DEFAULT_LIGHT_COLOR = LightColor.RED
    }

	/** ---- [LightEmitter]  */

    // Cannot extract to delegate because of dependency on ControlViewSource
	override var lightColor: LightColor by ControlViewSourceProperty(lightColor, eventBus)

    override val hasGraphParameter: Boolean get() = lightColor is LightColorExpression

    /** ---- [ControlView] */

	override val controlId: String
        get() {
            // Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
            // when ControlViews (event as part of a wrapping Component) are added to a Drawing
            return "led:${model.id}"
        }

	override fun sourcePropertiesChanged(source: ControlViewSource<LED>) {
		super.sourcePropertiesChanged(source)
		if (source is LEDView) {
			lightColor = source.lightColor
		}
	}

	/** ---- [ControlViewSource] */

    override fun createControlView(): ControlView<LED> {
        val clone = LEDView(styleProvider, model, lightColor, ledShape)
        clone.isShowPortViews = false
        clone.location = Point2D(0, 0)
		copyControlViewProperties(this, clone)
        return clone
    }

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        lightColor.write("lightColor", writer)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        lightColor = LightColor.read("lightColor", reader)
    }

    /** ---- [AbstractGraphElementView] */

    override fun bind(graphView: GraphView, deep: Boolean) {
        super.bind(graphView, deep)
        graphParamsChanged(graphView.graph!!)
    }

    override fun handleStateChanged(event: GraphElementEvent) {
        super.handleStateChanged(event)
        if (event.reason == LightEmitterModel.REASON_GRAPH_PARAM_CHANGED && event.argument is Graph) {
            graphParamsChanged(event.argument as Graph)
        }
    }

    override fun graphParamsChanged(graph: Graph) {
        (lightColor as? LightColorExpression)?.let { it.evaluateIn(graph)?.let { lc -> lightColor = lc } }
    }

    /** ---- [AbstractLEDView] */

    override fun getBulbExecuteColor(): Color = lightColor.executeColor(model.isOn)

    override fun getBulbEditColor(): Color = lightColor.editColor

    override fun drawBulbEdited(context: DrawContext) {
        if (!Themes.get<DrawTheme>().dark) {
            // Special case: Blend the edit color with its alpha into the circuit's light background color
            // to give it a pastell-like look
            drawBulb(context, context.chooseBackground(transparent.applyTo(Themes.get<AntaresTheme>().background.color.backgroundColor)))
        }
        super.drawBulbEdited(context)
    }
}
