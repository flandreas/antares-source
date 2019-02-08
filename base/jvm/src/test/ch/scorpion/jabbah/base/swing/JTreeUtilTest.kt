package ch.scorpion.jabbah.base.swing

import javax.swing.tree.DefaultMutableTreeNode
import kotlin.test.*

/** Unit tests for [JTreeUtil].*/
class JTreeUtilTest {

	@Test
	fun shouldFindTreeNode() {
		val node1 = DefaultMutableTreeNode("1")
		val node11 = DefaultMutableTreeNode("1.1")
		val node111 = DefaultMutableTreeNode("1.1.1")
		val node12 = DefaultMutableTreeNode("1.2")
		val node121 = DefaultMutableTreeNode("1.2.1")
		node1.add(node11)
		node11.add(node111)
		node1.add(node12)
		node12.add(node121)

		assertSame(node11, JTreeUtil.findTreeNode(node1) { (it as DefaultMutableTreeNode).userObject == "1.1" } as DefaultMutableTreeNode?)
		assertSame(node12, JTreeUtil.findTreeNode(node1) { (it as DefaultMutableTreeNode).userObject == "1.2" } as DefaultMutableTreeNode?)
		assertSame(node121, JTreeUtil.findTreeNode(node1) { (it as DefaultMutableTreeNode).userObject == "1.2.1" } as DefaultMutableTreeNode?)
		assertNull(JTreeUtil.findTreeNode(node1) { (it as DefaultMutableTreeNode).userObject == "something" })
	}
}