package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.NamableImpl
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.io.*

class UsecaseImpl(
	name: String = "",
	override var executionScript: String = "",
	override var testScript: String? = null,
	private val namable: Namable = NamableImpl(name),
	private val describable: Describable = DescribableImpl()
) : Usecase, Namable by namable, Describable by describable, Bean {

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

	/** ---- [Usecase] interface */

	override fun dispose() {}

	/** ---- [Storable] interface */

	override var storableId: Int = -1

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

	override fun write(writer: StoreWriter) {
		name.write("name", writer)
		description.write("desc", writer)
		writer.writeString("exec", executionScript)
		writer.writeOptionalString("test", testScript)
	}

	override fun read(reader: StoreReader) {
		name.read("name", reader)
		description.read("desc", reader)
		executionScript = reader.readString("exec")
		testScript = reader.readOptionalString("test")
	}
}