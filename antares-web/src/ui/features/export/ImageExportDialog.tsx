import { useMemo, useState } from 'react'
import { engine } from '../../../core/engine'
import { DialogFrame, dialogs } from '../../Dialogs'
import { useStoredState } from '../../hooks'
import { download } from '../../projectActions'
import { exportApi } from './api'

type ImageFormat = 'png' | 'jpeg' | 'svg'

const formats: Array<{ id: ImageFormat; label: string; mime: string }> = [
	{ id: 'png', label: 'PNG', mime: 'image/png' },
	{ id: 'jpeg', label: 'JPEG', mime: 'image/jpeg' },
	{ id: 'svg', label: 'SVG', mime: 'image/svg+xml' },
]

const scales = [0.5, 1, 2, 3, 4]

/** Browsers fail to create larger canvases. */
const MAX_PIXELS = 16384

/** A file name for [name] without characters that file systems don't accept. */
export const safeFileName = (name: string, fallback: string) => name.replace(/[\\/:*?"<>|]+/g, '').trim() || fallback

/**
 * Exports the shown circuit or symbol as image (desktop: `ExportSvgAction`, `ExportRasterImageAction`),
 * asking for format, size, theme and background.
 */
export async function showImageExport() {
	const size = engine.state.circuit ? exportApi.drawingSize() : null
	if (!size) {
		engine.message('info', 'Export image', 'There is nothing to export. Add some components first.')
		return
	}
	await dialogs.custom<null>((done) => <ImageExportDialog size={size} onClose={() => done(null)} />)
}

function ImageExportDialog({ size, onClose }: { size: { width: number; height: number }; onClose: () => void }) {
	const themes = useMemo(() => exportApi.themes(), [])
	const [format, setFormat] = useStoredState<ImageFormat>('antares.export.format', 'png')
	const [scale, setScale] = useStoredState('antares.export.scale', 2)
	const [storedTheme, setTheme] = useStoredState('antares.export.theme', '')
	const [transparentSetting, setTransparent] = useStoredState('antares.export.transparent', false)
	const circuitName = engine.state.view === 'symbol' ? `${engine.state.circuit?.name} symbol` : (engine.state.circuit?.name ?? 'circuit')
	const [name, setName] = useState(safeFileName(circuitName, 'circuit'))
	const [error, setError] = useState<string | null>(null)

	// A stored theme may no longer exist; '' means the editor's current theme
	const theme = themes.some((t) => t.id === storedTheme) ? storedTheme : ''
	const transparent = transparentSetting && format !== 'jpeg'
	const raster = format !== 'svg'
	const width = Math.round(size.width * scale)
	const height = Math.round(size.height * scale)
	const tooLarge = raster && (width > MAX_PIXELS || height > MAX_PIXELS)
	const extension = format === 'jpeg' ? 'jpg' : format

	// The preview is the SVG export, which looks like the raster images
	const preview = useMemo(() => {
		const svg = exportApi.svg(theme, transparent)
		return svg ? `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}` : ''
	}, [theme, transparent])

	const submit = async () => {
		const fileName = `${safeFileName(name, 'circuit')}.${extension}`
		if (format === 'svg') {
			const svg = exportApi.svg(theme, transparent)
			if (!svg) return
			download(fileName, svg, 'image/svg+xml')
		} else {
			if (tooLarge) return
			const mime = formats.find((f) => f.id === format)!.mime
			const url = exportApi.rasterImage(width, height, scale, theme, transparent, mime)
			if (!url) {
				setError('The image could not be created.')
				return
			}
			download(fileName, await (await fetch(url)).blob(), mime)
		}
		engine.message('success', '', `Exported ${fileName}`)
		onClose()
	}

	return (
		<DialogFrame title="Export image" onCancel={onClose}>
			<form
				onSubmit={(e) => {
					e.preventDefault()
					void submit()
				}}
			>
				<div className="dialog-body export-form">
					<div className={`export-preview${transparent ? ' transparent' : ''}`}>
						{preview && <img src={preview} alt="Preview of the exported image" />}
					</div>
					<div className="export-row">
						<span className="field-label">Format</span>
						<div className="segmented small" role="radiogroup" aria-label="Format">
							{formats.map((f) => (
								<button
									key={f.id}
									type="button"
									role="radio"
									aria-checked={format === f.id}
									className={format === f.id ? 'selected' : ''}
									onClick={() => setFormat(f.id)}
								>
									{f.label}
								</button>
							))}
						</div>
					</div>
					<label className="export-row">
						<span className="field-label">Size</span>
						<span className="export-size">
							<select className="input select" value={scale} disabled={!raster} onChange={(e) => setScale(Number(e.target.value))}>
								{scales.map((s) => (
									<option key={s} value={s}>
										{s * 100}%
									</option>
								))}
							</select>
							<span className={`export-dims${tooLarge ? ' error' : ''}`}>
								{raster ? `${width.toLocaleString()} × ${height.toLocaleString()} px` : 'Vector, scales freely'}
							</span>
						</span>
					</label>
					<label className="export-row">
						<span className="field-label">Colors</span>
						<select className="input select" value={theme} onChange={(e) => setTheme(e.target.value)}>
							<option value="">Current theme ({themes[0]?.id})</option>
							{themes.slice(1).map((t) => (
								<option key={t.id} value={t.id}>
									{t.id}
									{t.dark ? ' (dark)' : ''}
								</option>
							))}
						</select>
					</label>
					<label className={`export-row export-switch${format === 'jpeg' ? ' disabled' : ''}`}>
						<span className="field-label">Transparent background</span>
						<span className="switch">
							<input
								type="checkbox"
								checked={transparent}
								disabled={format === 'jpeg'}
								onChange={(e) => setTransparent(e.target.checked)}
							/>
							<span className="switch-track" />
						</span>
					</label>
					<label className="export-row">
						<span className="field-label">File name</span>
						<span className="export-name">
							<input
								className="input"
								value={name}
								spellCheck={false}
								onChange={(e) => {
									setName(e.target.value)
									setError(null)
								}}
							/>
							<span className="export-ext">.{extension}</span>
						</span>
					</label>
					{tooLarge && <div className="field-error">The image is too large. Choose a smaller size.</div>}
					{error && <div className="field-error">{error}</div>}
				</div>
				<div className="dialog-actions">
					<button type="button" className="btn" onClick={onClose}>
						Cancel
					</button>
					<button type="submit" className="btn btn-primary" autoFocus disabled={tooLarge}>
						Export
					</button>
				</div>
			</form>
		</DialogFrame>
	)
}
