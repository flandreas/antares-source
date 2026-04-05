package io.antarescircuit.jabbah.base.dsl

import io.antarescircuit.jabbah.base.EmptyHierarchyVisitor

class SyntaxTreePrinter : EmptyHierarchyVisitor() {

	private var indent = 0
	private val builder = StringBuilder()

	val result: String get() = builder.toString().trim().trimEnd('\n')

	override fun visitEnter(node: Any): Boolean {
		if (node is Node) {
			printNode(node)
			indent++
		}
		return true
	}

	override fun visit(node: Any): Boolean {
		if (node is Node) {
			printNode(node)
		}
		return true
	}

	override fun visitLeave(node: Any): Boolean {
		if (node is Node) {
			indent--
		}
		return true
	}

	private fun printNode(node: Node) {
		builder.append("-".repeat(indent))
		builder.append(" $node")
		builder.appendLine()
	}
}