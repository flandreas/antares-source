// Exports of the open circuit: images (SVG/PNG/JPEG), oscilloscope waveforms (VCD) and single circuits as
// bundles compatible with Antares Desktop (desktop: Export menu, ExportMetaGraphAction, ImportMetaGraphAction).
import { strFromU8, strToU8, unzipSync, zipSync } from 'fflate'
import { Activity, FileDown, FileUp } from 'lucide-react'
import { engine } from '../../../core/engine'
import { dialogs } from '../../Dialogs'
import { download } from '../../projectActions'
import { projectMenuActions, register } from '../registry'
import { exportApi } from './api'
import { safeFileName } from './ImageExportDialog'
import './export.css'

/** Desktop bundles are ZIP files with one XML file (`ExportMetaGraphAction.EXPORT_FILE_EXTENSION`). */
const BUNDLE_EXTENSION = 'zip'

/** Downloads the oscilloscope's recorded digital waveforms (desktop: `ExportVcdAction`). */
function exportVcd() {
	const result = exportApi.vcd()
	if (!result) return
	if ('error' in result) {
		engine.message('info', 'Export waveforms', result.error)
		return
	}
	const fileName = `${safeFileName(engine.state.circuit?.name ?? '', 'waveforms')}.vcd`
	download(fileName, result.vcd, 'text/plain')
	engine.message('success', '', `Exported ${fileName}`)
}

/** Downloads the open circuit with its sub-circuits as bundle that Antares Desktop can import. */
function exportCircuit() {
	const circuit = engine.state.circuit
	if (!circuit) return
	const result = exportApi.circuitBundle(circuit.uuid)
	if (!result) return
	if ('error' in result) {
		engine.message('error', 'Export circuit', result.error)
		return
	}
	const fileName = safeFileName(result.name, 'circuit')
	download(`${fileName}.${BUNDLE_EXTENSION}`, zipSync({ [`${fileName}.cir`]: strToU8(result.xml) }, { level: 6 }), 'application/zip')
	engine.message('success', '', `Exported “${result.name}”`)
}

/** Lets the user pick a circuit bundle and imports it into a new folder of the open project. */
function importCircuit(): Promise<void> {
	return new Promise((resolve) => {
		const input = document.createElement('input')
		input.type = 'file'
		input.accept = `.${BUNDLE_EXTENSION}`
		input.onchange = async () => {
			const file = input.files?.[0]
			if (file) await importCircuitFile(file)
			resolve()
		}
		input.click()
	})
}

async function importCircuitFile(file: File) {
	const name = file.name.replace(/\.[^.]*$/, '')
	let xml: string
	try {
		const entries = Object.values(unzipSync(new Uint8Array(await file.arrayBuffer())))
		if (entries.length !== 1) throw new Error('Expected one file')
		xml = strFromU8(entries[0])
	} catch {
		engine.message('error', 'Import circuit', `The file “${file.name}” is not an exported Antares circuit.`)
		return
	}
	let result = exportApi.importCircuitBundle(xml, name, false)
	if (result?.result === 'conflict') {
		const replace = await dialogs.confirm({
			title: 'Replace circuits?',
			message: <div className="export-message">{result.message}</div>,
			confirmLabel: 'Replace',
			danger: true,
		})
		if (!replace) return
		result = exportApi.importCircuitBundle(xml, name, true)
	}
	if (!result) return
	if (result.result === 'success') engine.message('success', '', `Imported “${name}” into the folder “Import '${name}'”`)
	else engine.message('error', 'Import circuit', result.message)
}

register(
	projectMenuActions,
	(a) => a.label,
	{
		label: 'Export waveforms (VCD)',
		icon: <Activity size={15} />,
		enabled: (state) => state.circuit?.type === 'Digital' && !state.document,
		run: exportVcd,
	},
	{
		label: 'Export circuit (.zip)',
		icon: <FileDown size={15} />,
		enabled: (state) => !!state.circuit && !state.document,
		run: exportCircuit,
	},
	{
		label: 'Import circuit (.zip)…',
		icon: <FileUp size={15} />,
		enabled: (state) => !!state.project && state.mode !== 'simulate',
		run: importCircuit,
	},
)
