package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.input.SwitchConfiguration
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.sound.SoundClipFactory
import ch.scorpion.jabbah.base.sound.SoundEffects
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.EmptyVerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A relay with single-pole SPDT ports.
 */
class AnalogRelay(
    inductance: Double = InductorLogic.DEF_INDUCTANCE
) : AbstractAnalogVertice<AnalogRelay>(
    EmptyVerticeCalculator,
    "library.element.AnalogRelay",
    AnalogElementMixin(true, 5)
) {
    companion object {
        private const val DEF_ON_CURRENT = 0.02

        private const val ON_RESISTANCE = 0.05
        private const val OFF_RESISTANCE = 1E8
    }

    private val soundClip by lazy { SoundClipFactory.create("/sound/relay.wav") }

    private val poleCount = 1

    private val inductorLogic = InductorLogic(this, 0)

    private val nSwitch0 = 2

    var switchConfiguration: SwitchConfiguration = SwitchConfiguration.SPDT
        set(value) {
            if (field != value) {
                field = value
                analogElem.postCount = 2 + value.portCount
                updatePorts()
            }
        }

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

    var isOn: Boolean = normallyOn
        private set

    init {
        propagationDelay = Switch.DEF_PROP_DELAY
        updatePorts()
        inductorLogic.setup(inductance, 0.0, InductorLogic.DEF_TRAPEZOIDAL)
    }

    private fun updatePorts() {
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
        analogElem.reset()
        inductorLogic.reset()
        isOn = normallyOn
    }

    override fun stamp(analysis: AnalogCircuitAnalysis) {
        inductorLogic.stamp(analysis)
        // No inductor resistor

        for (i in 0 until switchConfiguration.portCount) {
            analysis.stampNonLinear(analogElem.getNode(nSwitch0 + i))
        }
    }

    override fun startIteration() {
        inductorLogic.startIteration()

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
        //val p = coilCurrent * pmult / onCurrent
        val p = analogElem.getInternalCurrent() * pmult / onCurrent
        var dPos = abs(p * p) - 1.3

        if (dPos < 0) {
            dPos = 0.0
        }
        if (dPos > 1) {
            dPos = 1.0
        }

        return if (dPos < 0.1) {
            //iPos = 0
            false
        } else if (dPos > 0.9) {
            //iPos = 1
            true
        } else {
            //iPos = 2
            false
        }
    }

    override fun doStep(analysis: AnalogCircuitAnalysis, signalHandler: SignalHandler) {
        val requireRecalculation = inductorLogic.doStepRequiresRecalculation(analysis, signalHandler)

        for (p in 0 until  3 * poleCount step 3) {
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
            requestAnalogGraphRecalculation(signalHandler)
        }
    }

    override fun calculateCurrent() {
        setInternalCurrent(0, inductorLogic.calculateCurrent())
    }
}