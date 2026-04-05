package io.antarescircuit.jabbah.base.event

import org.w3c.dom.DragEvent
import org.w3c.dom.events.MouseEvent

typealias MouseEventHandler = (MouseEvent) -> Unit
typealias DragEventHandler = (DragEvent) -> Unit