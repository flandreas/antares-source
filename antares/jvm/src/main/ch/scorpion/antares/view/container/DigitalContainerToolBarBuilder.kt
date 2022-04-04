package ch.scorpion.antares.view.container

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.ComponentAtLocationTool
import ch.scorpion.jabbah.graph.container.ContainerEditor
import ch.scorpion.jabbah.graph.container.ContainerToolBarBuilder

class DigitalContainerToolBarBuilder : ContainerToolBarBuilder() {

	override fun buildDrawingToolsToolbar(application: Application?, editor: ContainerEditor, separator: Boolean): ToolBar {
		val toolBar = super.buildDrawingToolsToolbar(application, editor, separator)
		toolBar.addTool(ComponentAtLocationTool(editor, factory = { DilCase() } ), "/img/DIL.png", Translations.getString("antares.dilCase.name"))
		return toolBar
	}
}