package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.view.AbstractGraphViewEditingTest
import io.antarescircuit.antares.view.addressable.ROMView
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.app.ApplicationDataContentEvent
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.model.vertice.ImmediateVerticeLink
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlin.test.Test

class AddressableReferenceTest : AbstractGraphViewEditingTest(5) {

    private lateinit var ref: AddressableReference

    private val andGateView: LogicGateView get() = view.drawing.getWithId(1) as LogicGateView
    private val romView: ROMView get() = view.drawing.getWithId(2) as ROMView

    override fun setupCircuit() {
        // nothing to do before data holder is bound
    }

    @Suppress("UNCHECKED_CAST")
    private fun prepare() {
        service.add(LogicGateView.andGateView(), view as DrawingView<Component, Drawing<Component>>)
        service.add(ROMView(), view as DrawingView<Component, Drawing<Component>>)

        ref = AddressableReference(
            ImmediateVerticeLink(romView.modelId) as ObjectLink<Addressable>,
            view as DrawingView<GraphElementView<*>, GraphView>
        )
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