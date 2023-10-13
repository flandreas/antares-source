package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.io.*

/**
 * A [Testcase] contains test vectors with combinations of input signals and
 * the expected output signals. [Testcase]s are contained in a [DigitalGraph].
 */
class Testcase(
	name: String = "",
	script: String = ""
) : AbstractStorable(), Namable, Describable, Bean {

	/** The identification of this [Testcase] that is unique within a [DigitalGraph]. */
	var id: Int = 0

	/** Contains the executable test vectors. */
	var testVectors: ScriptProperty = ScriptProperty(script)

	override fun toString(): String = name.value

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(name))

	override var description: Description by observableDescription(Description(""))

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		name.write("name", writer)
		description.write("desc", writer)
		writer.writeOptionalString("testVectors", testVectors.script)
	}

	override fun read(reader: StoreReader) {
		id = reader.readInt("id")
		name = Name.read("name", reader)
		description = Description.read("desc", reader)
		testVectors = ScriptProperty(reader.readOptionalString("testVectors"))
	}
}