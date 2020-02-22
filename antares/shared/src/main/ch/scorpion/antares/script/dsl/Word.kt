package ch.scorpion.antares.script.dsl

/** A DSL wrapper for [ch.scorpion.antares.model.signal.Word].*/
class Word(val word: ch.scorpion.antares.model.signal.Word) {

	fun bitAt(index: Int): Boolean = word.bitAt(index).isSet

	fun toInt(): Int = word.toInt()!!

	fun toHexString(): String = word.toHexString()

	fun not(): Word = Word(word.not() as ch.scorpion.antares.model.signal.Word)

	fun and(other: Word): Word = Word(this.word.and(other.word) as ch.scorpion.antares.model.signal.Word)
}