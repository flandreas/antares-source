package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Editor
import java.awt.BorderLayout

/**
 * An [AbstractApplicationFrame] that contains a [SimpleEditorPanel] in the center of its layout.
 */
class SimpleApplicationFrame(
    application: DesktopApplication,
    override val editor: Editor,
    eventBus: EventBus,
    toolbars: List<ToolBar>
) : AbstractApplicationFrame(application, eventBus, toolbars) {

    constructor(application: DesktopApplication, editor: Editor, toolbars: List<ToolBar>): this(application, editor, BaseModule.eventBus, toolbars)

    init {
        add(SimpleEditorPanel(editor.view.canvas, editor), BorderLayout.CENTER)
    }
}