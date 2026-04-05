package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.graph.ui.container.SymbolComparatorController
import io.antarescircuit.jabbah.graph.ui.container.SymbolComparatorView
import io.antarescircuit.jabbah.graph.ui.library.BasicLibraryTreeView
import dev.mokkery.MockMode
import dev.mokkery.mock

class SymbolComparatorViewMockBuilder(controller: SymbolComparatorController) {

    private val symbolComparatorView = mock<SymbolComparatorView>(MockMode.autofill)
    private val basicLibraryTreeView = mock<BasicLibraryTreeView>(MockMode.autofill)

    init {
        controller.view = symbolComparatorView
        controller.libraryTreeViewController.view = basicLibraryTreeView
    }

    fun build(): SymbolComparatorView = symbolComparatorView
}