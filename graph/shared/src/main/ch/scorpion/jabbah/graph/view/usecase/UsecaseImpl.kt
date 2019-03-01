package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.NamableImpl
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.io.*

class UsecaseImpl(
	name: String = "",
	override var executionScript: String = "",
	private val namable: Namable = NamableImpl(name),
	private val describable: Describable = DescribableImpl()
) : Usecase, Namable by namable, Describable by describable {

	var executionScriptProperty: TextProperty
		get() = TextProperty(executionScript)
		set(value) {
			executionScript = value.text!!
		}

	/** ---- [Any] */

	override fun toString(): String = StringUtils.replaceNegation(name.value)

	/** ---- [Usecase] interface */

	override var id: Int = 0

	override fun dispose() {}

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		name.write("name", writer)
		description.write("desc", writer)
		writer.writeString("exec", executionScript)
	}

	override fun read(reader: StoreReader) {
		id = reader.readInt("id")
		name.read("name", reader)
		description.read("desc", reader)
		executionScript = reader.readString("exec")
	}

}