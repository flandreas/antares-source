package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.View
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * A simple application [JPanel] that displays a [View] of an [Editor], which it updates whenever
 * an [ApplicationDataEvent] gets posted on the [EventBus].
 */
class SimpleEditorPanel(
	private val canvas: Canvas,
	private val editor: Editor,
	private val eventBus: EventBus
) : JPanel() {

	constructor(canvas: Canvas, editor: Editor) : this(canvas, editor, BaseModule.eventBus)

	private val applicationDataEventHandler: (ApplicationDataEvent) -> Unit = { handle(it) }

	init {
		buildUI()
		eventBus.register(ApplicationDataEvent::class, applicationDataEventHandler)
	}

	fun dispose() {
		eventBus.unregister(ApplicationDataEvent::class, applicationDataEventHandler)
	}

	fun handle(event: ApplicationDataEvent) {
		if (event.newData?.content is Drawing<*>) {
			@Suppress("UNCHECKED_CAST")
			editor.view.drawing = event.newData.content as Drawing<Component>
		}
	}

	private fun buildUI() {
		layout = BorderLayout()
		add(canvas as JComponent)
	}
}