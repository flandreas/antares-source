import { useState } from 'react'
import { strToU8, zipSync } from 'fflate'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import { useStoredState } from '../../hooks'
import { download } from '../../projectActions'
import { hdlApi, type HdlExportInfo, type HdlFile, type HdlLanguage } from './api'

/** Shows the export dialog for the open circuit (desktop: `ExportHDLPanel`). */
export async function showHdlExport(language: HdlLanguage) {
	const info = hdlApi.exportInfo(language)
	if (!info) {
		engine.message('info', 'HDL export', 'Open a digital circuit to export it.')
		return
	}
	await dialogs.custom<void>((done) => <ExportHdlDialog language={language} info={info} onClose={() => done(null)} />)
}

/** Downloads a single file directly, or a test bench and its circuit together as zip archive. */
function downloadFiles(files: HdlFile[], baseName: string) {
	if (files.length === 1) {
		download(files[0].name, files[0].content, 'text/plain')
		return
	}
	const entries = Object.fromEntries(files.map((f) => [f.name, strToU8(f.content)]))
	download(`${baseName}.zip`, zipSync(entries, { level: 6 }), 'application/zip')
}

function ExportHdlDialog({ language, info, onClose }: { language: HdlLanguage; info: HdlExportInfo; onClose: () => void }) {
	const [baseName, setBaseName] = useState(info.baseName)
	// Like the desktop, the first test case is preselected
	const [testcaseId, setTestcaseId] = useState(info.testcases[0]?.id ?? -1)
	const [waitTime, setWaitTime] = useState(String(info.waitTime))
	const [useDelayModel, setUseDelayModel] = useStoredState(`antares.hdl.${language}.useDelayModel`, true)
	const [error, setError] = useState<string | null>(null)

	const withTestBench = testcaseId >= 0
	const fileNames = `${baseName || '…'}${info.fileExtension}${withTestBench ? ' + test bench' : ''}`

	const submit = () => {
		const result = hdlApi.export(language, { baseName, testcaseId, waitTime: Number(waitTime), useDelayModel })
		if (!result) return
		if ('error' in result) {
			setError(result.error)
			return
		}
		downloadFiles(result.files, baseName)
		engine.message('success', info.title, `Exported ${result.files.map((f) => f.name).join(' and ')}.`)
		onClose()
	}

	return (
		<DialogFrame title={`${info.title}`} onCancel={onClose}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					submit()
				}}
			>
				<div className="dialog-body hdl-form">
					<label className="field">
						<span className="field-label">{info.labels.baseName}</span>
						<div className="hdl-name">
							<input
								className="input"
								value={baseName}
								autoFocus
								spellCheck={false}
								onChange={(e) => {
									setBaseName(e.target.value)
									setError(null)
								}}
							/>
							<span className="hdl-ext">{info.fileExtension}</span>
						</div>
					</label>
					<label className="field">
						<span className="field-label">{info.labels.testcase}</span>
						<select
							className="input select"
							value={testcaseId}
							disabled={info.testcases.length === 0}
							onChange={(e) => {
								setTestcaseId(Number(e.target.value))
								setError(null)
							}}
						>
							<option value={-1}>None</option>
							{info.testcases.map((t) => (
								<option key={t.id} value={t.id}>
									{t.name}
								</option>
							))}
						</select>
					</label>
					<label className="field">
						<span className="field-label">{info.labels.waitTime}</span>
						<input
							className="input hdl-number"
							type="number"
							min={info.minWaitTime}
							step={1}
							value={waitTime}
							disabled={!withTestBench}
							onChange={(e) => {
								setWaitTime(e.target.value)
								setError(null)
							}}
						/>
					</label>
					<label className="hdl-switch-row">
						<span>{info.labels.delayModel}</span>
						<span className="switch">
							<input type="checkbox" checked={useDelayModel} onChange={(e) => setUseDelayModel(e.target.checked)} />
							<span className="switch-track" />
						</span>
					</label>
					<div className="hdl-files">Creates {fileNames}</div>
					{error && <div className="field-error hdl-error">{error}</div>}
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={onClose}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary">
						Export
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}
