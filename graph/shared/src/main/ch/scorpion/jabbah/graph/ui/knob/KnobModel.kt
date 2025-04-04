package ch.scorpion.jabbah.graph.ui.knob

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyOwner
import ch.scorpion.jabbah.base.event.PropertyOwnerImpl
import ch.scorpion.jabbah.base.math.TWO_PI
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

/**
 * A model of a 'knob' that can be turned by a particular angle in order to increase or decrease the
 * [KnobModel]'s [Long] value.
 *
 * [KnobModel] divides the 2*PI angle range into 9 equally sized segments, where the start of segment 1
 * is in the north, and segment indices increase clock-wise.
 *
 * @param initialValue the initial value of this [KnobModel], defaults to zero.
 */
class KnobModel(
    initialValue: Long = 0,
    private val propertyOwner: PropertyOwner<Long> = PropertyOwnerImpl()
) : PropertyOwner<Long> by propertyOwner {

    companion object {
        const val PROP_VALUE = "value"
    }

    init {
        propertyOwner.source = this
    }

    /** Returns the current value of this [KnobModel] as an angle (in radians, zero east, anti-clockwise).*/
    val asAngle: Double get() = asAngle(value)

    /**
     * Contains the current value of ths [KnobModel]. Changing this value results in sending a
     * [PropertyChangeEvent] to all registered [PropertyChangeListener]s.
     */
    var value: Long = initialValue
        set(value) {
            val effectiveNewValue = max(1L, value)
            if (field != effectiveNewValue) {
                val oldValue = field
                field = effectiveNewValue
                propertyOwner.fire(PROP_VALUE, oldValue, field)
            }
        }

    /**
     * The current value with all digits except the most significant digits set to zero.
     * Example: The base value of 12_345 is 10_000.
     */
    private val baseValue: Double get() = 10.0.pow(log10(value.toDouble()).toLong().toDouble())

    fun incrementAngleTo(newAngle: Double): Long {
        var currentBaseValue = baseValue
        val factor = newAngle / TWO_PI
        var newValue = (currentBaseValue + 9 * currentBaseValue * factor).toLong()
        if (newValue != value) {
            if (newValue < value) {
                currentBaseValue *= 10
                newValue = (currentBaseValue + 9 * currentBaseValue * factor).toLong()
            }
            value = newValue
        }
        return value
    }

    private fun decrementAngleTo(newAngle: Double): Long {
        var currentBaseValue = baseValue
        val factor = newAngle / TWO_PI
        var newValue = (currentBaseValue + 9 * currentBaseValue * factor).toLong()
        if (newValue != value) {
            if (newValue > value) {
                currentBaseValue /= 10
                newValue = (currentBaseValue + 9 * currentBaseValue * factor).toLong()
            }
            value = newValue
        }
        return value
    }

    /**
     * @return the new value of this [KnobModel] for convenience
     */
    fun dragToAngle(newAngle: Double, increment: Boolean): Long =
        if (increment) {
            incrementAngleTo(newAngle)
        } else decrementAngleTo(newAngle)

    fun clickToAngle(newAngle: Double) {
        value = (baseValue + (9 * baseValue * (newAngle / 2 / PI))).toLong()
    }

    /** Returns the specified value of this [KnobModel] as an angle (in radians, zero east, anti-clockwise).*/
    private fun asAngle(a: Long): Double {
        val diffValue = a - baseValue
        return if (diffValue == 0.0) 0.0 else TWO_PI * diffValue / (9 * baseValue)
    }
}