package ch.scorpion.antares.model.testcase.parser

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation.*
import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.antares.model.testcase.Value.Type.NORMAL
import ch.scorpion.antares.model.testcase.parser.TestcaseTokenType.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.BaseTokenType.*
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.parser.AbstractParser
import ch.scorpion.jabbah.base.parser.Token

/**
 * Parses [Testcase.testVectors] and creates the corresponding AST.
 *
 * <pre>
 *     testScript : portNames { statement }
 *     portNames : portName portName { portName } EOL
 *     portName : ID | inputPortName | outputPortName
 *     inputPortName : ">" ID
 *     outputPortName : "<" ID
 *     statement : testVector | block
 *     block : "run" "{“ { testVector } "}"
 *     testVector : value value { value } EOL
 *     value :  number | dontCare | undefined | clockedNumber
 *     number : decimalValue | hexValue | binaryValue
 *     decimalValue : { decimalDigit }
 *     hexValue : '0x' { hexDigit }
 *     hexDigit : '0'..'9', 'A'..'F' | undefined
 *     binaryValue : '0b' { binaryDigit }
 *     binaryDigit : '0' | '1' | undefined
 *     dontCare : 'X' | 'x'
 *     undefined : 'Z' | 'z'
 *     clockedNumber : "^" number
 *     EOL : "\n"
 * </pre>
 */
class TestcaseParser(
	lexer: TestcaseLexer,
	private val analyser: TestcaseAnalyser? = null
) : AbstractParser(lexer) {

	constructor(text: String, analyser: TestcaseAnalyser? = null): this(TestcaseLexer(text), analyser)

	private val onLine: Boolean get() = currentToken!!.type != EOF && currentToken!!.type != EOL

	override fun parse(): Node = TestScript(lexer.location, portNames(), statementList()).also {
		analyser?.analyse(it)
	}

	private fun portNames(): PortNames {
		val list = mutableListOf<PortName>()
		list.add(portName())
		list.add(portName())
		while(currentToken!!.type != EOL && currentToken!!.type != EOF) {
			list.add(portName())
		}
		return PortNames(lexer.location, list)
	}

	private fun portName(): PortName =
		when (currentToken!!.type) {
			GREATER -> {
				eat(GREATER)
				val name = currentToken as Token<String>
				eat(ID)
				PortName(lexer.location, name, PortNameType.INPUT)
			}
			SMALLER -> {
				eat(SMALLER)
				val name = currentToken as Token<String>
				eat(ID)
				PortName(lexer.location, name, PortNameType.OUTPUT)
			}
			else -> {
				val name = currentToken as Token<String>
				eat(ID)
				PortName(lexer.location, name, PortNameType.DEFAULT)
			}
    }

	private fun statementList(): List<Node> {
		val list = mutableListOf<Node>()
		while (currentToken!!.type != EOF) {
			if (currentToken!!.type == EOL) {
				eat(EOL)
			} else {
				list.add(statement())
			}
		}
		return list
	}

	private fun statement(): Node {
		return when (currentToken!!.type) {
			RUN -> run()
			else -> testVector()
		}
	}

	private fun run(): Node {
		lexer.location.let { location ->
			val list = mutableListOf<TestVectorNode>()
			eat(RUN)
			eat(LCURLEY)
			list.addAll(testVectors())
			eat(RCURLEY)
			return RunNode(location, list)
		}
	}

	private fun testVectors(): List<TestVectorNode> {
		val list = mutableListOf<TestVectorNode>()
		while (currentToken!!.type != RCURLEY) {
			if (currentToken!!.type == EOL) {
				eat(EOL)
			} else {
				list.add(testVector())
			}
		}
		return list
	}

	private fun testVector(): TestVectorNode {
		lexer.location.let {  location ->
			val list = mutableListOf<ValueNode>()
			while (onLine) {
				list.add(value())
			}
			if (currentToken!!.type == EOL) {
				eat(EOL)
			}
			return TestVectorNode(location, list)
		}
	}

	private fun value(): ValueNode {
		return when (currentToken!!.type) {
			DECIMAL_LITERAL -> decimalLiteral(NORMAL)
			BINARY_LITERAL -> binaryLiteral(NORMAL)
			HEX_LITERAL -> hexLiteral(NORMAL)
			DONT_CARE -> dontCare()
			UNDEFINED -> undefined()
			CARET -> clockedNumber()
			else -> throw SyntaxError(lexer.location, Translations.getString("base.dsl.unexpectedToken.msg", currentToken!!.type.id))
		}
	}

	private fun decimalLiteral(type: Value.Type): ValueNode {
		lexer.location.let { loc ->
			val value = Value((currentToken!!.value as DigitalSignal), type, DECIMAL)
			eat(DECIMAL_LITERAL)
			return ValueNode(loc, value)
		}
	}

	private fun binaryLiteral(type: Value.Type): ValueNode {
		lexer.location.let { loc ->
			val value = Value((currentToken!!.value as DigitalSignal), type, BINARY)
			eat(BINARY_LITERAL)
			return ValueNode(loc, value)
		}
	}

	private fun hexLiteral(type: Value.Type): ValueNode {
		lexer.location.let { loc ->
			val value = Value((currentToken!!.value as DigitalSignal), type, HEXADECIMAL)
			eat(HEX_LITERAL)
			return ValueNode(loc, value)
		}
	}

	private fun numericValue(type: Value.Type): ValueNode {
		return when (currentToken!!.type) {
			DECIMAL_LITERAL -> decimalLiteral(type)
			BINARY_LITERAL -> binaryLiteral(type)
			HEX_LITERAL -> hexLiteral(type)
			else -> throw SyntaxError(lexer.location, Translations.getString("base.dsl.unexpectedToken.msg", currentToken!!.type.id))
		}
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
		return numericValue(Value.Type.CLOCKED)
	}
}