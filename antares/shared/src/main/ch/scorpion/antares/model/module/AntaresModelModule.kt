package ch.scorpion.antares.model.module

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.arithmetic.Random
import ch.scorpion.antares.model.gate.*
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.input.Clock
import ch.scorpion.antares.model.input.DipSwitch
import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.memory.RAM
import ch.scorpion.antares.model.memory.ROM
import ch.scorpion.antares.model.net.*
import ch.scorpion.antares.model.output.LED
import ch.scorpion.antares.model.output.LEDMatrix
import ch.scorpion.antares.model.output.SevenSegmentDisplay
import ch.scorpion.antares.model.port.SubCircuitPort
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.antares.model] module.
 */
object AntaresModelModule : AbstractModule() {

    override fun initialize() {
        configureTypeMap(IOModule.typeMap)
        GraphModelModule.graphFactory = { DigitalGraph() }
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

    }
}