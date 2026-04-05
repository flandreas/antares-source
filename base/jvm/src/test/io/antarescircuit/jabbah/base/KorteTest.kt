package io.antarescircuit.jabbah.base

import korlibs.template.AutoEscapeMode
import korlibs.template.Template
import korlibs.template.TemplateConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Basic test of the Korte Template Engine.*/
class KorteTest {

	class Function {
		val property: String get() = "property"
		fun call(input: String): String = input
	}

	@Test
	fun helloWorld() = runTest {
		val template = Template("Hello {{ who }}")
		val rendered = template(mapOf("who" to "world"))
		assertEquals("Hello world", rendered)
	}

	@Test
	fun shouldAccessExternalProperty() = runTest {
		val template = Template("Hello {{ bean.property }}")
		val rendered = template(mapOf("bean" to Function()))
		assertEquals("Hello property", rendered)
	}

	@Test
	fun shouldCallExternalMethod() = runTest {
		val template = Template("Hello {{ function.call('Result') }}")
		val rendered = template(mapOf("function" to Function()))
		assertEquals("Hello Result", rendered)
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

	@Test
	fun shouldUseNullStringVariable() = runTest {
		val template = Template("""
			{%- if label == '' || label == null %}
			not set
			{%- else %}
			{{ label }}
		""".trimIndent())
		val rendered = template(mapOf("label" to null))
		assertEquals("\nnot set", rendered)
	}

	@Test
	fun shouldUseEmptyStringVariable() = runTest {
		val template = Template("""
			{%- if label == '' || label == null %}
			not set
			{%- else %}
			{{ label }}
		""".trimIndent())
		val rendered = template(mapOf("label" to ""))
		assertEquals("\nnot set", rendered)
	}

	@Test
	fun shouldSetEmptyStringVariable() = runTest {
		val template = Template("""
			{%- if label == '' || label == null %}
			not set
			{%- else %}
			{{ label }}
		""".trimIndent())
		val rendered = template(mapOf("label" to "abc"))
		assertEquals("\nabc", rendered)
	}

	@Test
	fun shouldOutputStringWithQuotes() = runTest {
		val template = Template("Hello {{ who }}", TemplateConfig(autoEscapeMode = AutoEscapeMode.RAW))
		val rendered = template(mapOf("who" to "\"Quoted\""))
		assertEquals("Hello \"Quoted\"", rendered)
	}
}