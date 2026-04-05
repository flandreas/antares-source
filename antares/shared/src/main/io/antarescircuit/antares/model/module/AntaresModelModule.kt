package io.antarescircuit.antares.model.module

import io.antarescircuit.antares.dsl.AntaresDslModule
import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.addressable.*
import io.antarescircuit.antares.model.analog.*
import io.antarescircuit.antares.model.analysis.CircuitAnalysisService
import io.antarescircuit.antares.model.arithmetic.BitExtender
import io.antarescircuit.antares.model.arithmetic.Random
import io.antarescircuit.antares.model.expression.BooleanExpressionLibraryItem
import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.expression.BooleanExpressionService
import io.antarescircuit.antares.model.expression.BooleanExpressionStorable
import io.antarescircuit.antares.model.fsm.*
import io.antarescircuit.antares.model.gate.*
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.*
import io.antarescircuit.antares.model.gate.UnaryLogicGateType.Buffer
import io.antarescircuit.antares.model.gate.UnaryLogicGateType.Not
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.input.*
import io.antarescircuit.antares.model.net.*
import io.antarescircuit.antares.model.output.*
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.SubCircuitPort
import io.antarescircuit.antares.model.signal.BitWidthGraphParamType
import io.antarescircuit.antares.model.signal.DigitalSignalColor
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.model.signal.FixedPointConfig
import io.antarescircuit.antares.model.testcase.CombinedTestcaseRunner
import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.antares.model.testcase.TestcaseAppService
import io.antarescircuit.antares.model.testcase.Testcases
import io.antarescircuit.antares.model.truthtable.*
import io.antarescircuit.antares.model.vertice.AntaresSubGraphVerticeRefActivationRecord
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.output.LightColorGraphParamType
import io.antarescircuit.antares.view.port.AntaresPortFactory
import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphFactory
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamTypeRegistry
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseRecorder
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.TypeMap

/**
 * Module definitions for the [io.antarescircuit.antares.model] module.
 */
object AntaresModelModule : AbstractModule() {

	val truthTableService = TruthTableService()
	val booleanExpressionService = BooleanExpressionService()
	val circuitAnalysisService = CircuitAnalysisService()
	val testcaseAppService = TestcaseAppService()
	val fsmEditorService: FSMEditorService = FSMEditorServiceImpl()
	val fsmTransitionService: FSMTransitionService = FSMTransitionServiceImpl()

	override fun initialize() {
		customizeProperties(BaseModule.properties)
		configureTypeMap(IOModule.typeMap)

		registerGraphTypes()
		registerGraphParamTypes()

		AntaresDslModule.require()

		GraphModelModule.portFactory = AntaresPortFactory()
		GraphModelModule.graphFactory = object : GraphFactory {
			override fun create(name: TranslatableText, type: GraphType): Graph =
				when (type) {
					AntaresGraphTypes.Digital -> DigitalGraph(name)
					AntaresGraphTypes.Analog -> AnalogGraph(name)
					else -> throw IllegalArgumentException("Unsupported GraphType $type")
				}
		}

		GraphModelModule.subGraphVerticeRefActivationRecordFactory = { verticeRef: SubGraphVerticeRef, signalHandler: SignalHandler ->
			AntaresSubGraphVerticeRefActivationRecord(verticeRef, signalHandler)
		}
	}

	override fun resetDependencies() {
		AntaresDslModule.reset()
	}

	private fun customizeProperties(properties: Properties) {
		properties.set(CurrentDefaultPropagationDelay.PROP_DEFAULT_PROPAGATION_DELAY, 20)
		properties.set(CurrentSwitchPropagationDelay.PROP_DEFAULT_DELAY, 1_000)
		properties.set(UsecaseRecorder.PROP_DEF_DELAY_MS, properties.getInt(CurrentSwitchPropagationDelay.PROP_DEFAULT_DELAY) / 1_000)
		properties.set(DigitalSignalColor.PROP_DIFFERENT_NON_ZERO_MULTI_BIT_COLOR, true)

		properties.set(UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR, UndefinedGateInputBehavior.ReadAs0.customName)
		properties.set(TruthTableService.PROP_TRUTH_TABLE_MAX_INPUTS, 8)
		properties.set(TruthTableService.PROP_TRUTH_TABLE_MAX_OUTPUTS, 8)
		properties.set(BooleanExpressionNotation.PROP_NOTATION, BooleanExpressionNotation.ARITHMETIC.customName)
		properties.set(BooleanExpressionNotation.PROP_OMIT_AND, true)
		properties.set(BooleanExpressionNotation.PROP_AND_PARENTHESIS, false)

		properties.set(AnalogCircuitAnalysis.PROP_TIME_STEP, AnalogCircuitAnalysis.DEF_TIME_STEP)
		properties.set(CombinedTestcaseRunner.PROP_CHECK_PROP_DELAY_CONSISTENCY, true)
		properties.set(DigitalPort.PROP_ADJUST_BIT_WIDTH, true)

		properties.set(DigitalSignalRepresentation.PROP_DEFAULT_SIGNAL_REPRESENTATION, DigitalSignalRepresentation.HEXADECIMAL.customName)
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("graph", DigitalGraph::class)
		typeMap.register("testcase", Testcase::class)
		typeMap.register("testcases", Testcases::class)

		typeMap.register("circuitInOut", DigitalCircuitInOutImpl::class)
		typeMap.register("subCircuitPort", SubCircuitPort::class)
		typeMap.register("digitalNet", DigitalNet::class)

		typeMap.register("triStateBufferGate", TriStateBufferGate::class)

		typeMap.register("notGate", { it is UnaryLogicGate && it.gateType == Not }) { UnaryLogicGate.notGate() }
		typeMap.register("bufferGate", { it is UnaryLogicGate && it.gateType == Buffer }) { UnaryLogicGate.bufferGate() }

		typeMap.register("andGate", { it is NonUnaryLogicGate && it.gateType == And }) { NonUnaryLogicGate.andGate() }
		typeMap.register("nandGate", { it is NonUnaryLogicGate && it.gateType == Nand }) { NonUnaryLogicGate.nandGate() }
		typeMap.register("orGate", { it is NonUnaryLogicGate && it.gateType == Or }) { NonUnaryLogicGate.orGate() }
		typeMap.register("norGate", { it is NonUnaryLogicGate && it.gateType == Nor }) { NonUnaryLogicGate.norGate() }
		typeMap.register("xorGate", { it is NonUnaryLogicGate && it.gateType == Xor }) { NonUnaryLogicGate.xorGate() }
		typeMap.register("xnorGate", { it is NonUnaryLogicGate && it.gateType == Xnor }) { NonUnaryLogicGate.xnorGate() }

		typeMap.register("switch", Switch::class)
		typeMap.register("dipSwitch", DipSwitch::class)
		typeMap.register("clock", Clock::class)
		typeMap.register("led", LED::class)
		typeMap.register("rgbLed", RgbLED::class)
		typeMap.register("sevenSegmentDisplay", SevenSegmentDisplay::class)
		typeMap.register("sixteenSegmentDisplay", SixteenSegmentDisplay::class)
		typeMap.register("bidirectionalSplitter", BidirectionalSplitter::class)
		typeMap.register("splitter", Splitter::class)
		typeMap.register("concentrator", Concentrator::class)
		typeMap.register("constant", Constant::class)
		typeMap.register("probe", Probe::class)
		typeMap.register("ram", RAM::class)
		typeMap.register("rom", ROM::class)
		typeMap.register("delay", DelayGate::class)
		typeMap.register("tunnel", Tunnel::class)
		typeMap.register("ledMatrix", LEDMatrix::class)
		typeMap.register("random", Random::class)
		typeMap.register("keyboard", Keyboard::class)
		typeMap.register("terminal", Terminal::class)
		typeMap.register("break", Break::class)
		typeMap.register("pullResistor", PullResistor::class)
		typeMap.register("transistor", Transistor::class)
		typeMap.register("ground", Ground::class)
		typeMap.register("power", Power::class)
		typeMap.register("joystick", Joystick::class)
		typeMap.register("realSwitch", RealSwitch::class)
		typeMap.register("bitExtender", BitExtender::class)
		typeMap.register("buzzer", Buzzer::class)
		typeMap.register("doubleThrowSwitch", DoubleThrowSwitch::class)
		typeMap.register("lookupTable", LookupTable::class)
		typeMap.register("wireTap", WireTap::class)
		typeMap.register("wireTapConfig", WireTapConfig::class)
		typeMap.register("fixedPointConfig", FixedPointConfig::class)
		typeMap.register("powerOnReset", PowerOnReset::class)

		typeMap.register("truthTable", TruthTable::class)
		typeMap.register("truthTableInputColumn", TruthTableInputColumn::class)
		typeMap.register("truthTableOutputColumn", TruthTableOutputColumn::class)
		typeMap.register("truthTableLibraryItem", TruthTableLibraryItem::class)
		typeMap.register("expression", BooleanExpressionStorable::class)
		typeMap.register("expressionLibraryItem", BooleanExpressionLibraryItem::class)
		typeMap.register("memory", MemoryStorable::class)
		typeMap.register("memoryLibraryItem", MemoryLibraryItem::class)

		typeMap.register("fsmDrawing", FSMDrawing::class)
		typeMap.register("fsmLibraryItem", FSMLibraryItem::class)
		typeMap.register("fsmState", FSMState::class)
		typeMap.register("fsmTransition", FSMTransition::class)

		// Analog
		typeMap.register("analogGraph", AnalogGraph::class)
		typeMap.register("analogNet", AnalogNet::class)
		typeMap.register("lightBulb", LightBulb::class)
		typeMap.register("battery", Battery::class)
		typeMap.register("currentSource", CurrentSource::class)
		typeMap.register("resistor", Resistor::class)
		typeMap.register("capacitor", Capacitor::class)
		typeMap.register("analogSwitch", AnalogSwitch::class)
		typeMap.register("analogGround", AnalogGround::class)
		typeMap.register("analogTransistor", AnalogTransistor::class)
		typeMap.register("analogInOut", AnalogCircuitInOut::class)
		typeMap.register("analogPower", AnalogPower::class)
		typeMap.register("analogOscilloscopeProbe", AnalogOscilloscopeProbeVertice::class)
		typeMap.register("inductor", Inductor::class)
		typeMap.register("analogDTS", AnalogDoubleThrowSwitch::class)
		typeMap.register("analogRelay", AnalogRelay::class)
		typeMap.register("diode", Diode::class)
		typeMap.register("analogLED", AnalogLED::class)
	}

	private fun registerGraphParamTypes() {
		GraphParamTypeRegistry.register(BitWidthGraphParamType.name) { BitWidthGraphParamType }
		GraphParamTypeRegistry.register(LightColorGraphParamType.name) { LightColorGraphParamType }
	}

	private fun registerGraphTypes() {
		GraphModelModule.defaultGraphType = AntaresGraphTypes.Digital
		GraphModelModule.graphTypeRegistry.clear()
		GraphModelModule.graphTypeRegistry.register(AntaresGraphTypes.Digital, asDefault = true)
		GraphModelModule.graphTypeRegistry.register(AntaresGraphTypes.Analog)
	}
}