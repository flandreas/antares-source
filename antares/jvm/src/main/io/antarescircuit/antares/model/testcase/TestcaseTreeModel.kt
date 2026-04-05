package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.edit.model.text.NamableTreeNode
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import kotlin.collections.iterator

internal class TestcaseTreeModel(
    graph: DigitalGraph?,
    font: Font?
) : DefaultTreeModel(graph?.let { NamableTreeNode(graph, font!!) } ?: DefaultMutableTreeNode()) {

    private val graphNode: DefaultMutableTreeNode get() = root as DefaultMutableTreeNode

    init {
        graph?.testcases?.testcases?.forEach { addTestcase(it) }
        nodeStructureChanged(root)
    }

    fun updateGraphName() {
        if (graphNode is NamableTreeNode) {
            (graphNode as NamableTreeNode).richTextName.reset()
        }
        nodeChanged(graphNode)
    }

    fun updateTestcaseName(testcase: Testcase) {
        findTestcaseNode(testcase)?.let { nodeChanged(it) }
    }

    fun addTestcase(testcase: Testcase): TreeNode {
        val node = DefaultMutableTreeNode(testcase)
        graphNode.add(node)
        nodesWereInserted(graphNode, intArrayOf(graphNode.childCount - 1))
        return node
    }

    fun removeTestcase(testcase: Testcase) {
        val testcaseNode = findTestcaseNode(testcase)
        val index = getTestcaseIndex(testcase)
        if (index >= 0) {
            graphNode.remove(index)
            nodesWereRemoved(graphNode, intArrayOf(index), arrayOf(testcaseNode))
        }
    }

    fun moveTestcase(testcase: Testcase, index: Int): TreeNode {
        val testcaseNode = findTestcaseNode(testcase)
        val oldIndex = getTestcaseIndex(testcase)
        val effIndex = if (oldIndex <= index) index - 1 else index
        (root as DefaultMutableTreeNode).remove(oldIndex)
        (root as DefaultMutableTreeNode).insert(testcaseNode, effIndex)
        nodeStructureChanged(root)
        return testcaseNode!!
    }

    private fun findTestcaseNode(testcase: Testcase): DefaultMutableTreeNode? {
        for (e in graphNode.depthFirstEnumeration()) {
            if ((e as DefaultMutableTreeNode).userObject == testcase) {
                return e
            }
        }
        return null
    }

    private fun getTestcaseIndex(testcase: Testcase): Int {
        for (index in 0 until graphNode.childCount) {
            val item = (graphNode.getChildAt(index) as DefaultMutableTreeNode).userObject as Testcase
            if (item == testcase) {
                return index
            }
        }
        return -1
    }
}