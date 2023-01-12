package ch.scorpion.antares.model.module

import ch.scorpion.antares.dsl.AntaresDslModule
import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.addressable.LookupTable
import ch.scorpion.antares.model.addressable.RAM
import ch.scorpion.antares.model.addressable.ROM
import ch.scorpion.antares.model.analog.*
import ch.scorpion.antares.model.analysis.CircuitAnalysisService
import ch.scorpion.antares.model.arithmetic.BitExtender
import ch.scorpion.antares.model.arithmetic.Random
import ch.scorpion.antares.model.expression.BooleanExpressionLibraryItem
import ch.scorpion.antares.model.expression.BooleanExpressionNotation
import ch.scorpion.antares.model.expression.BooleanExpressionService
import ch.scorpion.antares.model.expression.BooleanExpressionStorable
import ch.scorpion.antares.model.gate.*
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.input.*
import ch.scorpion.antares.model.net.*
import ch.scorpion.antares.model.output.*
import ch.scorpion.antares.model.port.SubCircuitPort
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.antares.model.signal.FixedPointConfig
import ch.scorpion.antares.model.truthtable.*
import ch.scorpion.antares.model.vertice.DigitalSubGraphVerticeRefActivationRecord
import ch.scorpion.antares.view.port.DigitalPortFactory
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphFactory
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.param.GraphParamTypeRegistry
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.antares.model] module.
 */
object AntaresModelModule : AbstractModule() {

	val truthTableService = TruthTableService()
	val booleanExpressionService = BooleanExpressionService()
	val circuitAnalysisService = CircuitAnalysisService()

	override fun initialize() {
		customizeProperties(BaseModule.properties)
		configureTypeMap(IOModule.typeMap)

		registerGraphTypes()
		registerGraphParamTypes()

		AntaresDslModule.require()

		GraphModelModule.portFactory = DigitalPortFactory()
		GraphModelModule.graphFactory = object : GraphFactory {
			override fun create(name: TranslatableText, type: GraphType): Graph =
				when (type) {
					AntaresGraphTypes.Digital -> DigitalGraph(name)
					AntaresGraphTypes.Analog -> AnalogGraph(name)
					else -> throw IllegalArgumentException("Unsupported GraphType $type")
				}
		}

		GraphModelModule.subGraphVerticeRefActivationRecordFactory = { verticeRef: SubGraphVerticeRef, signalHandler: SignalHandler ->
			DigitalSubGraphVerticeRefActivationRecord(verticeRef, signalHandler)
		}
	}

	private fun customizeProperties(properties: Properties) {
		properties.set(Switch.PROP_DEFAULT_DELAY, 1_000)
		properties.set(UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR, UndefinedGateInputBehavior.ReadAs0.customName)
		properties.set(TruthTableService.PROP_TRUTH_TABLE_MAX_INPUTS, 8)
		properties.set(TruthTableService.PROP_TRUTH_TABLE_MAX_OUTPUTS, 8)
		properties.set(BooleanExpressionNotation.PROP_NOTATION, BooleanExpressionNotation.ARITHMETIC.customName)
		properties.set(BooleanExpressionNotation.PROP_OMIT_AND, true)
		properties.set(BooleanExpressionNotation.PROP_AND_PARENTHESIS, false)
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("graph", DigitalGraph::class)

		typeMap.register("circuitInOut", CircuitInOutImpl::class)
		typeMap.register("subCircuitPort", SubCircuitPort::class)
		typeMap.register("digitalNet", DigitalNet::class)

		typeMap.register("notGate", NotGate::class)
		typeMap.register("andGate", AndGate::class)
		typeMap.register("nandGate", NandGate::class)
		typeMap.register("orGate", OrGate::class)
		typeMap.register("norGate", NorGate::class)
		typeMap.register("xorGate", XorGate::class)
		typeMap.register("xnorGate", XnorGate::class)
		typeMap.register("bufferGate", BufferGate::class)
		typeMap.register("triStateBufferGate", TriStateBufferGate::class)

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

		// Analog
		typeMap.register("analogGraph", AnalogGraph::class)
		typeMap.register("analogNet", AnalogNet::class)
		typeMap.register("lightBulb", LightBulb::class)
		typeMap.register("battery", Battery::class)
		typeMap.register("resistor", Resistor::class)
		typeMap.register("analogSwitch", AnalogSwitch::class)
		typeMap.register("analogGround", AnalogGround::class)
	}

	private fun registerGraphParamTypes() {
		GraphParamTypeRegistry.register(BitWidthGraphParamType.name) { BitWidthGraphParamType }
	}

	private fun registerGraphTypes() {
		GraphModelModule.graphTypeRegistry.clear()
		GraphModelModule.graphTypeRegistry.register(AntaresGraphTypes.Digital, asDefault = true)
		GraphModelModule.graphTypeRegistry.register(AntaresGraphTypes.Analog)
	}
}