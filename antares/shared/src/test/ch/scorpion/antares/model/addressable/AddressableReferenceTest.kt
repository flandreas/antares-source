package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.view.AbstractGraphViewEditingTest
import ch.scorpion.antares.view.addressable.ROMView
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.model.vertice.ImmediateVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.Test

class AddressableReferenceTest : AbstractGraphViewEditingTest(5) {

    private lateinit var ref: AddressableReference

    private val andGateView: LogicGateView get() = view.drawing.getWithId(1) as LogicGateView
    private val romView: ROMView get() = view.drawing.getWithId(2) as ROMView

    override fun setupCircuit() {
        // nothing to do before data holder is bound
    }

    private fun prepare() {
        service.add(LogicGateView.andGateView(), view)
        service.add(ROMView(), view)

        ref = AddressableReference(ImmediateVerticeLink(romView.modelId) as ObjectLink<Addressable>, view as DrawingView<GraphView>)
    }

    /**
     * Regression test for bug #714, where [AddressableReference] tried to access its [Addressable]
     * while handling a [ApplicationDataContentEvent] (due to creation of a new snapshot in undo),
     * but BEFORE the AddComponent commands were executed. This lead to accessing an empty snapshot
     * and therefore to an exception.
     */
    @Test
    fun shouldUpdateAddressableReference() {
        prepare()
        service.delete(listOf(andGateView), view)
        editor.commandManager.undo()
    }
}