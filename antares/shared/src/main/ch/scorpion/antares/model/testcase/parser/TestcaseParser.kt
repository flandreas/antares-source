package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Compound
import ch.scorpion.jabbah.base.dsl.DslTokenType.*
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.parser.AbstractParser
import ch.scorpion.jabbah.base.parser.Token

/**
 * Parses [Testcase.testVectors] and creates the corresponding AST.
 *
 * <pre>
 *     testScript : portNames { testVector }
 *     portNames : portName portName { portName } EOL
 *     portName: ID
 *     testVector : value value { value } EOL
 *     value : decimalValue | hexValue | binaryValue | dontCare
 *     decimalValue : { decimalDigit }
 *     hexValue : '0x' { hexDigit }
 *     binaryValue : '0b' { binaryDigit }
 *     dontCare : 'X'
 *     EOL : "\n"
 * </pre>
 */
// TODO Support hexValue and binaryValue
class TestcaseParser(
	lexer: TestcaseLexer,
	private val analyser: TestcaseAnalyser? = null
) : AbstractParser(lexer) {

	constructor(text: String, analyser: TestcaseAnalyser? = null): this(TestcaseLexer(text), analyser)

	private val onLine: Boolean get() = currentToken!!.type != EOF && currentToken!!.type != EOL

	override fun parse(): Node = TestScript(lexer.location, portNames(), testVectors()).also {
		analyser?.analyse(it)
	}

	private fun portNames(): PortNames {
		val list = mutableListOf<Token<String>>()
		list.add(portName())
		list.add(portName())
		while (currentToken!!.type == ID) {
			list.add(portName())
		}
		when (currentToken!!.type) {
			EOL -> eat(EOL)
			EOF -> eat(EOF)
		}
		return PortNames(lexer.location, list)
	}

	private fun portName(): Token<String> {
		val name = currentToken as Token<String>
		eat(ID)
		return name
	}

	private fun testVectors(): Compound<TestVectorNode> {
		val list = mutableListOf<TestVectorNode>()
		while (currentToken!!.type != EOF) {
			if (currentToken!!.type == EOL) {
				eat(EOL)
			} else {
				list.add(testVector())
			}
		}
		return Compound(lexer.location, list)
	}

	private fun testVector(): TestVectorNode {
		lexer.location.let {  loc ->
			val list = mutableListOf<ValueNode>()
			while (onLine) {
				when (currentToken!!.type) {
					LITERAL -> {
						list.add(ValueNode(lexer.location, Value((currentToken!!.value as Long).toULong())))
						eat(LITERAL)
					}
					ID -> {
						when ((currentToken!!.value as String).uppercase()) {
							"X" -> list.add(ValueNode(lexer.location, Value.X))
							"Z" -> list.add(ValueNode(lexer.location, Value.Z))
							else -> throw SyntaxError(lexer.location,Translations.getString("base.dsl.invalidCharacter.msg", currentToken!!.value!!))
						}
						eat(ID)
					}
					else -> throw SyntaxError(lexer.location, Translations.getString("base.dsl.unexpectedToken.msg", currentToken!!.type.id))
				}
			}
			return TestVectorNode(loc, list)
		}
	}
}