package ch.scorpion.jabbah.edit.command

import ch.scorpion.jabbah.edit.Command

internal class TransactionStackWriter(title: String) {
    private val out = StringBuilder()

    init {
        out.appendLine(title)
    }

    fun write(transaction: Transaction) {
        writeTransaction(transaction, out)
    }

    override fun toString(): String = out.toString()

    private fun writeTransaction(transaction: Transaction, out: StringBuilder) {
        out.appendLine("# Transaction")
        transaction.commands.forEach { command ->
            writeCommand(command, out)
        }
    }

    private fun writeCommand(command: Command, out: StringBuilder) {
        out.append("-- ")
        out.appendLine(command.getDetailedDescription())
    }
}