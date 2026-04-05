package io.antarescircuit.antares.view.container

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ToolBar
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.ComponentAtLocationTool
import io.antarescircuit.jabbah.graph.container.ContainerEditor
import io.antarescircuit.jabbah.graph.container.ContainerToolBarBuilder

class DigitalContainerToolBarBuilder : ContainerToolBarBuilder() {

	override fun buildDrawingToolsToolbar(application: Application?, editor: ContainerEditor, separator: Boolean): ToolBar {
		val toolBar = super.buildDrawingToolsToolbar(application, editor, separator)
		toolBar.addTool(ComponentAtLocationTool(editor, factory = { DilCase() } ), "/img/DIL.png", Translations.getString("antares.dilCase.name"))
		return toolBar
	}
}