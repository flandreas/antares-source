package ch.scorpion.jabbah.base

import korlibs.template.Template
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Basic test of the Korte Template Engine.*/
@OptIn(ExperimentalCoroutinesApi::class)
class KorteTest {

	@Test
	fun helloWorld() = runTest {
		val template = Template("Hello {{ who }}")
		val rendered = template(mapOf("who" to "world"))
		assertEquals("Hello world", rendered)
	}

	@Test
	fun shouldCallExternalMethod() = runTest {
		val template = Template("Hello {{ function.call('Result') }}")
		val rendered = template(mapOf("function" to Function()))
		assertEquals("Hello Result", rendered)
	}

	class Function {
		fun call(input: String): String = input
	}

	@Test
	fun shouldCallExternalMethodInTag() = runTest {
		val template = Template("""
			{% set entityName = function.call('Result') %}
			{{ entityName }}
		""".trimIndent())
		val rendered = template(mapOf("function" to Function()))
		assertEquals("\nResult", rendered)
	}

	@Test
	fun shouldSetVariable() = runTest {
		val template = Template("""
			{% set a = 5 %}
			a is {{ a }}
		""".trimIndent())
		assertEquals("\na is 5", template())
	}

	@Test
	fun shouldSetVariableConditional() = runTest {
		val template = Template("""
			{% if 1 == 0 %}
			{% set a = "Bla" %}
			{% else %}
			{% set a = "Blu" %}
			{% endif %}
			a is {{ a }}
		""".trimIndent())
		assertEquals("\n\n\na is Blu", template())
	}

	@Test
	fun shouldUseBooleanVariable() = runTest {
		val template = Template("""
			{%- if negative == true %}
			negative
			{%- else %}
			positive
		""".trimIndent())
		val rendered = template(mapOf("negative" to true))
		assertEquals("\nnegative", rendered)
	}
}