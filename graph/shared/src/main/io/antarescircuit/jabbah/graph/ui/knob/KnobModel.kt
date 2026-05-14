package io.antarescircuit.jabbah.graph.ui.knob

import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.event.PropertyOwner
import io.antarescircuit.jabbah.base.event.PropertyOwnerImpl
import io.antarescircuit.jabbah.base.math.TWO_PI
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.pow

/**
 * A model of a 'knob' that can be turned by a particular angle in order to increase or decrease the
 * [KnobModel]'s [Long] value.
 *
 * [KnobModel] divides the 2*PI angle range into 9 equally sized segments, where the start of segment 1
 * is in the north, and segment indices increase clock-wise.
 *
 * [KnobModel] normalizes its [value] to three significant digits. Besides rounding, this also includes
 * changing [Magnitude], i.e. "4825 V" gets normalized to "4.82 KV", and "0.0015 F" gets normalized to "1.5 mF".
 * Normalization respects [SIUnit.minimumMagnitude].
 *
 * @param initialValue the initial value of this [KnobModel]
 */
class KnobModel(
    initialValue: MagnitudeValue,
    private val propertyOwner: PropertyOwner<MagnitudeValue> = PropertyOwnerImpl()
) : PropertyOwner<MagnitudeValue> by propertyOwner {

    companion object {
        const val PROP_VALUE = "value"
    }

    init {
        propertyOwner.source = this
    }

    /** Returns the current value of this [KnobModel] as an angle (in radians, zero north, clockwise).*/
    val asAngle: Double get() = asAngle(value.baseValue)

    /**
     * Contains the current value of ths [KnobModel]. Changing this value results in sending a
     * [PropertyChangeEvent] to all registered [PropertyChangeListener]s.
     */
    private var _value: MagnitudeValue = initialValue
        set(value) {
            if (field != value) {
                val oldValue = _value
                field = value
                propertyOwner.fire(PROP_VALUE, oldValue, value)
            }
        }

    var value: MagnitudeValue
        get() = _value
        set(value) {
            if (_value != value) {
                try {
                    _value = value.normalize()
                } catch (_: IllegalArgumentException) {
                    // keep the old value
                }
            }
        }

    private fun setValueNormalized(value: MagnitudeValue) {
        if (_value != value) {
            try {
                _value = value.normalize()
            } catch (_: IllegalArgumentException) {
                // keep the old value
            }
        }
    }

    /**
     * @param newAngle origin north, clockwise (like the KnobView scale)
     */
    fun incrementAngleTo(newAngle: Double, changeMagnitude: Boolean, snap: Boolean = false): MagnitudeValue {
        var currentBaseValue = value.northBaseValue
        val factor = newAngle / TWO_PI
        var newValue = (currentBaseValue + 9 * currentBaseValue * factor)
        if (newValue != value.baseValue) {
            if (changeMagnitude) {
                currentBaseValue *= 10
                newValue = currentBaseValue + 9 * currentBaseValue * factor
            }
            value = value.withBaseValue(newValue, snap)
        }
        return value
    }

    /**
     * @param newAngle origin north, clockwise (like the KnobView scale)
     */
    private fun decrementAngleTo(newAngle: Double, changeMagnitude: Boolean, snap: Boolean = false): MagnitudeValue {
        var currentBaseValue = value.northBaseValue
        val factor = newAngle / TWO_PI
        var newValue = (currentBaseValue + 9 * currentBaseValue * factor)
        if (newValue != value.baseValue) {
            if (changeMagnitude) {
                currentBaseValue /= 10
                newValue = currentBaseValue + 9 * currentBaseValue * factor
            }
            try {
                value = value.withBaseValue(newValue, snap)
            } catch (_: IllegalArgumentException) {
                // Keep old value
            }
        }
        return value
    }

    /**
     * @return the new value of this [KnobModel] for convenience
     */
    fun dragToAngle(newAngle: Double, increment: Boolean, changeMagnitude: Boolean, snap: Boolean): MagnitudeValue =
        if (increment) {
            incrementAngleTo(newAngle, changeMagnitude, snap)
        } else {
            decrementAngleTo(newAngle, changeMagnitude, snap)
        }

    /**
     * Sets [value] by clicking on a scale point determined by [newAngle].
     *
     * Update the order of magnitude if the new angle is reached by implicitly turning
     * the knob across the 0 position; if crossed clockwise, magnitude is increased,
     * and if crossed counter-clockwise, magnitude is decreased.
     *
     * @param newAngle origin north, clockwise (like the KnobView scale)
     */
    fun clickToAngle(newAngle: Double, snap: Boolean) {
        val oldAngle = asAngle

        try {
            if (oldAngle < PI && newAngle > PI && TWO_PI - newAngle + oldAngle < PI) {
                // Decrement base value
                val newBaseValue = value.northBaseValue / 10
                setValueNormalized(value.withBaseValue(newBaseValue + (9 * newBaseValue * (newAngle / TWO_PI)), snap))
            } else if (newAngle < PI && oldAngle > PI && TWO_PI - oldAngle + newAngle < PI) {
                // Increment base value
                val newBaseValue = value.northBaseValue * 10
                setValueNormalized(value.withBaseValue(newBaseValue + (9 * newBaseValue * (newAngle / TWO_PI)), snap))
            } else {
                // Keep base value
                setValueNormalized(value.withBaseValue(value.northBaseValue + (9 * value.northBaseValue * (newAngle / TWO_PI)), snap))
            }
        } catch (_: IllegalArgumentException) {
            // Keep old value
        }
    }

    /** Returns the specified value as an angle (in radians, zero east, anti-clockwise).*/
    private fun asAngle(a: Double): Double {
        val diffValue = a - value.northBaseValue
        return if (diffValue == 0.0) 0.0 else TWO_PI * diffValue / (9 * value.northBaseValue)
    }
}