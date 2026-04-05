package io.antarescircuit.jabbah.base.io

import java.io.ByteArrayOutputStream

class StringCodePrinter : CodePrinter(ByteArrayOutputStream()) {

	override fun toString(): String {
		close()
		return out.toString()
	}
}