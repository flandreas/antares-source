package io.antarescircuit.antares.model.fsm

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A [Drawing] representing a finite state machine.
 */
class FSMDrawing(
    initialName: TranslatableText = TranslatableText("")
): DrawingImpl<Component>(Name(initialName)) {

    val states: Collection<FSMState> get() = drawables.filterIsInstance<FSMState>()

    val transitions: Collection<FSMTransition> get() = drawables.filterIsInstance<FSMTransition>()

    /** Used as title in property panel. */
    override fun toString(): String = Translations.getString("library.element.fsm.name")

    override fun write(writer: StoreWriter) {
        super.write(writer)
        name.write("name", writer)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        name = Name.read("name", reader)
    }
}