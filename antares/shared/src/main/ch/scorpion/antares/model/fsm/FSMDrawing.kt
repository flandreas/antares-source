package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name

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
}