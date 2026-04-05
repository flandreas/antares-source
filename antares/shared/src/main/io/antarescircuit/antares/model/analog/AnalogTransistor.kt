package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.model.net.TransistorIF
import io.antarescircuit.antares.model.net.TransistorIF.Companion.DEFAULT_TRANSISTOR_TYPE
import io.antarescircuit.antares.model.net.TransistorType
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.abs

/**
 * [AnalogTransistor] acts during simulation as a "Voltage Controlled Current Source (VCC)".
 * See https://ultimateelectronicsbook.com/dependent-sources/.
 */
class AnalogTransistor(
	transistorType: TransistorType = DEFAULT_TRANSISTOR_TYPE,
	var gain: Double = DEF_GAIN
) : AbstractAnalogVertice<AnalogTransistor>(
	EmptyVerticeCalculator,
	"library.element.AnalogTransistor",
	AnalogElementMixin(true, postCount = 3)
), TransistorIF<AnalogSignal> {

	companion object {
		private const val DEF_GAIN = 0.1
		private const val DEF_THRESHOLD = 1.5
		private const val BETA = 0.02
	}

	override val type: String get() = super<TransistorIF>.type

	override val typeDesc: String? get() = super<TransistorIF>.typeDesc

	override var transistorType: TransistorType = transistorType
		set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	override val isOn: Boolean get() = false

	init {
		addPort(AnalogPort(name = "S"))
		addPort(AnalogPort(name = "G"))
		addPort(AnalogPort(name = "D"))
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super<AbstractAnalogVertice>.write(writer)
		super<TransistorIF>.write(writer)
		writer.writeDouble("gain", gain)
	}

	override fun read(reader: StoreReader) {
		super<AbstractAnalogVertice>.read(reader)
		super<TransistorIF>.read(reader)
		gain = reader.readDouble("gain")
	}

	/** ---- [AnalogElement] */

	private var gm: Double = 0.0
	private var gds: Double = 0.0
	private var lastV0: Double = 0.0
	private var lastV2: Double = 0.0
	private var ids: Double = 0.0
	private var vt: Double = DEF_THRESHOLD
	private var mode = 0

	val conductance: Double get() = abs(-gds - gm)

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampNonLinear(analogElem.nodes[0])
		analysis.stampNonLinear(analogElem.nodes[2])
	}

	override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
		val pnp = if (transistorType == TransistorType.N) 1 else -1

		val vs = Array(3) { i -> analogElem.voltages[i] }
		if (vs[0] > lastV0 + 0.5) {
			vs[0] = lastV0 + 0.5
		}
		if (vs[0] < lastV0 - 0.5) {
			vs[0] = lastV0 - 0.5
		}
		if (vs[2] > lastV2 + 0.5) {
			vs[2] = lastV2 + 0.5
		}
		if (vs[2] < lastV2 - 0.5) {
			vs[2] = lastV2 - 0.5
		}

		val gate = 1
		var source = 0
		var drain = 2
		if (pnp * vs[0] > pnp * vs[2]) {
			source = 2
			drain = 0
		}

		var vgs = vs[gate] - vs[source]
		var vds = vs[drain] - vs[source]
		if (abs(lastV0 - vs[0]) > 0.01 || abs(lastV2 - vs[2]) > 0.01) {
			analysis.converged = false
		}
		lastV0 = vs[0]
		lastV2 = vs[2]

		val realVgs = vgs
		val realVds = vds

		vgs *= pnp
		vds *= pnp

		ids = 0.0
		gm = 0.0

		gds = 0.0

		if (vgs < vt) {
			// Should be all zero, but that causes a singular matrix. Instead, treat it as a large resistor.
			gds = 1e-8
			ids = vds * gds
			mode = 0
		} else if (vds < vgs - vt) {
			// Linear
			ids = BETA * ((vgs - vt) * vds - vds * vds * 0.5)
			gm = BETA * vds
			gds = BETA * (vgs - vds - vt)
			mode = 1
		} else {
			// Saturation. gds = 0
			gm = BETA * (vgs - vt)
			// Use very small gds to avoid non-convergence
			gds = 1e-8
			ids = 0.5 * BETA * (vgs - vt) * (vgs - vt) + (vds - (vgs - vt)) * gds
			mode = 2
		}
		val rs = -pnp * ids + gds * realVds + gm * realVgs

		analysis.stampMatrix(analogElem.nodes[drain], analogElem.nodes[drain], gds)
		analysis.stampMatrix(analogElem.nodes[drain], analogElem.nodes[source], -gds - gm)
		analysis.stampMatrix(analogElem.nodes[drain], analogElem.nodes[gate], gm)

		analysis.stampMatrix(analogElem.nodes[source], analogElem.nodes[drain], -gds)
		analysis.stampMatrix(analogElem.nodes[source], analogElem.nodes[source], gds + gm)
		analysis.stampMatrix(analogElem.nodes[source], analogElem.nodes[gate], -gm)

		analysis.stampRightSide(analogElem.nodes[drain], rs)
		analysis.stampRightSide(analogElem.nodes[source], -rs)

		if (source == 0 && pnp == 1 || source == 2 && pnp == -1) {
			ids = -ids
		}
	}
}