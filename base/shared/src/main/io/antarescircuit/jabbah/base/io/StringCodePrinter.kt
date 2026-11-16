package io.antarescircuit.jabbah.base.io

/** A [CodePrinter] collecting the printed code in memory, returned by [toString]. */
class StringCodePrinter : CodePrinter(StringBuilder()) {

	override fun toString(): String = out.toString()
}
