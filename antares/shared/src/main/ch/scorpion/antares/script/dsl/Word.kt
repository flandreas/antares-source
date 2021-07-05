package ch.scorpion.antares.script.dsl

import ch.scorpion.antares.model.signal.BitWidth

/** A DSL wrapper for [ch.scorpion.antares.model.signal.Word].*/
class Word(val word: ch.scorpion.antares.model.signal.DigitalSignal) {

	fun bitAt(index: Int): Boolean = word.bitAt(index).isSet

	fun toInt(): Int = word.toInt()!!

	fun toHexString(): String = word.hexString

	fun not(): Word = Word(word.not() as ch.scorpion.antares.model.signal.Word)

	fun and(other: Word): Word = Word(this.word.and(other.word) as ch.scorpion.antares.model.signal.Word)

	fun shiftLeft(bitCount: Int = 1): Word = Word(word.shiftLeft(bitCount))

	fun shiftRight(bitCount: Int = 1): Word = Word(word.shiftRight(bitCount))

	fun subword(index: Int, width: Int): Word = Word(word.getSubword(BitWidth.of(width), index))
}