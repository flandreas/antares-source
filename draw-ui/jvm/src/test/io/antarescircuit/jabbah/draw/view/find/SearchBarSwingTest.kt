package io.antarescircuit.jabbah.draw.view.find

import io.antarescircuit.jabbah.draw.module.DrawModuleUiJvm
import javax.swing.JTextField
import javax.swing.SwingUtilities
import kotlin.test.*

class SearchBarSwingTest {

	@Test
	fun shouldReapplyVisibleQueryWithoutChangingItAfterDrawingReplacement() {
		SwingUtilities.invokeAndWait {
			DrawModuleUiJvm.require()
			val requests = mutableListOf<SearchRequest>()
			val searchable = object : Searchable {
				override val canSearch = true
				override val searchResult = SearchResult(3)
				override fun execute(request: SearchRequest) { requests.add(request) }
			}
			val bar = SearchBarSwing(searchable)
			val field = bar.components.filterIsInstance<JTextField>().single()
			field.text = "clock"
			requests.clear()
			bar.refreshSearch()
			assertEquals("clock", field.text)
			assertEquals(1, requests.size)
			assertEquals("clock", requests.single().searchString)
		}
	}
}
