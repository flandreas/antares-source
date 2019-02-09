package ch.scorpion.jabbah.animation

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.NoSuchElementException
import kotlin.math.abs
import kotlin.math.sign

/**
 * A [DoubleRange] is a [Sequence] of [Double] values between a begin and end value.
 */
class DoubleRange(var begin: Double, var end: Double, sequenceType: SequenceType) : Sequence<Double> {

    constructor(begin: Double, end: Double): this(begin, end, SequenceType.ONCE)

    /** Holds the value to be returned next.*/
    private var value: Double = begin

    private val sequencer: Sequencer = when(sequenceType) {
        SequenceType.ONCE -> Sequencer.ONCE
        SequenceType.OSCILLATION -> Sequencer.OSCILLATION
        else -> throw IllegalArgumentException("unsupported SequenceType $sequenceType")
    }

    override fun toString(): String {
        return "DoubleRange[$begin,$end]"
    }

    /** ---- [Sequence] interface*/

    override val size: Double
        get() = abs(begin - end)

    override fun hasNext(): Boolean = sequencer.hasNext(begin, end, value)

    override fun getNext(distance: Double): Double {
        if (!hasNext()) {
            throw NoSuchElementException("$distance")
        }
        val result = value

        val nextValue = sequencer.calculateNext(begin, end, value, distance)
        value = nextValue.nextValue
        begin = nextValue.begin
        end = nextValue.end

        return result
    }

    override fun getCurrent(): Double {
        return value
    }

    /** ---- [DoubleRange] */

    private data class NextValue(val begin: Double, val end: Double, val nextValue: Double)

    private enum class Sequencer {

        ONCE() {
            override fun hasNext(begin: Double, end: Double, value: Double): Boolean {
                if (sign(end - begin) > 0) {
                    return value <= end
                }
                return value >= end
            }

            override fun calculateNext(begin: Double, end: Double, value: Double, distance: Double): NextValue {
                return NextValue(
                    begin = begin,
                    end = end,
                    nextValue = value + sign(end - begin) * distance
                )
            }
        },

        OSCILLATION() {
            override fun hasNext(begin: Double, end: Double, value: Double): Boolean = true

            override fun calculateNext(begin: Double, end: Double, value: Double, distance: Double): NextValue {
                val signum = sign(end - begin)
                val nextValue = value + signum * distance
                if (signum > 0) {
                    if (nextValue <= end) {
                        return NextValue(begin, end,nextValue)
                    }
                    return NextValue(end, begin, end - distance)
                } else {
                    if (nextValue >= end) {
                        return NextValue(begin, end, nextValue)
                    }
                    return NextValue(end, begin, end + distance)
                }
            }
        };

        abstract fun hasNext(begin: Double, end: Double, value: Double): Boolean

        abstract fun calculateNext(begin: Double, end: Double, value: Double, distance: Double): NextValue
    }
}