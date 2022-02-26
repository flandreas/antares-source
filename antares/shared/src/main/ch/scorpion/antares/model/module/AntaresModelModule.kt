package ch.scorpion.antares.model.module

import ch.scorpion.antares.dsl.AntaresDslModule
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.addressable.RAM
import ch.scorpion.antares.model.addressable.ROM
import ch.scorpion.antares.model.arithmetic.BitExtender
import ch.scorpion.antares.model.arithmetic.Random
import ch.scorpion.antares.model.gate.*
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.input.*
import ch.scorpion.antares.model.net.*
import ch.scorpion.antares.model.output.*
import ch.scorpion.antares.model.port.SubCircuitPort
import ch.scorpion.antares.model.signal.BitWidthGraphParamType
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableInputColumn
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.antares.model.truthtable.TruthTableOutputColumn
import ch.scorpion.antares.model.vertice.DigitalSubGraphVerticeRefActivationRecord
import ch.scorpion.antares.view.port.DigitalPortFactory
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.param.GraphParamTypeRegistry
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecord
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRefActivationRecordFactory
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.antares.model] module.
 */
object AntaresModelModule : AbstractModule() {

	override fun initialize() {
		customizeProperties(BaseModule.properties)
		configureTypeMap(IOModule.typeMap)
		registerGraphParamTypes()

		AntaresDslModule.require()

		GraphModelModule.portFactory = DigitalPortFactory()
		GraphModelModule.graphFactory = { DigitalGraph(name = it) }

		// Proguard doesn't seem to be happy with SAML lambdas
		GraphModelModule.subGraphVerticeRefActivationRecordFactory = object : SubGraphVerticeRefActivationRecordFactory {
			override fun create(verticeRef: SubGraphVerticeRef, signalHandler: SignalHandler): SubGraphVerticeRefActivationRecord =
				DigitalSubGraphVerticeRefActivationRecord(verticeRef, signalHandler)
		}
	}

	private fun customizeProperties(properties: Properties) {
		properties.set(Switch.PROP_DEFAULT_DELAY, 1_000)
		properties.set(UndefinedGateInputBehavior.PROP_UNDEFINED_GATE_INPUT_BEHAVIOR, UndefinedGateInputBehavior.ReadAs0.customName)
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

		typeMap.register("truthTable", TruthTable::class)
		typeMap.register("truthTableInputColumn", TruthTableInputColumn::class)
		typeMap.register("truthTableOutputColumn", TruthTableOutputColumn::class)
		typeMap.register("truthTableLibraryItem", TruthTableLibraryItem::class)
	}

	private fun registerGraphParamTypes() {
		GraphParamTypeRegistry.register(BitWidthGraphParamType.name) { BitWidthGraphParamType }
	}
}