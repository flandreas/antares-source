package ch.scorpion.antares.model.signal

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes

/**
 * A [Bit] is the smallest unit of information in ch.scorpion.antares.
 * In contrast to the classical binary nature of a bit, an Antares [Bit] can have an undefined state.
 * [Bit] is designed to be immutable.
 */
enum class Bit(private val value: Int?) {
	Undefined(null),
	Error(null),
	False(0),
	True(1);

	companion object {
		const val UNDEFINED_CHAR = '?'
		const val ERROR_CHAR = 'X'

		fun of(value: Int): Bit {
			return when (value) {
				0 -> False
				1 -> True
				else -> throw IllegalArgumentException("value must be 0 or 1")
			}
		}

		fun of(value: Boolean): Bit {
			return of(if (value) 1 else 0)
		}

		/**
		 * Converts a [Int] to the first [length] [Bit]s of its binary representation, starting with
		 * the least-priority bit.
		 */
		fun listFromInt(value: Int, length: Int): List<Bit> = (0 until length).map { Bit.of(BitOperation.getBitAt(value.toLong(), it)) }

		fun listFromLong(value: Long, length: Int): List<Bit> = (0 until length).map { Bit.of(BitOperation.getBitAt(value, it)) }
	}

	/** Checks whether this [Bit] has a defined value, i.e. whether it is not `null`.*/
	val isDefined: Boolean get() = value != null

	/** Checks whether this [Bit] is set, i.e. whether it has the value `1`.*/
	val isSet: Boolean get() = value == 1

	/**
	 * Returns the numerical value of this [Bit], i.e. either `0` or `1`.
	 * This property can only be accessed for defined [Bit]s.
	 * @throws KotlinNullPointerException if this [Bit] is undefined
	 */
	val numericalValue: Int get() = value!!

	val color: CompositeColor
		get() {
			return when (this) {
				Undefined -> Themes.get<AntaresTheme>().undefined
				Error -> Themes.get<AntaresTheme>().error
				False -> Themes.get<AntaresTheme>().zero
				True -> Themes.get<AntaresTheme>().one
			}
		}

	/** Returns the inverse of this [Bit].*/
	fun not(): Bit {
		return when (this) {
			Undefined -> Undefined
			Error -> Error
			False -> True
			True -> False
		}
	}

	fun and(bit: Bit): Bit = Bit.of(this.isSet && bit.isSet)

	/** Returns the inverse of this [Bit], if requested by the parameter.*/
	fun invert(invert: Boolean = true): Bit {
		return if (invert) not() else this
	}

	fun toHexString(): String {
		return toString()
	}

	fun toBinaryString(): String {
		return toString()
	}

	/** ---- [Any] */

	override fun toString(): String {
		return when (this) {
			Undefined -> UNDEFINED_CHAR.toString()
			Error -> ERROR_CHAR.toString()
			False -> "0"
			True -> "1"
		}
	}
}