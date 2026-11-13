package io.antarescircuit.antares.view.truthtable

import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.truthtable.KarnaughCell
import io.antarescircuit.antares.model.truthtable.KarnaughRectangle
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.Undoable
import java.awt.Point
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals

class KarnaughMapViewTest {

	@Test
	fun shouldCycleAClickedCellAsUndoableEditAndFollowTheTruthTable() {
		SwingUtilities.invokeAndWait {
			AntaresTestRule.configure()
			val model = TruthTable("Test", listOf("A", "B"), listOf("O"))
			val ref = TruthTableReference({ model })
			try {
				val commands = mutableListOf<Command>()
				val captured = Capture.slot<Command>()
				val manager = mock<CommandManager>()
				every { manager.execute(capture(captured)) } calls {
					captured.get().let { commands.add(it); it.execute() }
				}
				val view = KarnaughMapView(ref, manager)
				val component = view.mapComponent!!
				component.size = component.preferredSize
				// The bottom right cell (A=1, B=1) represents truth table row 3
				val center = component.cellBounds(KarnaughCell(0, 1, 1)).let { Point(it.centerX.toInt(), it.centerY.toInt()) }
				val click = {
					component.dispatchEvent(MouseEvent(component, MouseEvent.MOUSE_PRESSED, 0, InputEvent.BUTTON1_DOWN_MASK,
						center.x, center.y, 1, false, MouseEvent.BUTTON1))
				}

				click()
				assertEquals(Bit.True, model.getValue(3, 2))
				assertEquals(listOf(listOf(KarnaughRectangle(0, 1, 1, 1, 1))), view.implicants)

				click()
				assertEquals(Bit.Error, model.getValue(3, 2))
				assertEquals(emptyList(), view.implicants)

				(commands.last() as Undoable).undo()
				assertEquals(Bit.True, model.getValue(3, 2))
				assertEquals(1, view.implicants.size)
				assertEquals(2, commands.size)
			} finally {
				ref.dispose()
			}
		}
	}
}
