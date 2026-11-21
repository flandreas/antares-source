// Typed access to the Kotlin `WebMemory` API (memory items and RAM/ROM/lookup table contents).
import { engine } from '../../../core/engine'

/** Which memory a call refers to: the open memory item, or the component shown in the contents dialog. */
export type MemoryTarget = 'document' | 'dialog'

/** Number formats of cell values (desktop: `AddressableValueConverter`). */
export type ValueFormat = 'hex' | 'dec' | 'oct' | 'bin'

export type DumpFormat = 'amd' | 'raw' | 'hex'

export interface MemoryInfo {
	title: string
	type: 'memory' | 'rom' | 'ram' | 'lookupTable'
	addressWidth: number
	dataWidth: number
	maxAddress: number
	editable: boolean
	simulating: boolean
	/** Whether the memory has a disassembly column (ROMs with a disassembler configuration) */
	disassembly: boolean
	/** The cell accessed by the simulation, or -1 */
	currentAddress: number
	dataSource: string | null
	/** The memory item a ROM is loaded from at simulation start */
	linkedMemory: { uuid: string; name: string } | null
}

export interface MemoryCells {
	values: string[]
	/** Empty strings for cells without comment */
	comments: string[]
	disassembly: string[] | null
}

const api = () => engine.api.memory

export const memoryApi = {
	info(target: MemoryTarget): MemoryInfo | null {
		return JSON.parse(api().info(target)) as MemoryInfo | null
	},
	cells(target: MemoryTarget, start: number, count: number, format: ValueFormat): MemoryCells | null {
		return JSON.parse(api().cells(target, start, count, format)) as MemoryCells | null
	},
	/** Returns an error message, or '' on success. */
	setValues(target: MemoryTarget, start: number, texts: string[], format: ValueFormat): string {
		return api().setValues(target, start, texts, format)
	},
	fill(target: MemoryTarget, from: number, to: number, text: string, format: ValueFormat): string {
		return api().fill(target, from, to, text, format)
	},
	setComment(target: MemoryTarget, address: number, comment: string) {
		api().setComment(target, address, comment)
	},
	undo(target: MemoryTarget, redo = false) {
		api().undo(target, redo)
	},
	clear(target: MemoryTarget) {
		api().clear(target)
	},
	importDump(target: MemoryTarget, text: string, fileName: string): string {
		return api().importDump(target, text, fileName)
	},
	exportDump(target: MemoryTarget, format: DumpFormat): string {
		return api().exportDump(target, format)
	},
	create(name: string, addressWidth: number, dataWidth: number): string {
		const uuid = api().create(name, addressWidth, dataWidth)
		engine.refresh()
		return uuid
	},
	setAddressWidth(width: number) {
		api().setAddressWidth(width)
	},
	setDataWidth(width: number) {
		api().setDataWidth(width)
	},
	setName(name: string) {
		api().setName(name)
		engine.refresh()
	},
}

const radix: Record<ValueFormat, number> = { hex: 16, dec: 10, oct: 8, bin: 2 }

/** Number of characters of the widest value of [bits] bits in [format]. */
export function valueChars(bits: number, format: ValueFormat): number {
	return ((1n << BigInt(bits)) - 1n).toString(radix[format]).length
}

/** Renders an address in [format], padded to the width of the address space (like the desktop's row header). */
export function formatAddress(address: number, addressWidth: number, format: ValueFormat): string {
	const text = address.toString(radix[format]).toUpperCase()
	return format === 'hex' || format === 'bin' ? text.padStart(valueChars(addressWidth, format), '0') : text
}

/**
 * Parses an address typed by the user in [format] (desktop: `MemoryPage.parseAddress`). A `0x` prefix always
 * selects hexadecimal; `0b` and `0d` select binary/decimal except in hexadecimal mode. Returns null if invalid.
 */
export function parseAddress(text: string, format: ValueFormat, maxAddress: number): number | null {
	const value = text.trim().toLowerCase()
	let base = radix[format]
	let digits = value
	if (value.startsWith('0x')) {
		base = 16
		digits = value.slice(2)
	} else if (format !== 'hex' && (value.startsWith('0b') || value.startsWith('0d'))) {
		base = value[1] === 'b' ? 2 : 10
		digits = value.slice(2)
	}
	if (!digits || ![...digits].every((c) => Number.isInteger(parseInt(c, base)))) return null
	const address = parseInt(digits, base)
	return address >= 0 && address <= maxAddress ? address : null
}

export const formatLabels: Record<ValueFormat, string> = { hex: 'Hex', dec: 'Dec', oct: 'Oct', bin: 'Bin' }
