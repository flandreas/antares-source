package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.jabbah.base.dsl.Compound
import ch.scorpion.jabbah.base.dsl.DslTokenType.*
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.parser.AbstractParser
import ch.scorpion.jabbah.base.parser.Token

/**
 * Parses [Testcase.testVectors] and creates the corresponding AST.
 *
 * <pre>
 *     testScript : portNames { testVector }
 *     portNames : portName portName { portName } EOL
 *     portName: CHAR
 *     testVector : value value { value } EOL
 *     value : decimalValue | hexValue | binaryValue
 *     decimalValue : { decimalDigit }
 *     hexValue : '0x' { hexDigit }
 *     binaryValue : '0b' { binaryDigit }
 *     EOL : "\n"
 * </pre>
 */
// TODO Support hexValue and binaryValue
class TestcaseParser(
	lexer: TestcaseLexer
) : AbstractParser(lexer) {

	constructor(text: String): this(TestcaseLexer(text))

	override fun parse(): Node = TestScript(lexer.location, portNames(), testVectors())

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
			list.add(testVector())
			if (currentToken!!.type == EOL) {
				eat(EOL)
			}
		}
		return Compound(lexer.location, list)
	}

	private fun testVector(): TestVectorNode {
		val list = mutableListOf<ValueNode>()
		while (currentToken!!.type == LITERAL) {
			list.add(ValueNode(lexer.location, (currentToken!!.value as Long).toULong()))
			eat(LITERAL)
		}
		return TestVectorNode(lexer.location, list)
	}
}