package ch.scorpion.antares.model.module

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.arithmetic.Random
import ch.scorpion.antares.model.gate.*
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.input.*
import ch.scorpion.antares.model.addressable.RAM
import ch.scorpion.antares.model.addressable.ROM
import ch.scorpion.antares.model.net.*
import ch.scorpion.antares.model.output.LED
import ch.scorpion.antares.model.output.LEDMatrix
import ch.scorpion.antares.model.output.RgbLED
import ch.scorpion.antares.model.output.SevenSegmentDisplay
import ch.scorpion.antares.model.port.SubCircuitPort
import ch.scorpion.antares.view.port.DigitalPortFactory
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.antares.model] module.
 */
object AntaresModelModule : AbstractModule() {

	override fun initialize() {
		customizeProperties(BaseModule.properties)
		configureTypeMap(IOModule.typeMap)
		GraphModelModule.portFactory = DigitalPortFactory()
		GraphModelModule.graphFactory = { DigitalGraph(name = it) }
	}

	private fun customizeProperties(properties: Properties) {
		properties.set(Switch.PROP_DEFAULT_DELAY, 1_000)
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
	}
}