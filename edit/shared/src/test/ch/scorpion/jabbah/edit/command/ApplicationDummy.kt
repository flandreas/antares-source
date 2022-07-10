package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.Undoable
import ch.scorpion.jabbah.edit.UndoableDataHolder
import ch.scorpion.jabbah.io.*

class ApplicationDummy(
	data: StorableString? = StorableString()
) : UndoableDataHolder, Bean {

	var data: StorableString? = data
		set(value) {
			if (value?.value == "throwException") {
				throw IllegalArgumentException("requested exception")
			}
			field = value
		}

	val mandatoryData: StorableString get() = data!!

	override fun getUndoableState(): Storable? {
		return data
	}

	override fun setUndoableState(state: Storable) {
		data = state as StorableString
	}
}

class StorableString(value: String = "") : AbstractStorable() {

	var value: String = value
		private set

	fun append(s: String) {
		value = "$value$s"
	}

	fun dropLast(charCount: Int) {
		value = value.dropLast(charCount)
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeString("value", value)
	}

	override fun read(reader: StoreReader) {
		value = reader.readString("value")
	}
}

class AppendCommand(
	private val app: ApplicationDummy,
	val s: String
) : AbstractCommand("anyDescription") {

	override fun execute() {
		app.mandatoryData.append(s)
	}
}

class ExceptionCommand : AbstractCommand("anyDescription") {
	override fun execute() {
		throw RuntimeException("Error")
	}
}

class UndoableExceptionCommand : AbstractCommand("anyDescription"), Undoable {
	override fun execute() {
		throw RuntimeException("Error in execute")
	}

	override fun undo() {
		throw RuntimeException("Error in undo")
	}
}

class UndoableAppendCommand(
	private val app: ApplicationDummy,
	private val s: String
) : AbstractCommand("anyDescription"), Undoable {

	override fun execute() {
		app.mandatoryData.append(s)
	}

	override fun undo() {
		app.mandatoryData.dropLast(s.length)
	}
}