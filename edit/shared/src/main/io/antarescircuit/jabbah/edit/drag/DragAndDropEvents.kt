package io.antarescircuit.jabbah.edit.drag

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.Editor

/**
 * Posted when [Component]s are being dragged by the user.
 */
data class DragEvent(val editor: Editor, val components: Collection<Component>)

/**
 * Posted on [EventBus] after a [Component] has been added to a [Drawing] using drag&drop
 */
data class DropEvent(val editor: Editor, val component: Component)

/**
 * Posted on [EventBus] after [DragEvent] if dragging has not been terminated by [DropEvent].
 */
data class EndDragEvent(val editor: Editor)