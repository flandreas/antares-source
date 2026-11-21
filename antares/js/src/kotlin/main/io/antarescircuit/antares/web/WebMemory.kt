package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.AddressableCellChange
import io.antarescircuit.antares.model.addressable.AddressableCellChangeCommand
import io.antarescircuit.antares.model.addressable.AddressableClearCommand
import io.antarescircuit.antares.model.addressable.AddressableCommentChange
import io.antarescircuit.antares.model.addressable.AddressableCommentChangeCommand
import io.antarescircuit.antares.model.addressable.LogisimMemoryDump
import io.antarescircuit.antares.model.addressable.LookupTable
import io.antarescircuit.antares.model.addressable.MemoryDump
import io.antarescircuit.antares.model.addressable.MemoryLibraryItem
import io.antarescircuit.antares.model.addressable.MemoryStorable
import io.antarescircuit.antares.model.addressable.RAM
import io.antarescircuit.antares.model.addressable.ROM
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.addressable.OpenMemoryContentsRequest
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.edit.Command
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Memory contents: memory library items (opened as documents of kind `memory`) and the contents of RAM, ROM and
 * lookup table components (shown in the `memoryContents` dialog, see [OpenMemoryContentsRequest]).
 *
 * Most functions take a `target`: `"document"` for the open memory item, `"dialog"` for the component whose
 * contents are shown in the dialog. Values are exchanged as text in a `format`: `hex`, `dec`, `oct` or `bin`.
 * Edits are undoable commands on the editor's command manager like on the desktop (`AddressableContentsPanel`),
 * except for RAMs, whose contents only exist while simulating and are changed directly.
 */
@JsExport
class WebMemory internal constructor(private val ctx: WebEditorContext) {

	/** The memory being displayed, resolved anew for every call because undo/redo may exchange the model. */
	private class Target(
		val addressable: Addressable,
		val view: DrawingView<GraphElementView<*>, GraphView>?,
		val link: ObjectLink<Addressable>,
		val title: String,
		val type: String,
		val editable: Boolean,
	)

	private val isExecute get() = ctx.frame().applicationModeHolder.currentMode.isExecute()

	private val scheduler get() = ctx.frame().applicationContextHolder.scheduler

	private val documentLink = object : ObjectLink<Addressable> {
		override fun getLinkedObject(startGraph: Graph?): Addressable = ctx.dataController().data!!.content as MemoryStorable
	}

	/** The circuit that was open when the dialog's request was first seen, to detect a stale dialog. */
	private var dialogCircuit: Pair<OpenMemoryContentsRequest, String?>? = null

	private fun target(which: String): Target? = if (which == "dialog") dialogTarget() else documentTarget()

	private fun documentTarget(): Target? {
		if (ctx.document()?.kind != WebItemKinds.MEMORY) return null
		val data = ctx.dataController().data ?: return null
		val storable = data.content as? MemoryStorable ?: return null
		val editable = (data.savable?.editable ?: false) && !isExecute
		return Target(storable, null, documentLink, RichText.stripToPlainText(storable.name.value), "memory", editable)
	}

	private fun dialogTarget(): Target? {
		val request = ctx.dialog()?.takeIf { it.kind == WebItemKinds.MEMORY_CONTENTS }?.payload as? OpenMemoryContentsRequest ?: return null
		val circuit = (ctx.dataController().data?.content as? MetaGraph)?.uuid?.id
		val opened = dialogCircuit?.takeIf { it.first === request }?.second ?: circuit.also { dialogCircuit = request to it }
		if (opened != circuit) return null
		val addressable = try {
			request.link.getLinkedObject(request.drawingView.drawing.graph)
		} catch (_: Throwable) {
			return null
		}
		val editable = if (addressable.storesCells) request.drawingView.editable && !isExecute else isExecute
		val type = when (addressable) {
			is ROM -> "rom"
			is RAM -> "ram"
			is LookupTable -> "lookupTable"
			else -> "memory"
		}
		return Target(addressable, request.drawingView, request.link, request.name, type, editable)
	}

	/** Runs [command] undoably, or directly for memories that are only changed while simulating (RAM). */
	private fun run(target: Target, command: Command) {
		if (target.addressable.storesCells) ctx.editor.commandManager.execute(command) else command.execute()
		ctx.notifyChanged()
	}

	/** ---- Reading */

	/**
	 * Describes the memory of [target] as JSON, or `null` if there is none (e.g. the component was deleted):
	 * title, type (memory|rom|ram|lookupTable), widths, maxAddress, editable, simulating, disassembly (whether the
	 * memory has a disassembly column), currentAddress (the cell last accessed while simulating, or -1),
	 * dataSource and linkedMemory (the memory item a ROM is loaded from).
	 */
	fun info(target: String): String = ctx.guarded("null") {
		val t = target(target) ?: return@guarded "null"
		val a = t.addressable
		val simulating = isExecute
		// The last accessed cell; unlike the desktop also while the chip isn't selected, so it doesn't flicker
		val current = if (simulating && t.type != "memory") a.currentAddress.takeIf { it in 0..a.maxAddress } ?: -1 else -1
		val linked = (a as? ROM)?.takeIf { it.memoryStorableUuid != null }?.findMemoryLibraryItem()
		val linkedName = linked?.let { RichText.stripToPlainText(it.name.value) }
		toJson(obj {
			this.title = t.title
			this.type = t.type
			this.addressWidth = a.addressWidth.width
			this.dataWidth = a.dataWidth.width
			this.maxAddress = a.maxAddress
			this.editable = t.editable
			this.simulating = simulating
			this.disassembly = a.disassemblyWidth > 0
			this.currentAddress = current
			this.dataSource = a.dataSource
			this.linkedMemory = linked?.let { item -> obj { this.uuid = item.uuid.id; this.name = linkedName } }
		})
	}

	/**
	 * Returns up to [count] cells starting at address [start] as JSON: `values` (rendered in [format]),
	 * `comments` (empty strings for cells without comment) and `disassembly` (or `null`).
	 */
	fun cells(target: String, start: Int, count: Int, format: String): String = ctx.guarded("null") {
		val t = target(target) ?: return@guarded "null"
		val a = t.addressable
		val end = minOf(start.toLong() + count - 1, a.maxAddress.toLong()).toInt()
		if (start < 0 || end < start) return@guarded toJson(obj { this.values = emptyArray<String>(); this.comments = emptyArray<String>(); this.disassembly = null })
		val width = a.dataWidth
		val addresses = start..end
		val values = addresses.map { render(a.dataAt(it), width, format) }.toJsArray()
		val comments = addresses.map { a.commentAt(it) ?: "" }.toJsArray()
		val disassembly = if (a.disassemblyWidth > 0) addresses.map { a.disassemblyAt(it) }.toJsArray() else null
		toJson(obj {
			this.values = values
			this.comments = comments
			this.disassembly = disassembly
		})
	}

	/** ---- Editing */

	/**
	 * Sets the cells starting at [start] to [texts] (values in [format]) as one undoable change; values beyond the
	 * last address are ignored. Returns an error message if a value is invalid (nothing is changed then), else "".
	 */
	fun setValues(target: String, start: Int, texts: Array<String>, format: String): String = ctx.guarded("") {
		val t = target(target)?.takeIf { it.editable } ?: return@guarded ""
		val a = t.addressable
		val count = minOf(texts.size.toLong(), a.maxAddress.toLong() - start + 1).toInt()
		val values = (0 until count).map { i ->
			parse(texts[i], a.dataWidth, format) ?: return@guarded invalidValue(texts[i], a.dataWidth, format)
		}
		applyValues(t, values.mapIndexed { i, v -> start + i to v })
		""
	}

	/** Sets all cells from [from] to [to] (inclusive) to [text]. Returns an error message or "". */
	fun fill(target: String, from: Int, to: Int, text: String, format: String): String = ctx.guarded("") {
		val t = target(target)?.takeIf { it.editable } ?: return@guarded ""
		val a = t.addressable
		val value = parse(text, a.dataWidth, format) ?: return@guarded invalidValue(text, a.dataWidth, format)
		if (from < 0 || to > a.maxAddress || from > to) return@guarded "The address range is invalid."
		if (to.toLong() - from >= MAX_FILL) return@guarded "At most $MAX_FILL cells can be filled at once."
		applyValues(t, (from..to).map { it to value })
		""
	}

	private fun applyValues(t: Target, values: List<Pair<Int, ULong>>) {
		val a = t.addressable
		if (!a.storesCells) {
			// RAM while simulating: changed directly, so the data output updates immediately
			values.forEach { (address, value) -> if (a.dataAt(address) != value) a.setDataAt(address, value, scheduler) }
			ctx.notifyChanged()
			return
		}
		val changes = values.mapNotNull { (address, value) ->
			a.dataAt(address).takeIf { it != value }?.let { AddressableCellChange(address, it, value) }
		}
		if (changes.isNotEmpty()) run(t, AddressableCellChangeCommand(t.view, t.link, changes))
	}

	/** Sets the comment of the cell at [address]; an empty [comment] removes it. */
	fun setComment(target: String, address: Int, comment: String) = ctx.guarded(Unit) {
		val t = target(target)?.takeIf { it.editable } ?: return@guarded
		val a = t.addressable
		val old = a.commentAt(address)
		val new = comment.trim().takeIf { it.isNotEmpty() }
		if (old == new || address !in 0..a.maxAddress) return@guarded
		run(t, AddressableCommentChangeCommand(t.view, t.link, listOf(AddressableCommentChange(address, old, new))))
	}

	/** Clears all cells and comments. */
	fun clear(target: String) = ctx.guarded(Unit) {
		val t = target(target)?.takeIf { it.editable } ?: return@guarded
		run(t, AddressableClearCommand(t.view, t.link))
	}

	/**
	 * Undoes ([redo] `false`) or redoes the last edit. Needed because the editor's undo action requires an
	 * active drawing view, which documents don't have.
	 */
	fun undo(target: String, redo: Boolean) = ctx.guarded(Unit) {
		target(target)?.takeIf { it.editable && it.addressable.storesCells } ?: return@guarded
		val commandManager = ctx.editor.commandManager
		if (redo && commandManager.canRedo()) commandManager.redo()
		else if (!redo && commandManager.canUndo()) commandManager.undo()
		ctx.notifyChanged()
	}

	/** ---- Import / export */

	/**
	 * Replaces the contents with a memory dump [text] in an Antares or Logisim format, read from file [fileName].
	 * Returns an error message if the dump is invalid, else "".
	 */
	fun importDump(target: String, text: String, fileName: String): String = ctx.guarded("") {
		val t = target(target)?.takeIf { it.editable } ?: return@guarded ""
		val command = try {
			WebMemoryContentsCommand(t.view, t.link, text.replace("\r\n", "\n"), fileName)
		} catch (e: Throwable) {
			return@guarded "${Translations.getString("antares.memory.invalidData.text")} ${e.message ?: ""}".trim()
		}
		run(t, command)
		""
	}

	/** Returns the contents as dump in [format]: `amd` (Antares, with comments), `raw` or `hex` (Logisim). */
	fun exportDump(target: String, format: String): String = ctx.guarded("") {
		val a = target(target)?.addressable ?: return@guarded ""
		when (format) {
			"raw" -> LogisimMemoryDump.writeRaw(a.memory, a.dataWidth)
			"hex" -> LogisimMemoryDump.writeAddressed(a.memory, a.dataWidth)
			else -> MemoryDump.write(a.memory, a.dataWidth)
		}
	}

	/** ---- Memory library items */

	/** Creates a memory item in the open project and opens it. Returns its UUID, or "" on failure. */
	fun create(name: String, addressWidth: Int, dataWidth: Int): String = ctx.guarded("") {
		val project = ctx.project ?: return@guarded ""
		require(name.isNotBlank()) { Translations.getString("edit.property.name.empty.error") }
		ctx.saveNow()
		val item = MemoryLibraryItem(MemoryStorable(name.trim(), BitWidth.of(addressWidth), BitWidth.of(dataWidth)))
		project.libraryService.addLibraryItem(project, item, project)
		ctx.openItem(item.uuid.id)
		ctx.notifyChanged()
		item.uuid.id
	}

	/** Sets the address width (in bits) of the open memory item as undoable change. */
	fun setAddressWidth(width: Int) = setWidth(true, width)

	/** Sets the data width (in bits) of the open memory item; fails if existing values don't fit. */
	fun setDataWidth(width: Int) = setWidth(false, width)

	private fun setWidth(isAddress: Boolean, width: Int) = ctx.guarded(Unit) {
		val t = documentTarget()?.takeIf { it.editable } ?: return@guarded
		val storable = t.addressable as MemoryStorable
		val bitWidth = BitWidth.of(width)
		if (!isAddress) storable.validateDataBitWidth(bitWidth)
		if ((if (isAddress) storable.addressWidth else storable.dataWidth) == bitWidth) return@guarded
		run(t, WebMemoryWidthCommand({ ctx.dataController().data!!.content as MemoryStorable }, isAddress, bitWidth))
	}

	/** Renames the open memory item, keeping the open (edited) copy and the stored item in sync. */
	fun setName(name: String) = ctx.guarded(Unit) {
		val t = documentTarget()?.takeIf { it.editable } ?: return@guarded
		require(name.isNotBlank()) { Translations.getString("edit.property.name.empty.error") }
		val item = ctx.document()?.payload as? MemoryLibraryItem ?: return@guarded
		val lib = item.library ?: return@guarded
		val newName = Name(TranslatableText(name.trim()))
		(t.addressable as MemoryStorable).name = newName
		item.name = newName
		lib.libraryService.updateLibraryItem(lib, item)
		ctx.notifyChanged()
	}

	private companion object {
		const val MAX_FILL = 1 shl 20

		fun radix(format: String) = when (format) {
			"dec" -> 10
			"oct" -> 8
			"bin" -> 2
			else -> 16
		}

		/** Renders [value] in [format], padding hexadecimal and binary values to the width of [bitWidth]. */
		fun render(value: ULong, bitWidth: BitWidth, format: String): String {
			val text = value.toString(radix(format)).uppercase()
			return when (format) {
				"hex" -> text.padStart((bitWidth.width + 3) / 4, '0')
				"bin" -> text.padStart(bitWidth.width, '0')
				else -> text
			}
		}

		/**
		 * Parses [text] in [format]; a `0x` prefix always selects hexadecimal. Returns `null` if the text is no
		 * number or the value exceeds [bitWidth].
		 */
		fun parse(text: String, bitWidth: BitWidth, format: String): ULong? {
			val value = text.trim().replace("_", "")
			val hexPrefix = value.startsWith("0x", ignoreCase = true)
			val digits = if (hexPrefix) value.drop(2) else value
			if (digits.isEmpty()) return null
			return digits.toULongOrNull(if (hexPrefix) 16 else radix(format))?.takeIf { it <= bitWidth.maxValue }
		}

		fun invalidValue(text: String, bitWidth: BitWidth, format: String): String {
			val name = when (format) {
				"dec" -> "decimal"
				"oct" -> "octal"
				"bin" -> "binary"
				else -> "hexadecimal"
			}
			return "“${text.trim()}” is not a valid $name value for ${bitWidth.width}-bit cells " +
				"(maximum ${render(bitWidth.maxValue, bitWidth, format)})."
		}
	}
}
