package ch.scorpion.jabbah.base.swing

import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.junit.Assert.assertThat
import org.junit.Test
import javax.swing.tree.DefaultMutableTreeNode

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

		assertThat(JTreeUtil.findTreeNode(node1, { (it as DefaultMutableTreeNode).userObject == "1.1" }) as DefaultMutableTreeNode?, sameInstance(node11))
		assertThat(JTreeUtil.findTreeNode(node1, { (it as DefaultMutableTreeNode).userObject == "1.2" }) as DefaultMutableTreeNode?, sameInstance(node12))
		assertThat(JTreeUtil.findTreeNode(node1, { (it as DefaultMutableTreeNode).userObject == "1.2.1" }) as DefaultMutableTreeNode?, sameInstance(node121))
		assertThat(JTreeUtil.findTreeNode(node1, { (it as DefaultMutableTreeNode).userObject == "something" }), nullValue())
	}
}