package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.antares.model.testcase.parser.TestcaseTokenType.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.BaseTokenType.*
import ch.scorpion.jabbah.base.dsl.Compound
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
 *     value :  number | dontCare | undefined | clockedNumber
 *     number : decimalValue | hexValue | binaryValue
 *     decimalValue : { decimalDigit }
 *     hexValue : '0x' { hexDigit }
 *     binaryValue : '0b' { binaryDigit }
 *     dontCare : 'X' | 'x'
 *     undefined : 'Z' | 'z'
 *     clockedNumber : "^" number
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
				list.add(value())
			}
			return TestVectorNode(loc, list)
		}
	}

	private fun value(): ValueNode {
		return when (currentToken!!.type) {
			LITERAL -> number(Value.Type.NORMAL)
			DONT_CARE -> dontCare()
			UNDEFINED -> undefined()
			CARET -> clockedNumber()
			else -> throw SyntaxError(lexer.location, Translations.getString("base.dsl.unexpectedToken.msg", currentToken!!.type.id))
		}
	}

	private fun number(type: Value.Type): ValueNode {
		val value = Value((currentToken!!.value as Long).toULong(), type)
		eat(LITERAL)
		return ValueNode(lexer.location, value)
	}

	private fun dontCare(): ValueNode {
		eat(DONT_CARE)
		return ValueNode(lexer.location, Value.X)
	}

	private fun undefined(): ValueNode {
		eat(UNDEFINED)
		return ValueNode(lexer.location, Value.Z)
	}

	private fun clockedNumber(): ValueNode {
		eat(CARET)
		return number(Value.Type.CLOCKED)
	}
}