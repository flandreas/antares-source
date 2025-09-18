package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.LED
import ch.scorpion.antares.model.output.LightEmitterModel
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.help.HelpIdProvider
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.ControlViewSourceProperty
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class LEDView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: LED = LED(),
    lightColor: LightColor = DEFAULT_LIGHT_COLOR,
    square: Boolean = false,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractLEDView<LED>(styleProvider, model, square, eventBus), LightEmitter, HelpIdProvider {

    companion object {
        const val PROP_ICON_PATH = "ch.scorpion.antares.view.output.LEDView.iconPath"
        private val DEFAULT_LIGHT_COLOR = LightColor.RED
    }

	/** ---- [LightEmitter]  */

    // Cannot extract to delegate because of dependency on ControlViewSource
	override var lightColor: LightColor by ControlViewSourceProperty(lightColor, eventBus)

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
        val clone = LEDView(styleProvider, model, lightColor, square)
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

    private fun graphParamsChanged(graph: Graph) {
        (lightColor as? LightColorExpression)?.let { it.evaluateIn(graph)?.let { lc -> lightColor = lc } }
    }

    /** ---- [AbstractLEDView] */

    override fun getBulbExecuteColor(): Color = lightColor.executeColor(model.isOn)

    override fun getBulbEditColor(): Color = lightColor.editColor

    override fun drawBulbEdited(context: DrawContext) {
        if (!Themes.get<DrawTheme>().dark) {
            // Special case: Blend the edit color with its alpha into the circuit's light background color
            // to give it a pastell-like look
            drawBulb(context, transparent.applyTo(Themes.get<AntaresTheme>().background.color.backgroundColor))
        }
        super.drawBulbEdited(context)
    }
}
