package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.view.app.GraphViewAppServiceImpl
import ch.scorpion.jabbah.graph.view.graph.GraphViewCopyPasteService
import ch.scorpion.jabbah.graph.view.net.edge.MoveSegmentCommand
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView.Companion.createEastOutputVerticeView
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Creates a [GraphView] using add, connect, move, move segment etc.
 * and then performs a random sequence of undo/redo operations.
 * Used to hunt a bug that sporadically occurs in production, where
 * [Command]s reference the wrong [Component]s.
 */
class RandomUndoRedoTest : AbstractGraphViewEditingTest(snapshotSize = 5) {

	private val blockCount = 16

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		EditModule.drawingAppService = GraphViewAppServiceImpl(GraphViewCopyPasteService())
	}

	override fun setupCircuit() {
		// Cannot run buildCircuit() because CommandManager is not yet bound.
	}

	@Test
	fun test() {
		buildCircuit()

		createRandom(100).also {
			try {
				play(it)
			} catch (e: Throwable) {
				print(it, e)
				throw e
			}
		}
	}

	private fun play(undoRedo: List<Int>) {
		var pos = editor.commandManager.commandCount
		for (item in undoRedo) {
			if (item < 0) {
				for (i in 1 .. abs(item)) {
					//println("Pos $pos: Undo")
					editor.commandManager.undo()
					pos--
				}
			} else {
				for (i in 1 .. item) {
					//println("Pos $pos: Redo")
					editor.commandManager.redo()
					pos++
				}
			}
		}
	}

	private fun createRandom(iterationCount: Int): List<Int> {
		val commandCount = editor.commandManager.commandCount
		var pos = commandCount
		var isUndo = true
		val undoRedo = mutableListOf<Int>()

		for (iteration in 0 until iterationCount) {
			if (isUndo) {
				// Undo
				val count = Random.nextInt(0, pos - 1)
				//val count = pos - 1
				undoRedo.add(-count)
				pos -= count
			} else {
				// Redo
				val count = Random.nextInt(0, commandCount - pos)
				//val count = commandCount - pos
				undoRedo.add(count)
				pos += count
			}
			isUndo = !isUndo
		}

		return undoRedo
	}

	private fun print(undoRedo: List<Int>, e: Throwable) {
		println("Undo/redo protocol: ")
		undoRedo.forEach {
			print(it)
			print(", ")
		}
		print(e)
	}

	private fun buildCircuit() {
		createInitialComponents(0)
		connectInitialComponents(0)

		createBlock(300)

		val components = mutableListOf(
			builder.graphView.getVerticeViews().first { it.model.name == "I1" },
			builder.graphView.getVerticeViews().first { it.model.name == "I2" },
			builder.graphView.getVerticeViews().first { it.model.name == "O" },
		)

		// Copy the first block
		editor.view.selectionManager.select(components)
		EditModule.drawingAppService.copy(editor.view)

		// Paste the first block
		EditModule.drawingAppService.paste(editor.view)


		// Move the pasted block
		EditModule.drawingAppService.move(
			builder.graphView.getWidthIds(listOf(8, 9, 10)),
			Point2D(200 - 30, -30),
			editor,
			register = false)


		// Paste and connect the block n times
		for (i in 0 until blockCount) {
			EditModule.drawingAppService.paste(editor.view)

			// Dummy move
			EditModule.drawingAppService.move(
				listOf(builder.graphView.getVerticeViews().first()),
				Point2D.ZERO,
				editor,
				register = true)

			connectBlock(300 + i * 200, i)
		}
	}

	private fun createInitialComponents(x: Int) {
		EditModule.drawingAppService.add(createEastOutputVerticeView("A", x, 0), editor.view)
		EditModule.drawingAppService.add(createEastOutputVerticeView("B", x + 100, -100), editor.view)
		EditModule.drawingAppService.add(createEastOutputVerticeView("C", x + 200, -200), editor.view)
	}

	private fun connectInitialComponents(x: Int) {
		connect(Point2D(x + 20, 0), Point2D(x + 100 - 20, -100))
	}

	private fun createBlock(x: Int) {
		EditModule.drawingAppService.add(createEastOutputVerticeView("I1", x, -100), editor.view)
		EditModule.drawingAppService.add(createEastOutputVerticeView("I2", x, -200), editor.view)
		EditModule.drawingAppService.add(createEastOutputVerticeView("O", x + 100, -200), editor.view)
	}

	private fun connectBlock(x: Int, index: Int) {
		when (index) {
			0 -> connect(Point2D(x - 240, 0), Point2D(x - 20, -100), Modifier.Alt.mask)
			1 -> connect(Point2D(x - 320, 0), Point2D(x - 20, -100), Modifier.Alt.mask)
			2 -> connect(Point2D(x - 360, 0), Point2D(x - 20, -100), Modifier.Alt.mask)
			3 -> connect(Point2D(x - 380, 0), Point2D(x - 20, -100), Modifier.Alt.mask)
			4 -> connect(Point2D(x - 390, 0), Point2D(x - 20, -100), Modifier.Alt.mask)
			5 -> connect(Point2D(x - 395, 0), Point2D(x - 20, -100), Modifier.Alt.mask)
			else -> connect(Point2D(x - 398, 0), Point2D(x - 20, -100), Modifier.Alt.mask)
		}

		// add no-op MoveSegmentCommand
		val lastEdgeView = editor.view.drawing.drawables.first() as EdgeView<*>
		editor.commandManager.execute(MoveSegmentCommand(editor, lastEdgeView.id, 1, 0.0))

		connect(Point2D(x - 100 + 20, -200), Point2D(x - 20, -200))
	}

	private fun connect(begin: Point2D, end: Point2D, modifiers: Int = 0) {
		driver.mouseMoveTo(begin.xInt, begin.yInt, modifiers)
		driver.pressMouseAt(begin.xInt, begin.yInt, modifiers)
		driver.dragMouseTo(end.xInt, end.yInt)
		driver.releaseMouseAt(end.xInt, end.yInt)
	}
}