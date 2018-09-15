package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.base.swing.JTreeUtil
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.vertice.TestControlVerticeView
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

/** Unit tests for [ContainerTree].*/
class ContainerTreeTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphViewTestRule()
	}

	@Before
	fun setup() {
		TestTranslationsBuilder().withAnyKey()
	}

	@Test
	fun shouldBuildWithControlView() {
		val testGraphView = TestGraphView()
		val source = TestControlVerticeView()
		testGraphView.graphView.add(source)

		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = ContainerDrawing())

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun controlOfContainerShouldNotBeAddedToTree() {
		val testGraphView = TestGraphView()
		val source = TestControlVerticeView()
		testGraphView.graphView.add(source)
		val containerDrawing = ContainerDrawing()
		containerDrawing.add(ControlViewComponent(controlView = source.createControlView() as ControlView<Vertice>))

		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = containerDrawing)

		assertControlView(containerTree, nullValue())
	}

	@Test
	fun shouldAddControlView() {
		val testGraphView = TestGraphView()
		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = ContainerDrawing())
		val source = TestControlVerticeView()
		testGraphView.graphView.add(source)

		containerTree.addControlViewSource(source as ControlViewSource<Vertice>)

		assertControlView(containerTree, notNullValue())
	}

	@Test
	fun shouldRemoveControlView() {
		val testGraphView = TestGraphView()
		val source = TestControlVerticeView()
		testGraphView.graphView.add(source)
		val containerTree = ContainerTree(graphView = testGraphView.graphView, containerDrawing = ContainerDrawing())

		containerTree.removeControlViewSource(source.controlId!!)

		assertControlView(containerTree, nullValue())
	}

	private fun assertControlView(containerTree: ContainerTree, matcher: Matcher<in TreeNode?>) {
		assertThat(JTreeUtil.findTreeNode(containerTree.treeModel.root as TreeNode) {
			it is DefaultMutableTreeNode
				&& it.userObject is DraggableTreeItem
				&& (it.userObject as DraggableTreeItem).type == ContainerTreeItemType.Control }, matcher)
	}
}