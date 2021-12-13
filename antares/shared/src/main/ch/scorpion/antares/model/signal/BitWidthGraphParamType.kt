package ch.scorpion.antares.model.signal

import ch.scorpion.antares.dsl.AntaresInterpreter
import ch.scorpion.antares.dsl.AntaresLexer
import ch.scorpion.antares.dsl.AntaresParser
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.CodeLocation
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.graph.GraphActivationRecord
import ch.scorpion.jabbah.graph.model.param.GraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.reflect.KClass

/**
 * Defines [BitWidth] as [GraphParamType].
 */
object BitWidthGraphParamType : GraphParamType<BitWidth> {

	override val name: String = "bitWidth"

	override val displayableName: String by lazy { Translations.getString("${BitWidth.BASE_KEY}.name") }

	override val valueClass: KClass<BitWidth> get() = BitWidth::class

	override fun toString(): String = displayableName

	override fun createValue(name: String, value: BitWidth): GraphParamValue<BitWidth> =
		GraphParamValue.create(name, this, value)

	override fun writeValue(name: String, value: BitWidth, writer: StoreWriter) {
		value.write(name, writer)
	}

	override fun readValue(name: String, reader: StoreReader): BitWidth =
		BitWidth.read(name, reader)

	override fun toDslValue(value: BitWidth): Any = value.width.toLong()

	override fun evaluateIn(graph: Graph, value: BitWidth): BitWidth {
		return if (value is BitWidthExpression) {
			val parser = AntaresParser(
				AntaresLexer(value.expression),
				BaseModule.semanticAnalyserFactory.create(graph.symbolTable))
			val newValue = AntaresInterpreter(
				parser,
				Memory(GraphActivationRecord(graph))
			).interpret()

			if (newValue is Long) {
				if (value.width.toLong() != newValue) {
					BitWidthExpression(value.expression, BitWidth.of(newValue.toInt()))
				} else {
					value
				}
			} else {
				throw DslError(CodeLocation.UNDEFINED, "Expecting Long as value of BitWidth")
			}
		} else {
			value
		}
	}
}