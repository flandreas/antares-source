package io.antarescircuit.antares.model.analog

import io.antarescircuit.antares.model.input.Switch
import io.antarescircuit.antares.model.input.SwitchConfiguration
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementMixin
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.sound.SoundClipFactory
import io.antarescircuit.jabbah.base.sound.SoundEffects
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.vertice.EmptyVerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A relay with either single-pole SPST ports or single-port SPDT ports.

 * Posts SPST:
 * - 0: Switch in
 * - 1: Switch out
 * - 2, 3: Coil
 * - 4: End of coil resistor
 *
 * Posts SPDT:
 * - 0: Switch in
 * - 1, 2: Switch out
 * - 3, 4: Coil
 * - 5: End of coil resistor
 */
class AnalogRelay(
    inductance: Double = InductorLogic.DEF_INDUCTANCE
) : AbstractAnalogVertice<AnalogRelay>(
    EmptyVerticeCalculator,
    "library.element.AnalogRelay",
    AnalogElementMixin(true, 5, internalNodeCount = 1)
) {
    companion object {
        private const val DEF_ON_CURRENT = 0.02
        private const val ON_RESISTANCE = 0.05
        private const val OFF_RESISTANCE = 1E8
        private const val COIL_RESISTANCE = 20.0
    }

    private val soundClip by lazy { SoundClipFactory.create("/sound/relay.wav") }

    private val inductorLogic = InductorLogic()

    private val nSwitch0 = 0

    private var nCoil1 = 0
    private var nCoil2 = 0
    private var nCoil3 = 0

    private var coilCurrent = 0.0

    private val voltDiff: Double get() = analogElem.getNodeVoltage(nCoil1) - analogElem.getNodeVoltage(nCoil3)

    var switchConfiguration: SwitchConfiguration = SwitchConfiguration.SPDT
        set(value) {
            if (field != value) {
                field = value
                analogElem.postCount = 2 + value.portCount
                updatePorts()
            }
        }

    val coilPortIdBase: Int get() = switchConfiguration.portCount + 1

    /** The inductance of this [AnalogRelay] in Henry.*/
    var inductance: Double
        get() = inductorLogic.inductance
        set(value) {
            if (inductorLogic.inductance != value) {
                inductorLogic.setup(value, 0.0, InductorLogic.DEF_TRAPEZOIDAL)
            }
        }

    /** The current (in A) through the inductor at which the [AnalogRelay] is switched on. */
    var onCurrent: Double = DEF_ON_CURRENT
        set(value) {
            require(value > 0) { Translations.getString("element.property.relay.onCurrentNotLargerThanZero.msg") }
            field = value
        }

    /** If set, the switch is in state 'on' if there is no current flowing through the inductor. */
    var normallyOn: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                isOn = normallyOn
                stateChanged()
            }
        }

    /**
     * This flag is set if the switch is "on", meaning that the circuit between ports 1 and 2 is closed.
     */
    var isOn: Boolean = normallyOn
        private set

    init {
        propagationDelay = Switch.DEF_PROP_DELAY
        updatePorts()
        inductorLogic.setup(inductance, coilCurrent, InductorLogic.DEF_TRAPEZOIDAL)
    }

    private fun setupPoles() {
        nCoil1 = switchConfiguration.portCount
        nCoil2 = nCoil1 + 1
        nCoil3 = nCoil1 + 2
    }

    private fun updatePorts() {
        setupPoles()
        clearPorts()
        (1..2 + switchConfiguration.portCount).forEach { _ -> addPort(AnalogPort()) }
    }

    /** ---- [AbstractAnalogVertice] */

    override fun executionInitialize(signalHandler: SignalHandler) {
        isOn = normallyOn
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        isOn = normallyOn
    }

    /** ---- [Storable] interface */

    override fun read(reader: StoreReader) {
        super.read(reader)
        inductance = reader.readString("inductance").toDouble()
        onCurrent = reader.readString("onCurrent").toDouble()
        if (reader.hasAttribute("switchConfig")) {
            switchConfiguration = SwitchConfiguration.withName(reader.readString("switchConfig"))
        }
        if (reader.hasAttribute("normallyOn")) {
            normallyOn = reader.readBoolean("normallyOn")
        }
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("inductance", inductance.toString())
        writer.writeString("onCurrent", onCurrent.toString())
        writer.writeString("switchConfig", switchConfiguration.customName)
        if (normallyOn) {
            writer.writeBoolean("normallyOn", normallyOn)
        }
    }

    /** ---- [AnalogElement] */

    override fun reset() {
        super.reset()
        inductorLogic.reset()
        analogElem.reset()
        coilCurrent = 0.0
        isOn = normallyOn
    }

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        // Inductor from coil post 1 to internal node
        inductorLogic.stamp(analysis, analogElem.getNode(nCoil1), analogElem.getNode(nCoil3))

        // Resistor from internal node to coil post 2
        analysis.stampResistor(analogElem.getNode(nCoil3), analogElem.getNode(nCoil2), COIL_RESISTANCE)

        for (i in 0 until switchConfiguration.portCount) {
            analysis.stampNonLinear(analogElem.getNode(nSwitch0 + i))
        }
    }

    override fun startIteration() {
        inductorLogic.startIteration(voltDiff)

        val newIsOn = shouldBeOn()

        if (newIsOn != isOn) {
            isOn = newIsOn
            stateChanged()
            if (SoundEffects.ENABLED) {
                soundClip.play()
            }
        }
    }

    private fun shouldBeOn(): Boolean {
        val attracted = shouldBeAttracted()
        return if (normallyOn) !attracted  else attracted
    }

    private fun shouldBeAttracted(): Boolean {
        // This would be able to calculate intermediate switch positions, but is
        // currently not used
        val magic = 1.3
        val pmult = sqrt(magic + 1)
        val p = coilCurrent * pmult / onCurrent
        var dPos = abs(p * p) - 1.3

        if (dPos < 0) {
            dPos = 0.0
        }
        if (dPos > 1) {
            dPos = 1.0
        }

        return if (dPos < 0.1) {
            false
        } else if (dPos > 0.9) {
            true
        } else {
            false
        }
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        val requireRecalculation = inductorLogic.doStepRequiresRecalculation(voltDiff, analysis)

        for (p in 0 until  3 step 3) {
            analysis.stampResistor(
                analogElem.getNode(nSwitch0 + p),
                analogElem.getNode(nSwitch0 + 1 + p),
                if (isOn) ON_RESISTANCE else OFF_RESISTANCE
            )
            if (switchConfiguration.portCount > 2) {
                analysis.stampResistor(
                    analogElem.getNode(nSwitch0 + p),
                    analogElem.getNode(nSwitch0 + 2 + p),
                    if (!isOn) ON_RESISTANCE else OFF_RESISTANCE
                )
            }
        }

        if (requireRecalculation || isOn != shouldBeOn()) {
            requestAnalogReanalization(signalHandler)
        }
    }

    override fun calculateCurrent() {
        coilCurrent = inductorLogic.calculateCurrent(voltDiff)
    }
}