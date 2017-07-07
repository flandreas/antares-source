package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor

/**
 * Posted on [EventBus] after a [Component] has been added to a [Drawing] using drag&drop
 */
data class DropEvent(val editor: Editor, val component: Component)