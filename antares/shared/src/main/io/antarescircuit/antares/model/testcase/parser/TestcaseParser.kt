package io.antarescircuit.antares.model.testcase.parser

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation.*
import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.antares.model.testcase.Value
import io.antarescircuit.antares.model.testcase.Value.Type.NORMAL
import io.antarescircuit.antares.model.testcase.parser.TestcaseTokenType.*
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.BaseTokenType.*
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.SyntaxError
import io.antarescircuit.jabbah.base.parser.AbstractParser
import io.antarescircuit.jabbah.base.parser.Token

/**
 * Parses [Testcase.testVectors] and creates the corresponding AST.
 *
 * <pre>
 *     testScript : portNames { statement }
 *     portNames : portName portName { portName } EOL
 *     portName : ID | inputPortName | outputPortName
 *     inputPortName : ">" ID
 *     outputPortName : "<" ID
 *     statement : testVector | block | loop
 *     block : "run" "{“ { testVector | loop } "}"
 *     loop : "loop" ID number ".." number "{" { statement } "}"
 *     testVector : value value { value } EOL
 *     value :  number | dontCare | undefined | clockedValue | expression
 *     number : decimalValue | hexValue | binaryValue
 *     decimalValue : { decimalDigit }
 *     hexValue : '0x' { hexDigit }
 *     hexDigit : '0'..'9', 'A'..'F' | undefined
 *     binaryValue : '0b' { binaryDigit }
 *     binaryDigit : '0' | '1' | undefined
 *     dontCare : 'X' | 'x'
 *     undefined : 'Z' | 'z'
 *     clockedValue : "^" ( number | expression )
 *     expression : "(" or ")"
 *     or : xor { "|" xor }
 *     xor : and { "^" and }
 *     and : shift { "&" shift }
 *     shift : sum { ( "<<" | ">>" ) sum }
 *     sum : product { ( "+" | "-" ) product }
 *     product : unary { "*" unary }
 *     unary : ( "-" | "~" ) unary | primary
 *     primary : number | ID | "(" or ")"
 *     EOL : "\n"
 * </pre>
 *
 * Loops are nestable, and a "loop" inside a "run" block may contain test vectors and loops only.
 * The ID of a loop is a variable that can be used in the expressions of the loop's test vectors.
 * Inside expressions, "^" denotes bitwise XOR. This is unambiguous because a clock marker "^"
 * can only precede a whole value, i.e. it never occurs inside parentheses.
 * Since a standalone "X" or "Z" denotes a don't care or undefined value, these are not valid loop variable names.
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
				val name = currentToken!!.cast<String>()
				eat(ID)
				PortName(lexer.location, name, PortNameType.INPUT)
			}
			SMALLER -> {
				eat(SMALLER)
				val name = currentToken!!.cast<String>()
				eat(ID)
				PortName(lexer.location, name, PortNameType.OUTPUT)
			}
			else -> {
				val name = currentToken!!.cast<String>()
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
				list.add(statement(allowRun = true))
			}
		}
		return list
	}

	private fun statement(allowRun: Boolean): Node {
		return when (currentToken!!.type) {
			RUN -> if (allowRun) run() else throw unexpectedToken()
			LOOP -> loop(allowRun)
			else -> testVector()
		}
	}

	private fun run(): Node {
		lexer.location.let { location ->
			eat(RUN)
			return RunNode(location, block(allowRun = false))
		}
	}

	private fun loop(allowRun: Boolean): Node {
		lexer.location.let { location ->
			eat(LOOP)
			val variable = currentToken!!.value as? String
			eat(ID)
			val from = loopBound()
			eat(RANGE)
			val to = loopBound()
			return LoopNode(location, variable!!, from, to, block(allowRun))
		}
	}

	private fun loopBound(): ULong =
		when (currentToken!!.type) {
			DECIMAL_LITERAL, BINARY_LITERAL, HEX_LITERAL -> definedNumber().also { eat(currentToken!!.type) }
			else -> throw unexpectedToken()
		}

	/** Parses "{" { statement } "}", where nested "run" blocks are only allowed if [allowRun] is `true`.*/
	private fun block(allowRun: Boolean): List<Node> {
		eat(LCURLEY)
		val list = mutableListOf<Node>()
		while (currentToken!!.type != RCURLEY && currentToken!!.type != EOF) {
			if (currentToken!!.type == EOL) {
				eat(EOL)
			} else {
				list.add(statement(allowRun))
			}
		}
		eat(RCURLEY)
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
			CARET -> clockedValue()
			LPAREN -> expressionValue(NORMAL)
			else -> throw unexpectedToken()
		}
	}

	private fun unexpectedToken(): SyntaxError =
		SyntaxError(lexer.location, Translations.getString("base.dsl.unexpectedToken.msg", currentToken!!.type.id))

	private fun decimalLiteral(type: Value.Type): ValueNode {
		lexer.location.let { loc ->
			val value = Value((currentToken!!.value as DigitalSignal), type, DECIMAL)
			eat(DECIMAL_LITERAL)
			return LiteralValueNode(loc, value)
		}
	}

	private fun binaryLiteral(type: Value.Type): ValueNode {
		lexer.location.let { loc ->
			val value = Value((currentToken!!.value as DigitalSignal), type, BINARY)
			eat(BINARY_LITERAL)
			return LiteralValueNode(loc, value)
		}
	}

	private fun hexLiteral(type: Value.Type): ValueNode {
		lexer.location.let { loc ->
			val value = Value((currentToken!!.value as DigitalSignal), type, HEXADECIMAL)
			eat(HEX_LITERAL)
			return LiteralValueNode(loc, value)
		}
	}

	private fun numericValue(type: Value.Type): ValueNode {
		return when (currentToken!!.type) {
			DECIMAL_LITERAL -> decimalLiteral(type)
			BINARY_LITERAL -> binaryLiteral(type)
			HEX_LITERAL -> hexLiteral(type)
			LPAREN -> expressionValue(type)
			else -> throw unexpectedToken()
		}
	}

	private fun dontCare(): ValueNode {
		eat(DONT_CARE)
		return LiteralValueNode(lexer.location, Value.X)
	}

	private fun undefined(): ValueNode {
		eat(UNDEFINED)
		return LiteralValueNode(lexer.location, Value.Z)
	}

	private fun clockedValue(): ValueNode {
		eat(CARET)
		return numericValue(Value.Type.CLOCKED)
	}

	private fun expressionValue(type: Value.Type): ValueNode {
		lexer.location.let { loc ->
			eat(LPAREN)
			val expression = or()
			eat(RPAREN)
			return ExpressionValueNode(loc, expression, type)
		}
	}

	/** Parses a left-associative sequence of binary [operators] with operands parsed by [operand].*/
	private fun binary(operators: Set<TestcaseTokenType>, operand: () -> Expression): Expression {
		var result = operand()
		while (currentToken!!.type in operators) {
			val operator = currentToken!!.type as TestcaseTokenType
			eat(operator)
			result = BinaryExpression(result, operator, operand())
		}
		return result
	}

	private fun or(): Expression = binary(setOf(VERTICAL_BAR)) { xor() }

	private fun xor(): Expression = binary(setOf(CARET)) { and() }

	private fun and(): Expression = binary(setOf(AMPERSAND)) { shift() }

	private fun shift(): Expression = binary(setOf(SHIFT_LEFT, SHIFT_RIGHT)) { sum() }

	private fun sum(): Expression = binary(setOf(PLUS, MINUS)) { product() }

	private fun product(): Expression = binary(setOf(MULTIPLY)) { unary() }

	private fun unary(): Expression =
		when (val type = currentToken!!.type) {
			MINUS, TILDE -> {
				eat(type)
				UnaryExpression(type as TestcaseTokenType, unary())
			}
			else -> primary()
		}

	private fun primary(): Expression =
		when (currentToken!!.type) {
			DECIMAL_LITERAL, BINARY_LITERAL, HEX_LITERAL -> NumberExpression(definedNumber()).also { eat(currentToken!!.type) }
			ID -> VariableExpression(lexer.location, currentToken!!.value as String).also { eat(ID) }
			LPAREN -> {
				eat(LPAREN)
				or().also { eat(RPAREN) }
			}
			else -> throw unexpectedToken()
		}

	/** Returns the value of the current number literal, which must not contain undefined bits.*/
	private fun definedNumber(): ULong =
		(currentToken!!.value as DigitalSignal).toLong()
			?: throw SyntaxError(lexer.location, Translations.getString("antares.testcase.error.undefinedNumber"))
}
