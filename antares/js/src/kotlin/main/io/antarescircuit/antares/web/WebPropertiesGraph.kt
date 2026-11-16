package io.antarescircuit.antares.web

import io.antarescircuit.antares.view.analog.AnalogLEDControlView
import io.antarescircuit.antares.view.inout.AbstractCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.output.LEDMatrixView
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.antares.view.output.LightEmitter
import io.antarescircuit.antares.view.output.SevenSegmentDisplayView
import io.antarescircuit.antares.view.output.LEDShape
import io.antarescircuit.antares.view.output.AbstractLEDView
import io.antarescircuit.antares.view.signal.DigitalSignalSourceControlView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.draw.drawable.Orientable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import io.antarescircuit.jabbah.graph.model.semantic.GraphSemantic
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImpl
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewImpl
import io.antarescircuit.jabbah.graph.view.net.edge.LayoutType
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewStyle
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeSignalCurveStyle
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.ControlViewVisibility
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphDisplayMode
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import io.antarescircuit.jabbah.graph.view.vertice.VerticeLabelPosition

/** `EditProperties.orientation()` */
internal fun <B : Orientable> WebPropertyBuilder<B>.orientation() =
	enum("orientation", "edit.property.Component.orientation", Direction.entries, { it.orientation }, { b, v -> b.orientation = v }, group = WebGroups.APPEARANCE)

/** `EditProperties.shadow()`: reads `shadow`, writes `customShadow`. */
internal fun <B : Component> WebPropertyBuilder<B>.shadow() =
	bool("shadow", "edit.property.shadow", { it.shadow }, { b, v -> b.customShadow = v }, group = WebGroups.APPEARANCE)

/** `EditProperties.color()` for any [Component]. */
internal fun <B : Component> WebPropertyBuilder<B>.customColor() =
	color(get = { it.customColor }, set = { b, v -> b.customColor = v })

/** `GraphProperties.modelId()` */
internal fun <B : AbstractVerticeView<*>> WebPropertyBuilder<B>.modelId() =
	readOnly("modelId", "graph.property.modelId", { it.modelId })

internal fun <B : Component> WebPropertyBuilder<B>.componentLocation() =
	location({ it.location }, { b, v -> b.location = v })

/** `GraphProperties.interactivePropagationDelay()` */
internal fun <B : Any> WebPropertyBuilder<B>.interactivePropagationDelay(get: (B) -> Long, set: (B, Long) -> Unit) =
	long("interactivePropagationDelay", "graph.property.interactivePropagationDelay", get, set, min = 0, group = WebGroups.SIMULATION)

/** Vertices whose BeanInfo is not derived from `VerticeViewBeanInfo`. */
internal fun isStandardVertice(v: AbstractVerticeView<*>): Boolean =
	v !is AbstractCircuitInOutView<*> && v !is SubGraphVerticeView<*> && v !is OscilloscopeProbeVerticeView<*> &&
		v !is OscilloscopeView && v !is AnalogLEDControlView && v !is DigitalSignalSourceControlView<*>

/** Components, vertices, edges, sub-circuits, oscilloscopes, symbol components and circuits. */
internal fun WebPropertyRegistry.registerGraphProperties() {
	// AbstractComponentBeanInfo
	define<Component> { c ->
		readOnly("id", "edit.property.id", { it.id })
		if (c !is EdgeView<*>) componentLocation()
	}

	// VerticeViewBeanInfo; subclasses hide properties with remove(...) like their isShow... overrides
	define<AbstractVerticeView<*>>({ isStandardVertice(it) }) {
		modelId()
		componentLocation()
		longValue("propagationDelay", "graph.property.propagationDelay", { it.propagationDelay }, { b, v -> b.propagationDelay = v }, group = WebGroups.SIMULATION)
		shadow()
		customColor()
		description({ it.description }, { b, v -> b.description = v })
	}
	define<LabeledRectangularVerticeView<*>>({ isStandardVertice(it) }) {
		optionalText("name", "edit.property.name", { it.name }, { b, v -> b.name = v })
		bool("showExternalLabel", "graph.labeledVerticeView.showName", { it.showExternalLabel }, { b, v -> b.showExternalLabel = v }, group = WebGroups.APPEARANCE)
	}

	// EdgeViewImplBeanInfo (DigitalEdgeView, AnalogEdgeView)
	define<EdgeViewImpl<*>> { edge ->
		readOnly("modelId", "graph.property.modelId", { it.modelId })
		bool("arrow", "graph.property.edgeView.arrow", { it.isArrow }, { b, v -> b.isArrow = v }, group = WebGroups.APPEARANCE)
		val style = edge.netView?.style
		enum(
			"layout.type", "graph.property.edgeView.layout",
			LayoutType.entries.filter { style == null || it.supportsNetViewStyle(style) },
			{ it.layout.type }, { b, v -> b.layout.type = v }, group = WebGroups.APPEARANCE,
		)
		if (style != null) {
			enum(
				"netView.style", "graph.property.edgeViewLineStyle",
				NetViewStyle.entries.filter { it.supportsLayoutType(edge.layout.type) },
				{ it.netView!!.style }, { b, v -> b.netView!!.style = v }, group = WebGroups.APPEARANCE,
			)
			color("netView.customColor", get = { it.netView?.customColor }, set = { b, v -> b.netView!!.customColor = v })
		}
		description({ it.description }, { b, v -> b.description = v })
	}

	// SubGraphVerticeViewImplBeanInfo
	define<SubGraphVerticeViewImpl> { sub ->
		modelId()
		componentLocation()
		if (sub.model.designError != null) return@define
		val paramDefs = try {
			sub.model.graphUUID?.let { LibraryModule.libraryHolder.getMetaGraph(it).graph.model?.parameterDefinitions }
		} catch (_: Throwable) {
			null
		}
		if (paramDefs == null || !paramDefs.hasAnyWithSemantic(GraphSemantic.PropagationDelay)) {
			longValue("propagationDelay", "graph.property.propagationDelay", { it.propagationDelay }, null, group = WebGroups.SIMULATION)
		}
		enum("orientation", "edit.property.Component.orientation", Direction.entries, { it.orientation }, { b, v -> b.orientation = v }, group = WebGroups.APPEARANCE)
		bool("horizontallyMirrored", "graph.property.mirrorHorizontally", { it.isHorizontallyMirrored }, { b, v -> b.isHorizontallyMirrored = v }, group = WebGroups.APPEARANCE)
		bool("verticallyMirrored", "graph.property.mirrorVertically", { it.isVerticallyMirrored }, { b, v -> b.isVerticallyMirrored = v }, group = WebGroups.APPEARANCE)
		customColor()
		enum("displayMode", "graph.property.displayMode", SubGraphDisplayMode.entries, { it.displayMode }, { b, v -> b.displayMode = v }, group = WebGroups.APPEARANCE)
		enum("controlViewVisibility", "graph.property.controlViewVisibility", ControlViewVisibility.entries, { it.controlViewVisibility }, { b, v -> b.controlViewVisibility = v })
		if (sub.label != null) {
			translatable("label", "graph.property.label", { it.label }, { b, v -> b.label = v })
		}
		// Generic parameter values (GraphParamValuePropertyFactoryRegistry) are not supported yet
		description({ it.description }, { b, v -> b.description = v })
	}

	// OscilloscopeProbeVerticeViewBeanInfo
	define<OscilloscopeProbeVerticeView<*>> {
		text("name", "edit.property.name", { it.name }, { b, v -> b.name = v })
	}

	// OscilloscopeViewBeanInfo
	define<OscilloscopeView> { osc ->
		double("persistentTimelineScale", "graph.property.oscilloscopeScale", { it.persistentTimelineScale }, { b, v -> b.persistentTimelineScale = v }, min = 0.0)
		enum("mode", "graph.property.oscilloscopeMode.type", SignalHistoriesType.entries, { it.mode }, { b, v -> b.mode = v })
		int("bufferSize", "graph.property.oscilloscope.bufferSize", { it.bufferSize }, { b, v -> b.bufferSize = v }, min = 1)
		if (osc.model.graphType.supportOscilloscopeSignalCurveStyleSelection) {
			enum("signalCurveStyle", "graph.property.oscilloscope.signalCurveStyle", OscilloscopeSignalCurveStyle.entries, { it.signalCurveStyle }, { b, v -> b.signalCurveStyle = v })
		}
	}

	// GraphViewImplBeanInfo: the circuit sheet itself
	define<GraphViewImpl> { gv ->
		readOnly("type.typeName", "graph.property.type", { it.type?.typeName })
		translatable("translatableName", "graph.property.GraphViewImpl", { it.translatableName }, { b, v -> b.translatableName = v })
		if (gv.graph?.parameterDefinitions?.hasAnyWithSemantic(GraphSemantic.PropagationDelay) != true) {
			long("effectivePropagationDelay", "graph.property.propagationDelay", { it.effectivePropagationDelay }, { b, v -> b.effectivePropagationDelay = v }, min = 0, group = WebGroups.SIMULATION)
		}
		optionalLong("graph.startupTime", "graph.property.startupTime", { it.graph?.startupTime }, { b, v -> b.graph!!.startupTime = v }, min = 0, group = WebGroups.SIMULATION)
		translatable("description", "graph.property.GraphViewImpl.shortDescription", { it.description }, { b, v -> b.description = v }, multiline = true, group = WebGroups.DOCUMENTATION)
		script("script", "graph.property.GraphViewImpl.script", { it.script }, { b, v -> b.script = v })
		bool("purelyScripted", "graph.property.GraphViewImpl.purelyScripted", { it.purelyScripted }, { b, v -> b.purelyScripted = v }, editable = gv.script.isNotEmpty())
		// Parameter definitions (GraphParamDefinitions editor) are not supported yet
	}

	// ContainerDrawingBeanInfo: the symbol sheet
	define<ContainerDrawing> {
		script("execDrawScript", "graph.property.ContainerDrawing.execDrawScript", { it.execDrawScript }, { b, v -> b.execDrawScript = v })
		enum("controlViewVisibility", "graph.property.controlViewVisibility", ControlViewVisibility.entries, { it.controlViewVisibility }, { b, v -> b.controlViewVisibility = v })
	}

	// ControlViewComponentBeanInfo, including the control view's own properties (ControlViewBeanInfo)
	define<ControlViewComponent> { cvc ->
		remove("location")
		readOnly("modelId", "graph.property.modelId", { it.modelId })
		readOnly("name", "edit.property.name", { it.name })
		if (cvc.useOrientation) orientation()
		val cv = cvc.controlView
		if (cv is LabeledRectangularVerticeView<*> && isStandardVertice(cv)) {
			bool(
				"controlView.showExternalLabel", "graph.labeledVerticeView.showName",
				{ (it.controlView as LabeledRectangularVerticeView<*>).showExternalLabel },
				{ b, v -> (b.controlView as LabeledRectangularVerticeView<*>).showExternalLabel = v },
				group = WebGroups.APPEARANCE,
			)
		}
		if (cv is SwitchView) {
			enum(
				"controlView.labelPosition", "graph.property.VerticeLabelPosition", VerticeLabelPosition.entries,
				{ (it.controlView as SwitchView).labelPosition }, { b, v -> (b.controlView as SwitchView).labelPosition = v },
				group = WebGroups.APPEARANCE,
			)
		}
		if (cv is LEDView || cv is LEDMatrixView || cv is SevenSegmentDisplayView) {
			lightColor(
				"controlView.lightColor",
				get = { (it.controlView as LightEmitter).lightColor }, set = { b, v -> (b.controlView as LightEmitter).lightColor = v!! },
				current = (cv as LightEmitter).lightColor,
			)
		}
		if (cv is AnalogLEDControlView) {
			enum(
				"controlView.ledShape", "element.property.ledShape", LEDShape.entries,
				{ (it.controlView as AbstractLEDView<*>).ledShape }, { b, v -> (b.controlView as AbstractLEDView<*>).ledShape = v },
				group = WebGroups.APPEARANCE,
			)
		}
	}

	// DigitalSignalSourceControlViewBeanInfo, AnalogLEDControlViewBeanInfo (AbstractBeanInfo: no ID/location)
	define<DigitalSignalSourceControlView<*>> {
		remove("id", "location")
		readOnly("name", "edit.property.name", { it.name }) // the setter is private
	}
	define<AnalogLEDControlView> {
		remove("id", "location")
	}
}

