package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.EnterBehavior
import io.antarescircuit.antares.model.InputPortNumber
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.OutputAnnotation
import io.antarescircuit.antares.model.addressable.MemoryStorableIdentification
import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.Trigger
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.net.NetSignalApplierStrategy
import io.antarescircuit.antares.model.net.PullDirection
import io.antarescircuit.antares.model.net.TransistorType
import io.antarescircuit.antares.model.net.TunnelName
import io.antarescircuit.antares.model.output.SevenSegmentDisplayScheme
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.antares.view.PortViewSpacing
import io.antarescircuit.antares.view.addressable.AbstractAddressableView
import io.antarescircuit.antares.view.addressable.LookupTableView
import io.antarescircuit.antares.view.addressable.RAMView
import io.antarescircuit.antares.view.addressable.ROMView
import io.antarescircuit.antares.view.app.AntaresGraphViewService
import io.antarescircuit.antares.view.arithmetic.BitExtenderView
import io.antarescircuit.antares.view.arithmetic.RandomView
import io.antarescircuit.antares.view.container.DigitalPortViewComponent
import io.antarescircuit.antares.view.gate.DelayGateView
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.gate.TriStateBufferGateView
import io.antarescircuit.antares.view.inout.AbstractCircuitInOutView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.ClockView
import io.antarescircuit.antares.view.input.DipSwitchView
import io.antarescircuit.antares.view.input.DoubleThrowSwitchView
import io.antarescircuit.antares.view.input.ImageSwitchView
import io.antarescircuit.antares.view.input.JoystickDeflection
import io.antarescircuit.antares.view.input.JoystickView
import io.antarescircuit.antares.view.input.KeyboardView
import io.antarescircuit.antares.view.input.RealSwitchView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.net.AbstractBranchCountSplitterView
import io.antarescircuit.antares.view.net.AbstractTransistorView
import io.antarescircuit.antares.view.net.BreakView
import io.antarescircuit.antares.view.net.ConcentratorView
import io.antarescircuit.antares.view.net.ConstantView
import io.antarescircuit.antares.view.net.GroundView
import io.antarescircuit.antares.view.net.PowerOnResetView
import io.antarescircuit.antares.view.net.PowerView
import io.antarescircuit.antares.view.net.ProbeView
import io.antarescircuit.antares.view.net.PullResistorView
import io.antarescircuit.antares.view.net.SplitterView
import io.antarescircuit.antares.view.net.TransistorView
import io.antarescircuit.antares.view.net.TransistorViewSymbol
import io.antarescircuit.antares.view.net.WireTapView
import io.antarescircuit.antares.view.net.tunnel.TunnelFlowDirection
import io.antarescircuit.antares.view.net.tunnel.TunnelView
import io.antarescircuit.antares.view.net.tunnel.TunnelViewFace
import io.antarescircuit.antares.view.output.BuzzerView
import io.antarescircuit.antares.view.output.LEDMatrixView
import io.antarescircuit.antares.view.output.LEDShape
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.RgbLEDView
import io.antarescircuit.antares.view.output.SevenSegmentDisplayView
import io.antarescircuit.antares.view.output.SixteenSegmentDisplayView
import io.antarescircuit.antares.view.output.TerminalView
import io.antarescircuit.antares.view.output.VideoRamColorModel
import io.antarescircuit.antares.view.output.VideoRamView
import io.antarescircuit.antares.view.output.AbstractLEDView
import io.antarescircuit.antares.view.output.AbstractSegmentDisplayView
import io.antarescircuit.antares.view.port.DigitalPortViewStyle
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.sound.WaveformType
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.graph.container.InternalLabelOrientation
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.vertice.VerticeLabelPosition

private const val SIGNAL_REPRESENTATION = "element.property.DigitalSignalRepresentation"

/** The service the desktop uses for structural changes such as changing the number of ports. */
private fun antaresService(): AntaresGraphViewService =
	EditModule.drawingAppService as? AntaresGraphViewService ?: AntaresGraphViewService()

@Suppress("UNCHECKED_CAST")
private fun Editor.graphDrawingView() = view as DrawingView<GraphElementView<*>, GraphView>

/** `AntaresProperties.handedness(baseKey)` */
private fun <B : Any> WebPropertyBuilder<B>.handedness(baseKey: String, get: (B) -> Handedness, set: (B, Handedness) -> Unit, editable: Boolean = true) =
	enum("handedness", baseKey, Handedness.entries, get, set, editable = editable, group = WebGroups.APPEARANCE)

/** `AntaresProperties.signalRepresentation()`, by default without fixed point like most BeanInfos. */
private fun <B : Any> WebPropertyBuilder<B>.signalRepresentation(
	get: (B) -> DigitalSignalRepresentation, set: (B, DigitalSignalRepresentation) -> Unit, withFixedPoint: Boolean = false,
) = enum(
	"signalRepresentation", SIGNAL_REPRESENTATION,
	DigitalSignalRepresentation.entries.filter { withFixedPoint || it != DigitalSignalRepresentation.FIXED_POINT },
	get, set,
)

/** `EditProperties.size()` */
private fun <B : Any> WebPropertyBuilder<B>.size(
	get: (B) -> Size, set: (B, Size) -> Unit, baseKey: String = "edit.property.size", editable: Boolean = true, key: String = "size",
) = enum(key, baseKey, Size.entries, get, set, editable = editable, group = WebGroups.APPEARANCE)

/** `EditProperties.border()` */
private fun <B : Any> WebPropertyBuilder<B>.hasBorder(get: (B) -> Boolean, set: (B, Boolean) -> Unit) =
	bool("hasBorder", "edit.property.hasBorder", get, set, group = WebGroups.APPEARANCE)

private fun <B : Any> WebPropertyBuilder<B>.toggle(get: (B) -> Boolean, set: (B, Boolean) -> Unit) =
	bool("toggle", "element.property.Switch.toggle", get, set)

private fun <B : Any> WebPropertyBuilder<B>.portViewSpacing(get: (B) -> PortViewSpacing, set: (B, PortViewSpacing) -> Unit) =
	enum("portViewSpacing", "element.property.portViewSpacing", PortViewSpacing.entries, get, set, group = WebGroups.APPEARANCE)

/** Hides the propagation delay like `isShowPropagationDelay = false`. */
private fun WebPropertyBuilder<*>.hidePropagationDelay() = remove("propagationDelay")

/** Digital components of the Antares standard libraries and the digital circuit sheet. */
internal fun WebPropertyRegistry.registerDigitalProperties() {
	registerDigitalBase()
	registerGates()
	registerInputs()
	registerNets()
	registerOutputs()
	registerMemories()
	registerInOut()
}

private fun WebPropertyRegistry.registerDigitalBase() {
	// DigitalComponentViewBeanInfo / AnalogComponentViewBeanInfo: orientation, except for the
	// BeanInfos directly derived from VerticeViewBeanInfo
	define<OrientableRectangularVerticeView<*>>({
		it.useOrientation && isStandardVertice(it) && it !is KeyboardView && it !is LEDMatrixView && it !is AbstractSegmentDisplayView<*> &&
			it !is TerminalView && it !is VideoRamView
	}) {
		orientation()
	}

	// DigitalGraphViewBeanInfo
	define<DigitalGraphView> {
		add(WebProperty<DigitalGraphView, LightColor?>(
			"defaultLightColor", "element.property.DigitalGraphView.lightColor", WebPropertyType.Color,
			get = { it.defaultLightColor },
			choices = {
				listOf(WebChoice<LightColor?>("", Translations.getString("$SIGNAL_REPRESENTATION.none"), null)) +
					LightColor.PREDEFINED.map { WebChoice<LightColor?>(it.customName, it.toString(), it, it.onColor.toCss()) }
			},
			group = WebGroups.APPEARANCE,
			apply = { gv, value, editor ->
				// Like DefaultLightColorProperty: optionally apply the new default to all existing components
				val change = { replace: Boolean ->
					runPropertyCommand(editor, "element.property.DigitalGraphView.lightColor", emptyArray(), listOf(gv), value, { (it as DigitalGraphView).defaultLightColor }) { b, v ->
						(b as DigitalGraphView).defaultLightColor = v
						if (replace) antaresService().replaceLightColor(b)
					}
				}
				if (value != null && value != gv.defaultLightColor) {
					WebPrompts.confirm(Translations.getString("antares.action.replaceLightColor.question"), change)
				} else {
					change(false)
				}
			},
		))
		optionalEnum(
			"defaultSignalRepresentation", "element.property.DigitalGraphView.signalRepresentation", DigitalSignalRepresentation.entries,
			{ it.defaultSignalRepresentation }, { b, v -> b.defaultSignalRepresentation = v },
			nullLabel = Translations.getString("$SIGNAL_REPRESENTATION.none"),
		)
		enum("netSignalApplierStrategy", "element.property.netSignalApplierStrategy", NetSignalApplierStrategy.entries, { it.netSignalApplierStrategy }, { b, v -> b.netSignalApplierStrategy = v }, group = WebGroups.SIMULATION)
		size({ it.defaultLogicGateSize }, { b, v -> b.defaultLogicGateSize = v }, baseKey = "element.property.defaultLogicGateSize", key = "defaultLogicGateSize")
	}

	// DigitalPortViewComponentBeanInfo (symbol editor)
	define<DigitalPortViewComponent> { pvc ->
		remove("id", "location")
		readOnly("portId", "graph.property.PortId", { it.portId })
		readOnly("port.name", "edit.property.name", { it.port.name })
		enum("portViewStyle", "element.property.DigitalPortViewStyle", DigitalPortViewStyle.entries, { it.portViewStyle }, { b, v -> b.portViewStyle = v }, group = WebGroups.APPEARANCE)
		enum("direction", "graph.property.direction", Direction.entries, { it.direction }, { b, v -> b.direction = v }, group = WebGroups.APPEARANCE)
		enum("portLabelPosition", "graph.property.PortLabelPosition", PortLabelPosition.entries, { it.portLabelPosition }, { b, v -> b.portLabelPosition = v }, group = WebGroups.APPEARANCE)
		if (pvc.portLabelPosition == PortLabelPosition.INTERNAL) {
			enum("internalLabelOrientation", "graph.property.internalLabelOrientation", InternalLabelOrientation.entries, { it.internalLabelOrientation }, { b, v -> b.internalLabelOrientation = v }, group = WebGroups.APPEARANCE)
		}
		bool(
			"largeExternalPortLabelDistance", "element.property.DigitalPortViewComponent.largeLabelDist",
			{ it.largeExternalPortLabelDistance }, { b, v -> b.largeExternalPortLabelDistance = v },
			editable = pvc.portLabelPosition == PortLabelPosition.EXTERNAL, group = WebGroups.APPEARANCE,
		)
		bool("showBitWidthAnnotation", "element.property.DigitalPortViewComponent.showBitWidthAnnotation", { it.showBitWidthAnnotation }, { b, v -> b.showBitWidthAnnotation = v }, group = WebGroups.APPEARANCE)
		enum("logic", "element.property.logic", Logic.entries, { it.logic }, { b, v -> b.logic = v })
		if (pvc.port.portType == PortType.INPUT) {
			enum("trigger", "element.property.trigger", Trigger.entries, { it.trigger }, { b, v -> b.trigger = v })
		}
		if (pvc.port.portType == PortType.OUTPUT) {
			enum("outputAnnotation", "element.property.outputAnnotation", OutputAnnotation.entries, { it.outputAnnotation }, { b, v -> b.outputAnnotation = v })
		}
	}
}

private fun WebPropertyRegistry.registerGates() {
	// LogicGateViewBeanInfo
	define<LogicGateView> { gate ->
		if (gate.model.maxInputCount.count > 1) {
			val min = gate.model.minInputCount.count
			val max = gate.model.maxInputCount.count
			val counts = PortCount.entries.filter { it.count >= 2 && it.count in min..max }
			add(WebProperty<LogicGateView, PortCount>(
				"chosenInputCount", PortCount.INPUT_COUNT_BASE_KEY, WebPropertyType.Choice,
				get = { it.chosenInputCount },
				choices = { counts.map { WebChoice(it.name, it.toString(), it) } },
				group = WebGroups.PORTS,
				apply = { g, value, editor ->
					// Like ChangeInputCountCommandSwing: not undoable due to possibly removed connections
					runPropertyCommand(editor, PortCount.INPUT_COUNT_BASE_KEY, emptyArray(), listOf(g), value, { (it as LogicGateView).chosenInputCount }, undoable = false) { b, v ->
						antaresService().changeInputCount(b as LogicGateView, v!!, editor.graphDrawingView())
					}
				},
			))
		}
		size({ it.size }, { b, v -> b.size = v })
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		optionalText("outputPortName", "element.property.outputPort", { it.outputPortName }, { b, v -> b.outputPortName = v }, group = WebGroups.PORTS)
		if (gate.model.gateType == NonUnaryLogicGateType.And) {
			choice(
				"dataPort", "element.property.AndGate.dataPort",
				InputPortNumber.entries.filter { it.id <= gate.chosenInputCount.count },
				{ it.dataPort }, { b, v -> b.dataPort = v!! }, id = { it.name }, group = WebGroups.PORTS,
			)
		}
		for (portId in 1..gate.chosenInputCount.count) {
			optionalText(
				"inputPortName$portId", "element.property.inputPort",
				{ it.getInputPortName(portId) }, { b, v -> b.setInputPortName(portId, v) },
				group = WebGroups.PORTS, baseKeyParams = arrayOf(portId),
			)
			bool(
				"negateInput$portId", "element.property.Gate.negateInput",
				{ it.getInputNegation(portId) }, { b, v -> b.setInputNegation(portId, v) },
				group = WebGroups.PORTS, baseKeyParams = arrayOf(portId),
			)
		}
	}

	// TriStateBufferGateViewBeanInfo
	define<TriStateBufferGateView> {
		size({ it.size }, { b, v -> b.size = v })
		enum("enableLogic", "element.property.logic", Logic.entries, { it.enableLogic }, { b, v -> b.enableLogic = v })
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		optionalText("inputPortName", "element.property.inputPort", { it.inputPortName }, { b, v -> b.inputPortName = v }, group = WebGroups.PORTS, baseKeyParams = arrayOf(1))
		optionalText("outputPortName", "element.property.outputPort", { it.outputPortName }, { b, v -> b.outputPortName = v }, group = WebGroups.PORTS)
		handedness("element.property.TriStateBuffer.handedness", { it.handedness }, { b, v -> b.handedness = v })
	}

	// DelayGateViewBeanInfo
	define<DelayGateView> {
		hidePropagationDelay()
		magnitude("delay", "element.property.DelayGate.delay", { it.delay }, { b, v -> b.delay = v }, SIUnit.Second)
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		orientation()
	}

	// BitExtenderViewBeanInfo
	define<BitExtenderView> { be ->
		bitWidth("inputBitWidth", BitExtenderView.INPUT_BIT_WIDTH_BASE_KEY, { it.inputBitWidth }, { b, v -> b.inputBitWidth = v }, filter = { it.width <= be.outputBitWidth.width })
		bitWidth("outputBitWidth", BitExtenderView.OUTPUT_BIT_WIDTH_BASE_KEY, { it.outputBitWidth }, { b, v -> b.outputBitWidth = v }, filter = { it.width >= be.inputBitWidth.width })
	}

	// RandomViewBeanInfo
	define<RandomView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
	}
}

private fun WebPropertyRegistry.registerInputs() {
	// SwitchViewBeanInfo (push button and toggle switch)
	define<SwitchView> { sw ->
		hidePropagationDelay()
		interactivePropagationDelay({ it.interactivePropagationDelay }, { b, v -> b.interactivePropagationDelay = v })
		optionalText("name", "edit.property.name", { it.name }, { b, v -> b.name = v })
		toggle({ it.toggle }, { b, v -> b.toggle = v })
		enum("labelPosition", "graph.property.VerticeLabelPosition", VerticeLabelPosition.entries, { it.labelPosition }, { b, v -> b.labelPosition = v }, group = WebGroups.APPEARANCE)
		if (!sw.toggle) {
			long("minOnTime", "element.property.Switch.minOnTime", { it.minOnTime }, { b, v -> b.minOnTime = v }, min = 0)
		}
		bool("closedOnStart", "element.property.Switch.closedOnStart", { it.closedOnStart }, { b, v -> b.closedOnStart = v })
	}

	// RealSwitchViewBeanInfo, DoubleThrowSwitchViewBeanInfo
	define<RealSwitchView> {
		hidePropagationDelay()
		interactivePropagationDelay({ it.interactivePropagationDelay }, { b, v -> b.interactivePropagationDelay = v })
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		toggle({ it.toggle }, { b, v -> b.toggle = v })
	}
	define<DoubleThrowSwitchView> {
		hidePropagationDelay()
		interactivePropagationDelay({ it.interactivePropagationDelay }, { b, v -> b.interactivePropagationDelay = v })
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		toggle({ it.toggle }, { b, v -> b.toggle = v })
	}

	// DipSwitchViewBeanInfo
	define<DipSwitchView> {
		hidePropagationDelay()
		interactivePropagationDelay({ it.interactivePropagationDelay }, { b, v -> b.interactivePropagationDelay = v })
		optionalText("name", "edit.property.name", { it.name }, { b, v -> b.name = v })
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		long("initialValue", "element.property.DipSwitch.initialValue", { it.initialValue }, { b, v -> b.initialValue = v }, min = 0)
		bool("retainValue", "element.property.DipSwitch.retainValue", { it.retainValue }, { b, v -> b.retainValue = v })
	}

	// ImageSwitchViewBeanInfo
	define<ImageSwitchView> { sw ->
		hidePropagationDelay()
		interactivePropagationDelay({ it.interactivePropagationDelay }, { b, v -> b.interactivePropagationDelay = v })
		toggle({ it.toggle }, { b, v -> b.toggle = v })
		enum("portDirection", "library.element.ImageSwitch.portDirection", Direction.entries, { it.portDirection }, { b, v -> b.portDirection = v })
		image("onImageId", "library.element.ImageSwitch.onImage", { it.onImageUuid }, { b, v -> b.onImageUuid = v })
		image("offImageId", "library.element.ImageSwitch.offImage", { it.offImageUuid }, { b, v -> b.offImageUuid = v })
		double("scale", "library.element.ImageSwitch.scale", { it.scale }, { b, v -> b.scale = v }, min = 0.01, group = WebGroups.APPEARANCE)
		if (!sw.toggle) {
			long("minOnTime", "element.property.Switch.minOnTime", { it.minOnTime }, { b, v -> b.minOnTime = v }, min = 0)
		}
	}

	// ClockViewBeanInfo
	define<ClockView> {
		hidePropagationDelay()
		magnitude("periodOrFrequency", "element.property.ClockView.periodOrFrequency", { it.periodOrFrequency }, { b, v -> b.periodOrFrequency = v }, SIUnit.Second, SIUnit.Hertz)
		double("offPercentage", "element.property.ClockView.offPercentage", { it.offPercentage }, { b, v -> b.offPercentage = v }, min = 0.0, max = 100.0)
		bool("enabled", "element.property.ClockView.enabled", { it.isEnabled }, { b, v -> b.isEnabled = v })
		bool("knobEnabled", "element.property.ClockView.knobEnabled", { it.isKnobEnabled }, { b, v -> b.isKnobEnabled = v })
	}

	// JoystickViewBeanInfo
	define<JoystickView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		enum("deflection", "element.property.joystickDeflection", JoystickDeflection.entries, { it.deflection }, { b, v -> b.deflection = v })
	}

	// KeyboardViewBeanInfo
	define<KeyboardView> {
		int("bufferSize", "element.property.bufferSize", { it.bufferSize }, { b, v -> b.bufferSize = v }, min = 1)
		enum("enterBehavior", "element.property.Keyboard.enterBehavior", EnterBehavior.entries, { it.enterBehavior }, { b, v -> b.enterBehavior = v })
	}
}

private fun WebPropertyRegistry.registerNets() {
	// SplitterViewBeanInfo, ConcentratorViewBeanInfo, BidirectionalSplitterViewBeanInfo
	define<AbstractBranchCountSplitterView<*>> { sp ->
		if (sp is SplitterView || sp is ConcentratorView) hidePropagationDelay()
		val connected = sp.model.isConnected
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v }, editable = !connected)
		choice(
			"branchCount", "element.property.branchCount", sp.model.supportedBranchCounts,
			{ it.branchCount }, { b, v -> b.branchCount = v!! }, id = { it.count.toString() }, editable = !connected,
		)
		handedness("element.property.Splitter.handedness", { it.handedness }, { b, v -> b.handedness = v })
		portViewSpacing({ it.portViewSpacing }, { b, v -> b.portViewSpacing = v })
		signalRepresentation({ it.signalRepresentation }, { b, v -> b.signalRepresentation = v })
	}

	// BreakViewBeanInfo
	define<BreakView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		long("value", "element.property.Break.value", { it.value }, { b, v -> b.value = v })
	}

	// ConstantViewBeanInfo
	define<ConstantView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		signalRepresentation({ it.signalRepresentation }, { b, v -> b.signalRepresentation = v })
		longValue("value", "element.property.Constant.value", { it.value }, { b, v -> b.value = v })
	}

	// GroundViewBeanInfo, PowerViewBeanInfo, PowerOnResetViewBeanInfo
	define<GroundView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
	}
	define<PowerView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
	}
	define<PowerOnResetView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		enum("logic", "element.property.logic", Logic.entries, { it.logic }, { b, v -> b.logic = v })
	}

	// ProbeViewBeanInfo
	define<ProbeView> { probe ->
		optionalText("name", "edit.property.name", { it.name }, { b, v -> b.name = v })
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		signalRepresentation({ it.signalRepresentation }, { b, v -> b.signalRepresentation = v }, withFixedPoint = true)
		bool("hasOutput", "element.property.hasOutput", { it.hasOutput }, { b, v -> b.hasOutput = v }, editable = !probe.model.isConnected)
		bool("logging", "element.property.logging", { it.isLogging }, { b, v -> b.isLogging = v }, group = WebGroups.SIMULATION)
		if (probe.signalRepresentation == DigitalSignalRepresentation.FIXED_POINT) {
			int("fixedPointFractionSize", "element.property.fixedPointConfig.fractionSize", { it.fixedPointFractionSize ?: 0 }, { b, v -> b.fixedPointFractionSize = v }, min = 0)
			bool("fixedPointSigned", "element.property.fixedPointConfig.signed", { it.fixedPointSigned ?: false }, { b, v -> b.fixedPointSigned = v })
		}
	}

	// PullResistorViewBeanInfo
	define<PullResistorView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		enum("pullDirection", "element.property.PullDirection", PullDirection.entries, { it.pullDirection }, { b, v -> b.pullDirection = v })
	}

	// TransistorViewBeanInfo (AnalogTransistorView shares the AbstractTransistorView properties)
	define<AbstractTransistorView<*>> {
		enum("transistorType", "element.property.transistorType", TransistorType.entries, { it.transistorType }, { b, v -> b.transistorType = v })
		enum("symbol", "element.property.transistorSymbol", TransistorViewSymbol.entries, { it.symbol }, { b, v -> b.symbol = v }, group = WebGroups.APPEARANCE)
	}
	define<TransistorView> {
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		handedness("element.property.handedness", { it.handedness }, { b, v -> b.handedness = v })
	}

	// WireTapViewBeanInfo
	define<WireTapView> { tap ->
		val connected = tap.model.isConnected
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v }, baseKey = "library.element.WireTap.inputBitWidth", editable = !connected)
		bitWidth(
			"narrowSideBitWidth", "library.element.WireTap.narrowSideBitWidth", { it.narrowSideBitWidth }, { b, v -> b.narrowSideBitWidth = v },
			editable = !connected, filter = { it.width <= tap.bitWidth.width },
		)
		handedness("library.element.WireTap.handedness", { it.handedness }, { b, v -> b.handedness = v }, editable = !connected)
		portViewSpacing({ it.portViewSpacing }, { b, v -> b.portViewSpacing = v })
		val counts = PortCount.entries.filter { it.count in 1..8 }
		add(WebProperty<WireTapView, PortCount>(
			"tapCount", PortCount.OUTPUT_COUNT_BASE_KEY, WebPropertyType.Choice,
			get = { it.tapCount },
			choices = { counts.map { WebChoice(it.name, it.toString(), it) } },
			group = WebGroups.PORTS,
			apply = { t, value, editor ->
				// Like ChangeOutputCountCommandSwing: not undoable due to possibly removed connections
				runPropertyCommand(editor, PortCount.OUTPUT_COUNT_BASE_KEY, emptyArray(), listOf(t), value, { (it as WireTapView).tapCount }, undoable = false) { b, v ->
					antaresService().changeOutputCount(b as WireTapView, v!!, editor.graphDrawingView())
				}
			},
		))
		for (i in 0 until tap.tapCount.count) {
			int(
				"tapPosition$i", "library.element.WireTap.tapPosition",
				{ it.model.getTapPosition(i) }, { b, v -> b.model.setTapPosition(i, v) },
				min = 0, max = tap.bitWidth.width - 1, group = WebGroups.PORTS, baseKeyParams = arrayOf(i),
			)
		}
	}

	// TunnelViewBeanInfo; the name is edited by the special TunnelNameProperty
	define<TunnelView> {
		remove("name", "showExternalLabel")
		add(WebProperty<TunnelView, TunnelName>(
			"tunnelName", "edit.property.name", WebPropertyType.Text,
			get = { it.tunnelName },
			toJs = { it?.name ?: "" },
			fromJs = { v -> jsString(v)?.takeIf { it.isNotEmpty() }?.let { TunnelName(it) } },
			apply = { tunnel, value, editor -> changeTunnelName(tunnel, value, editor) },
		))
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		if (TunnelView.face == TunnelViewFace.ARROW) {
			enum("flowDirection", "element.property.tunnelFlowDirection", TunnelFlowDirection.entries, { it.flowDirection }, { b, v -> b.flowDirection = v })
		}
		bool("global", "element.property.tunnelIsGlobal", { it.isGlobal }, { b, v -> b.isGlobal = v })
	}
}

/**
 * Like `TunnelNameProperty`: if other tunnels share the old name, the user is asked whether to rename them as well
 * (not undoable, like `ChangeTunnelNamesCommandSwing`).
 */
private fun changeTunnelName(tunnel: TunnelView, value: TunnelName?, editor: Editor) {
	val oldName = tunnel.tunnelName?.name
	val getter: (Any) -> TunnelName? = { (it as TunnelView).tunnelName }
	fun sameNamed() = editor.drawing.getDrawables { it is TunnelView && !oldName.isNullOrEmpty() && it.tunnelName?.name == oldName }
	val change = { renameAll: Boolean ->
		if (renameAll) {
			runPropertyCommand(editor, "antares.tunnelName.command", emptyArray(), listOf(tunnel), value, getter, undoable = false) { _, v ->
				sameNamed().forEach { (it as TunnelView).tunnelName = v }
			}
		} else {
			runPropertyCommand(editor, "edit.property.name", emptyArray(), listOf(tunnel), value, getter) { b, v -> (b as TunnelView).tunnelName = v }
		}
	}
	if (sameNamed().size > 1 && value?.name != oldName) {
		WebPrompts.confirm(Translations.getString("antares.tunnelName.question.text"), change)
	} else {
		change(false)
	}
}

private fun WebPropertyRegistry.registerOutputs() {
	// LEDViewBeanInfo
	define<LEDView> { led ->
		remove("customColor")
		lightColor(get = { it.lightColor }, set = { b, v -> b.lightColor = v!! }, current = led.lightColor)
	}
	// LEDViewBeanInfo, RgbLEDViewBeanInfo
	define<AbstractLEDView<*>>({ it is LEDView || it is RgbLEDView }) {
		enum("ledShape", "element.property.ledShape", LEDShape.entries, { it.ledShape }, { b, v -> b.ledShape = v }, group = WebGroups.APPEARANCE)
		size({ it.size }, { b, v -> b.size = v })
		hasBorder({ it.hasBorder }, { b, v -> b.hasBorder = v })
	}

	// LEDMatrixViewBeanInfo
	define<LEDMatrixView> { m ->
		bitWidth("columnWidth", "element.property.columns", { it.columnWidth }, { b, v -> b.columnWidth = v })
		bitWidth("rowWidth", "element.property.rows", { it.rowWidth }, { b, v -> b.rowWidth = v })
		lightColor(get = { it.lightColor }, set = { b, v -> b.lightColor = v!! }, current = m.lightColor)
		size({ it.size }, { b, v -> b.size = v })
		long("afterglowDuration", "element.property.LEDMatrix.afterglow", { it.afterglowDuration }, { b, v -> b.afterglowDuration = v }, min = 0, group = WebGroups.APPEARANCE)
		bool("circleDots", "element.property.LEDMatrix.isCircleDots", { it.isCircleDots }, { b, v -> b.isCircleDots = v }, group = WebGroups.APPEARANCE)
		bool("debug", "element.property.isDebug", { it.isDebug }, { b, v -> b.isDebug = v })
	}

	// SevenSegmentDisplayViewBeanInfo, SixteenSegmentDisplayViewBeanInfo
	define<AbstractSegmentDisplayView<*>> { d ->
		val connected = d.model.isConnected
		lightColor(get = { it.lightColor }, set = { b, v -> b.lightColor = v!! }, current = d.lightColor)
		if (d is SevenSegmentDisplayView && d.size == Size.LARGE) {
			enum(
				"portScheme", "element.property.SevenSegmentDisplayScheme", SevenSegmentDisplayScheme.entries,
				{ (it as SevenSegmentDisplayView).portScheme }, { b, v -> (b as SevenSegmentDisplayView).portScheme = v },
				editable = !connected, group = WebGroups.PORTS,
			)
		}
		enum("logic", "element.property.segmentDisplay.logic", Logic.entries, { it.logic }, { b, v -> b.logic = v })
		size({ it.size }, { b, v -> b.size = v }, editable = !connected)
		hasBorder({ it.hasBorder }, { b, v -> b.hasBorder = v })
	}

	// TerminalViewBeanInfo
	define<TerminalView> { t ->
		int("rowsCount", "element.property.Terminal.rowsCount", { it.rowsCount }, { b, v -> b.rowsCount = v }, min = 1)
		int("columnsCount", "element.property.Terminal.columnsCount", { it.columnsCount }, { b, v -> b.columnsCount = v }, min = 1)
		size({ it.size }, { b, v -> b.size = v })
		lightColor(
			"lightColor", "element.property.Terminal.textColor", { it.lightColor }, { b, v -> b.lightColor = v }, current = t.lightColor,
			nullLabel = Translations.getString("edit.style.property.fromStyle.name"),
		)
		handedness("element.property.Terminal.handedness", { it.handedness }, { b, v -> b.handedness = v })
	}

	// VideoRamViewBeanInfo
	define<VideoRamView> {
		bitWidth("dataWidth", "element.property.dataBitWidth", { it.dataWidth }, { b, v -> b.dataWidth = v })
		int("columnsCount", "element.property.VideoRam.width", { it.columnsCount }, { b, v -> b.columnsCount = v }, min = 1)
		int("rowsCount", "element.property.VideoRam.height", { it.rowsCount }, { b, v -> b.rowsCount = v }, min = 1)
		size({ it.size }, { b, v -> b.size = v }, baseKey = "element.property.VideoRam.pixelSize")
		enum("colorModel", "element.property.videoRamColorModel", VideoRamColorModel.entries, { it.colorModel }, { b, v -> b.colorModel = v })
	}

	// BuzzerViewBeanInfo
	define<BuzzerView> {
		enum("waveformType", BuzzerView.BASE_KEY_WAVEFORM, WaveformType.entries, { it.waveformType }, { b, v -> b.waveformType = v })
	}
}

private fun WebPropertyRegistry.registerMemories() {
	// RAMViewBeanInfo, ROMViewBeanInfo (the contents are edited in the memory contents dialog, see WebMemory)
	define<AbstractAddressableView<*>>({ it is RAMView || it is ROMView }) {
		bitWidth("addressWidth", "element.property.addressBitWidth", { it.addressWidth }, { b, v -> b.addressWidth = v })
		bitWidth("dataWidth", "element.property.dataBitWidth", { it.dataWidth }, { b, v -> b.dataWidth = v })
	}
	define<RAMView> { ram ->
		val connected = ram.model.isConnected
		bool("separateDataPorts", "element.property.RAM.separateDataPorts", { it.separateDataPorts }, { b, v -> b.separateDataPorts = v }, editable = !connected, group = WebGroups.PORTS)
		bool("hasClock", "element.property.RAM.clock", { it.hasClock }, { b, v -> b.hasClock = v }, editable = !connected, group = WebGroups.PORTS)
	}
	define<AbstractAddressableView<*>>({ it is RAMView || it is ROMView }) {
		translatable("text", "graph.property.label", { it.text }, { b, v -> b.text = v })
	}
	define<RAMView> {
		bool("nonVolatile", "graph.property.nonVolatile", { it.nonVolatile }, { b, v -> b.nonVolatile = v }, group = WebGroups.SIMULATION)
	}
	define<ROMView> {
		bool("loadDataSource", "element.property.ROM.loadDataSource", { it.loadDataSource }, { b, v -> b.loadDataSource = v })
		// MemoryStorableIdentificationPropertyJvm: the project's memory items the ROM is loaded from
		choice(
			"memoryStorableId", "element.property.memoryStorable", MemoryStorableIdentification.getAll(),
			{ it.memoryStorableId }, { b, v -> b.memoryStorableId = v },
			id = { it.uuid.id }, label = { RichText.stripToPlainText(it.name.value) }, nullLabel = "None",
		)
	}
	define<AbstractAddressableView<*>>({ it is RAMView || it is ROMView }) { mem ->
		bool("showContents", "element.property.Addressable.showContents", { it.showContents }, { b, v -> b.showContents = v }, group = WebGroups.APPEARANCE)
		if (mem.showContents) {
			int("contentRowsCount", "element.property.Addressable.rowsCount", { it.contentRowsCount }, { b, v -> b.contentRowsCount = v }, min = 1, group = WebGroups.APPEARANCE)
			int("contentColumnsCount", "element.property.Addressable.columnsCount", { it.contentColumnsCount }, { b, v -> b.contentColumnsCount = v }, min = 1, group = WebGroups.APPEARANCE)
		}
	}
	define<ROMView> { rom ->
		if (rom.showContents) {
			script("disassemblerConfig", "element.property.ROM.disassemblerConfig", { it.disassemblerConfig }, { b, v -> b.disassemblerConfig = v })
			bool("showDisassembler", "element.property.ROM.disassemblerDisplay", { it.showDisassembler }, { b, v -> b.showDisassembler = v }, group = WebGroups.APPEARANCE)
			bool(
				"highlightCurrentCellWhenNotSelected", "element.property.ROM.highlightCurrentCellWhenNotSelected",
				{ it.highlightCurrentCellWhenNotSelected }, { b, v -> b.highlightCurrentCellWhenNotSelected = v }, group = WebGroups.APPEARANCE,
			)
		}
	}

	// LookupTableViewBeanInfo
	define<LookupTableView> {
		bitWidth("addressWidth", "element.property.addressBitWidth", { it.addressWidth }, { b, v -> b.addressWidth = v })
		bitWidth("dataWidth", "element.property.dataBitWidth", { it.dataWidth }, { b, v -> b.dataWidth = v })
	}
}

private fun WebPropertyRegistry.registerInOut() {
	// AbstractCircuitInOutViewBeanInfo (circuit input/output ports, digital and analog)
	define<AbstractCircuitInOutView<*>> {
		modelId()
		componentLocation()
		optionalText("name", "edit.property.name", { it.name }, { b, v -> b.name = v })
		orientation()
		customColor()
		enum("portType", PortType.BASE_KEY, PortType.entries, { it.portType }, { b, v -> b.portType = v })
	}

	// DigitalCircuitInOutViewBeanInfo
	define<DigitalCircuitInOutView> { io ->
		bitWidth(get = { it.bitWidth }, set = { b, v -> b.bitWidth = v })
		signalRepresentation({ it.signalRepresentation }, { b, v -> b.signalRepresentation = v })
		if (io.model.portType.isInput) {
			toggle({ it.toggle }, { b, v -> b.toggle = v })
			optionalLong("startValue", "graph.property.input.startValue", { it.startValue }, { b, v -> b.startValue = v }, min = 0)
			interactivePropagationDelay({ it.interactivePropagationDelay }, { b, v -> b.interactivePropagationDelay = v })
		}
		if (io.model.portType == PortType.OUTPUT) {
			bool("customCanBeUndefined", "element.property.CircuitOutput.triState", { it.customCanBeUndefined }, { b, v -> b.customCanBeUndefined = v })
		}
		description({ it.description }, { b, v -> b.description = v })
	}
}

