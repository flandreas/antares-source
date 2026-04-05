package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.testcase.parser.TestcaseAnalyser
import io.antarescircuit.antares.model.testcase.parser.TestcaseParser
import io.antarescircuit.jabbah.base.dsl.SemanticAnalyser
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.edit.model.text.description.*
import io.antarescircuit.jabbah.io.*

/**
 * A [Testcase] contains test vectors with combinations of input signals and
 * the expected output signals. [Testcase]s are contained in a [DigitalGraph].
 */
class Testcase(
	name: String = "",
	script: String = ""
) : AbstractStorable(), Namable, Describable, Bean {

	companion object {
		const val DEF_NUMBER_OF_ITERATIONS = 1_000
		val SCRIPT_HELP_ID = HelpId("antares.testcase.script")
	}

	/** The identification of this [Testcase] that is unique within a [DigitalGraph]. */
	var id: Int = 0

	/** Contains the executable test vectors. */
	var testVectors: ScriptProperty = ScriptProperty(script)

	var ignored: Boolean = false

	var skipPropDelayConsistenceCheck: Boolean = false

	var numberOfIterations: Int = DEF_NUMBER_OF_ITERATIONS

	/**
	 * Non-persistent reference to the owing [DigitalGraph]. Used only for creating a
	 * [SemanticAnalyser] when editing [testVectors].
	 */
	var graph: DigitalGraph? = null

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
		if (ignored) {
			writer.writeBoolean("ignored", ignored)
		}
		if (skipPropDelayConsistenceCheck) {
			writer.writeBoolean("skipPropDelayConsistenceCheck", skipPropDelayConsistenceCheck)
		}
		if (numberOfIterations != DEF_NUMBER_OF_ITERATIONS) {
			writer.writeInt("numberOfIterations", numberOfIterations)
		}
	}

	override fun read(reader: StoreReader) {
		id = reader.readInt("id")
		name = Name.read("name", reader)
		description = Description.read("desc", reader)
		testVectors = ScriptProperty(reader.readOptionalString("testVectors"))
		if (reader.hasAttribute("ignored")) {
			ignored = reader.readBoolean("ignored")
		}
		if (reader.hasAttribute("skipPropDelayConsistenceCheck")) {
			skipPropDelayConsistenceCheck = reader.readBoolean("skipPropDelayConsistenceCheck")
		}
		if (reader.hasAttribute("numberOfIterations")) {
			numberOfIterations = reader.readInt("numberOfIterations")
		}
	}

	/** ---- [Testcase] */

	fun createParser(program: String, semanticAnalyser: SemanticAnalyser?): TestcaseParser =
		TestcaseParser(program, graph?.let { TestcaseAnalyser(it) })

	fun duplicate(newName: String): Testcase {
		val duplicate = StorableCloner.clone(this)
		duplicate.name = Name(newName)
		return duplicate
	}
}