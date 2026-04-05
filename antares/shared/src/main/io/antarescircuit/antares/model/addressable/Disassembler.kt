package io.antarescircuit.antares.model.addressable

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.logger
import kotlin.text.Regex

/**
 * A simple disassembler that uses regular expression to disassemble hexadecimal operations into a
 * description, typically an assembler-like notation consisting of an operation code and an argument.
 *
 * After construction, a [Disassembler] can be configured using the various [operation] and [operations]
 * methods, before it can be asked to [disassemble] a hexadecimal string.
 */
class Disassembler {

    companion object {
        private val LOG by logger(Disassembler::class)
    }

    private val operations = mutableListOf<Operation>()

    fun reset() {
        operations.clear()
    }

    /** Adds an operation consisting of a regex pattern and a replacement result.*/
    fun operation(pattern: String, result: String): Disassembler {
        operations.add(Operation(pattern.trim().toRegex(), result.trim()))
        return this
    }

    /** Adds an operation in the form "pattern=result".*/
    fun operation(expression: String): Disassembler {
        val terms = expression.split("=")
        return operation(terms[0], terms[1])
    }

    /** Adds a [List] of operations in the form "pattern=result".*/
    fun operations(expressions: List<String>): Disassembler {
        expressions.forEach {
            val expression = it.trim()
            if (StringUtils.isNotEmpty(expression)) {
                operation(expression)
            }
        }
        return this
    }

    /** Adds operations from a newline-separated [String] of operations in the form "pattern=result".*/
    fun operations(expressions: String): Disassembler {
        return operations(expressions.split("\n"))
    }

    /** Disassembles an hexadecimal string using the first matching registered operation.*/
    fun disassemble(op: String): String {
        return findOperation(op)?.let {
            it.regex.replaceFirst(op, it.result)
        } ?: "Error"
    }

    private fun findOperation(op: String): Operation? = operations.firstOrNull { it.regex.matches(op) }

    private data class Operation(val regex: Regex, val result: String)
}