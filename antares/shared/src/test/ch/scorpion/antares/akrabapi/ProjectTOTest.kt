package ch.scorpion.antares.akrabapi

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ProjectTOTest {

	@Test
	fun shouldDeserializeProjectTO() {
		val msg = """
			[{
				"uuid":"bdd1210c-1623-4dc2-acc8-7fe69d6ebd8e",
				"name":{
					"translations":[{
						"language":"English",
						"text":"My second project"
					}]
				},
				"author":{"id":"auth0|6184e440be040e0068472fb3"},
				"isSystem":false,
				"description":null,
				"importedLibrary":"cb21300b-8f5d-4c64-8f37-5d9a49807e8c",
				"public":false
			}]
		""".trimIndent()
		val projects: List<ProjectTO> = Json.decodeFromString(msg)

		assertEquals(1, projects.size)
		with(projects.first()) {
			assertEquals("bdd1210c-1623-4dc2-acc8-7fe69d6ebd8e", uuid)
			assertEquals("My second project", name.getText())
			assertEquals("auth0|6184e440be040e0068472fb3", author.id)
			assertFalse(isSystem)
			assertNull(description)
			assertEquals("cb21300b-8f5d-4c64-8f37-5d9a49807e8c", importedLibrary)
		}
	}
}