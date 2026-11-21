package io.antarescircuit.antares.web

import io.antarescircuit.antares.hdl.HDLExportParams
import io.antarescircuit.antares.hdl.HDLExportTestBenchParams
import io.antarescircuit.antares.hdl.HDLLanguage
import io.antarescircuit.antares.hdl.createHDLTestBenchName
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * HDL (VHDL/Verilog) export of the open circuit (desktop: `ExportHDLAction`, `ExportHDLPanel`).
 * Parameter definitions of the open circuit are provided by [params].
 */
@JsExport
class WebHdl internal constructor(private val ctx: WebEditorContext) {

	companion object {
		private const val DEF_WAIT_TIME = 30L

		/** Minimum wait time between test vectors, as enforced by the desktop dialog. */
		private const val MIN_WAIT_TIME = 10
	}

	/** Parameter definitions of the open circuit. */
	val params: WebParams = WebParams(ctx)

	private val circuit: DigitalGraph? get() = (ctx.editor.drawing as? GraphView)?.graph as? DigitalGraph

	private fun language(id: String): HDLLanguage = HDLLanguage.entries.first { it.name.equals(id, ignoreCase = true) }

	/**
	 * Returns the defaults of the export dialog for [language] ("vhdl" or "verilog") as JSON,
	 * or `null` if no digital circuit is open.
	 */
	fun exportInfo(language: String): String = ctx.guarded("null") {
		val circuit = circuit ?: return@guarded "null"
		val lang = language(language)
		val baseName = lang.createRenaming().checkName(StringUtils.simplify(RichText.stripToPlainText(circuit.name.value)))
		val propDelay = circuit.effectivePropagationDelay
		val waitTime = if (propDelay > 0) propDelay else DEF_WAIT_TIME
		val testcases = circuit.testcases.testcases.map { t ->
			obj {
				this.id = t.id
				this.name = t.name.getTranslation()
			}
		}
		val title = Translations.getString("${lang.keyPrefix}.action.name")
		val minWaitTime = MIN_WAIT_TIME
		val labels = mapOf(
			"testcase" to Translations.getString("antares.vhdl.testcase.name"),
			"waitTime" to Translations.getString("antares.vhdl.waitTime.name"),
			"delayModel" to Translations.getString("${lang.keyPrefix}.delayModel.name"),
			"baseName" to Translations.getString("antares.vhdl.baseFileName.name"),
		)
		val labelsJs: dynamic = obj { }
		labels.forEach { (key, value) -> labelsJs[key] = value }
		toJson(obj {
			this.title = title
			this.fileExtension = lang.fileExtension
			this.baseName = baseName
			this.waitTime = waitTime.toInt()
			this.minWaitTime = minWaitTime
			this.testcases = testcases.toJsArray()
			this.labels = labelsJs
		})
	}

	/**
	 * Exports the open circuit and returns the created files as JSON (`{files: [{name, content}]}`),
	 * or `{error}` with a translated message if the parameters are invalid or the circuit cannot be exported.
	 * @param testcaseId the ID of the test case to create a test bench with, or -1 for none
	 */
	fun export(language: String, baseName: String, testcaseId: Int, waitTime: Int, useDelayModel: Boolean): String =
		ctx.guarded("null") {
			val circuit = circuit ?: return@guarded "null"
			val lang = language(language)
			try {
				val params = validatedParameters(circuit, lang, baseName, testcaseId, waitTime, useDelayModel)
				val result = lang.generate(params, circuit)
				val files = listOfNotNull(
					file("${params.baseName}${lang.fileExtension}", result.hdl),
					params.testBenchParams?.let { tb -> result.testBench?.let { file("${tb.testBenchName}${lang.fileExtension}", it) } },
				)
				toJson(obj { this.files = files.toJsArray() })
			} catch (e: IllegalArgumentException) {
				error(e.message ?: "")
			} catch (e: HDLException) {
				error(e.message ?: "")
			} catch (e: DslError) {
				error(Translations.getString("antares.vhdl.testcase.error.text", e.message ?: ""))
			}
		}

	private fun file(name: String, content: String): dynamic = obj {
		this.name = name
		this.content = content
	}

	private fun error(message: String): String = toJson(obj { this.error = message })

	/**
	 * Validates the user input like the desktop's `ExportHDLPanel`.
	 * @throws IllegalArgumentException with a translated message in case of an invalid parameter
	 */
	private fun validatedParameters(
		circuit: DigitalGraph,
		lang: HDLLanguage,
		baseName: String,
		testcaseId: Int,
		waitTime: Int,
		useDelayModel: Boolean,
	): HDLExportParams {
		require(StringUtils.isNotBlank(baseName)) { Translations.getString("antares.vhdl.fileNameEmpty.error.txt") }
		val renaming = lang.createRenaming()
		val normalizedBaseName = renaming.checkName(StringUtils.simplify(baseName))
		require(baseName == normalizedBaseName) { Translations.getString("antares.vhdl.fileNameInvalid.error.txt", normalizedBaseName) }

		val testcase = circuit.testcases.testcases.firstOrNull { it.id == testcaseId }
		val testBenchParams = testcase?.let {
			require(waitTime >= MIN_WAIT_TIME) { Translations.getString("edit.magnitude.invalidValue.text", "$waitTime") }
			HDLExportTestBenchParams(renaming, createHDLTestBenchName(baseName, it, renaming), it, waitTime)
		}
		return HDLExportParams(renaming, baseName, useDelayModel, testBenchParams)
	}
}
