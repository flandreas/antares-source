package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
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