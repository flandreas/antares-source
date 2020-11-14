package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.io.*

class UsecaseImpl(
	name: String = "",
	override var executionScript: String = "",
	override var testScript: String? = null
) : Usecase, Namable, Describable, Bean {

	var executionScriptProperty: ScriptProperty
		get() = ScriptProperty(executionScript)
		set(value) {
			executionScript = value.script!!
		}

	var testScriptProperty: ScriptProperty
		get() = ScriptProperty(testScript)
		set(value) {
			testScript = value.script!!
		}

	/** ---- [Any] */

	override fun toString(): String = StringUtils.replaceNegation(name.value)

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(name))

	override var description: Description by observableDescription(Description(""))

	/** ---- [Usecase] interface */

	override var id: Int = 0

	override fun dispose() {}

	/** ---- [Storable] interface */

	override var storableId: Int = -1

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		name.write("name", writer)
		description.write("desc", writer)
		writer.writeString("exec", executionScript)
		writer.writeOptionalString("test", testScript)
	}

	override fun read(reader: StoreReader) {
		// Attribute 'id' was introduced after version 0.1
		if (reader.hasAttribute("id")) {
			id = reader.readInt("id")
		}
		name = Name.read("name", reader)
		description = Description.read("desc", reader)
		executionScript = reader.readString("exec")
		testScript = reader.readOptionalString("test")
	}
}