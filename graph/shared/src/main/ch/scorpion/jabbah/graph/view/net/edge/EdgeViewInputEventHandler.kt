package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.EdgeView

/**
 * Base class for implementing [InputEventHandler]s for [EdgeView]s depending on their [Layout].
 */
open class EdgeViewInputEventHandler(var edgeView: EdgeView<*>? = null) : InputEventHandlerAdapter<EditInputEventContext>()