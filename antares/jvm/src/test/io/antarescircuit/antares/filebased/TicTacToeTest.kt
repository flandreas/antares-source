package io.antarescircuit.antares.filebased

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.output.LED
import io.antarescircuit.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.MouseEventImpl
import io.antarescircuit.jabbah.base.event.MouseEventType
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.graph.library.FileLibraryPersistenceService
import io.antarescircuit.jabbah.graph.library.LibraryIdentification
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.project.ProjectManagementService
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractSubGraphVerticeView
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Plays winning and drawn games through the Keyboard symbol in the Tic-Tac-Toe sample's Main circuit. */
class TicTacToeTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView

	override fun getCircuitView(): GraphView = circuitView

	override fun setup() {
		super.setup()
		AbstractStandardLibraryBasedCircuitTest.setupLibrary()
		val path = Paths.get("samples").toAbsolutePath().toString()
		ProjectModule.projectDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService({ path }, "projects"))
		ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService({ path }, "projects")
		ProjectModule.projectManagementService = ProjectManagementService()
		val project = ProjectModule.projectManagementService.load(
			LibraryIdentification(UUID("87e048a2-3e58-4155-b7fc-7de69576b9b3"), null),
		)
		LibraryModule.libraryHolder.l = project
		circuitView = project.getMetaGraph(UUID("d2f41129-f90f-4905-85a7-a02881ac7754")).graph.graphView
	}

	@Test
	fun shouldLightPlayer1WinsAfterCompletingTopRowDeep() = checkPlayer1Wins(deep = true)

	@Test
	fun shouldLightPlayer1WinsAfterCompletingTopRowShallow() = checkPlayer1Wins(deep = false)

	@Test
	fun shouldLightDrawAfterFillingBoardWithoutWinnerDeep() = checkDraw(deep = true)

	@Test
	fun shouldLightDrawAfterFillingBoardWithoutWinnerShallow() = checkDraw(deep = false)

	private fun checkPlayer1Wins(deep: Boolean) {
		scheduler.isDeepExecution = deep
		scheduler.isSoftBreakpointsEnabled = false
		try {
			startSimulation()
			settle()
			assertFalse(scheduler.isInitializing)

			val turnIndicator = circuitView.getWithId(11) as AbstractSubGraphVerticeView
			val arbitrator = circuitView.getWithId(20) as AbstractSubGraphVerticeView
			val player1Turn = led(turnIndicator, "Player 1")
			val player2Turn = led(turnIndicator, "Player 2")
			val player1Wins = led(arbitrator, "Player 1 wins")
			val player2Wins = led(arbitrator, "Player 2 wins")
			val draw = led(arbitrator, "Draw")
			assertTrue(player1Turn.isOn)
			assertFalse(player2Turn.isOn)
			assertFalse(player1Wins.isOn)
			assertFalse(player2Wins.isOn)
			assertFalse(draw.isOn)

			// Squares are numbered 1..9, left to right and top to bottom.
			// Player 1 takes the top row; Player 2 takes the first two middle-row squares.
			listOf(1, 4, 2, 5, 3).forEachIndexed { move, square ->
				clickSquare(square)
				if (move < 4) {
					assertEquals(move % 2 != 0, player1Turn.isOn, "Player 1 turn after move ${move + 1}")
					assertEquals(move % 2 == 0, player2Turn.isOn, "Player 2 turn after move ${move + 1}")
					assertFalse(player1Wins.isOn, "Player 1 must not win before completing the top row")
				}
			}

			assertTrue(player1Wins.isOn, "Arbitrator's Player 1 wins LED must be on")
			assertFalse(player2Wins.isOn)
			assertFalse(draw.isOn)
			assertNoIssues()
		} finally {
			stopSimulation()
		}
	}

	private fun checkDraw(deep: Boolean) {
		scheduler.isDeepExecution = deep
		scheduler.isSoftBreakpointsEnabled = false
		try {
			startSimulation()
			settle()
			assertFalse(scheduler.isInitializing)

			val turnIndicator = circuitView.getWithId(11) as AbstractSubGraphVerticeView
			val arbitrator = circuitView.getWithId(20) as AbstractSubGraphVerticeView
			val player1Turn = led(turnIndicator, "Player 1")
			val player2Turn = led(turnIndicator, "Player 2")
			val player1Wins = led(arbitrator, "Player 1 wins")
			val player2Wins = led(arbitrator, "Player 2 wins")
			val draw = led(arbitrator, "Draw")
			assertFalse(player1Wins.isOn)
			assertFalse(player2Wins.isOn)
			assertFalse(draw.isOn)

			// Final board (X = Player 1, O = Player 2): XOX / XOO / OXX.
			listOf(1, 2, 3, 5, 4, 6, 8, 7, 9).forEachIndexed { move, square ->
				assertEquals(move % 2 == 0, player1Turn.isOn, "Player 1 turn before move ${move + 1}")
				assertEquals(move % 2 != 0, player2Turn.isOn, "Player 2 turn before move ${move + 1}")
				clickSquare(square)
				assertFalse(player1Wins.isOn, "Player 1 must not win after move ${move + 1}")
				assertFalse(player2Wins.isOn, "Player 2 must not win after move ${move + 1}")
				assertEquals(move == 8, draw.isOn, "Draw must light only when the board is full (move ${move + 1})")
			}
			assertNoIssues()
		} finally {
			stopSimulation()
		}
	}

	private fun led(symbol: AbstractSubGraphVerticeView, name: String): LED =
		symbol.getControlViewComponents().single { it.model.name == name }.model as LED

	private fun clickSquare(square: Int) {
		val keyboard = circuitView.getWithId(1) as AbstractSubGraphVerticeView
		val button = keyboard.getControlViewComponents().single { it.controlModelLink.toStoreFormat() == "$square/31" }
		val x = keyboard.location.x + button.boundingBox.centerX
		val y = keyboard.location.y + button.boundingBox.centerY
		val view = DrawingViewMockBuilder().withDrawing(circuitView).build<GraphElementView<*>, GraphView>()
		fun contextFor(type: MouseEventType) = ActorInteractionContext(
			signalHandler = scheduler,
			view = view,
			mouseEvent = MouseEventImpl(type, x = x.toInt(), y = y.toInt(), button = Button.BUTTON1, clickCount = 1),
			x = x,
			y = y,
		)
		val context = contextFor(MouseEventType.PRESSED)
		val actor = assertNotNull(circuitView.getDrawableAt(x, y) { it is ActorView } as? ActorView)
		val handler = actor.getActorInteractionHandler(context)
		val captured = handler.mousePressed(context)
		settle()
		(captured ?: handler).mouseReleased(contextFor(MouseEventType.RELEASED))
		settle()
	}

	private fun settle() {
		// The Stop watch contains a running clock, so the scheduling queue never empties.
		proceedToNanos(scheduler.executionTime + 20_000)
	}
}
