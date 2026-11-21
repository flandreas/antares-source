// The poster dialog: the circuits of the project packed onto one page, printable or downloadable as PNG
// (desktop: PosterViewerSwing with page size, orientation, resolution and borders).
import { useEffect, useMemo, useState } from 'react'
import { Download, Printer } from 'lucide-react'
import { engine } from '../../../core/engine'
import { DialogFrame } from '../../Dialogs'
import { useStoredState } from '../../hooks'
import { download } from '../../projectActions'

interface PosterOptions {
	project: string
	circuits: { uuid: string; name: string }[]
	sizes: string[]
	orientations: { id: string; label: string }[]
	resolutions: number[]
}

interface PosterSettings {
	size: string
	orientation: string
	borders: boolean
	dpi: number
}

const MM_PER_INCH = 25.4
/** Resolution of the image handed to the browser's print dialog. */
const PRINT_DPI = 200
/** Pixels of the longest page side in the preview, before the device pixel ratio. */
const PREVIEW_PIXELS = 900

/** Page dimensions in millimeter, as on the desktop (`PageSize`). */
const PAGE_MM: Record<string, [number, number]> = { A4: [210, 297], A3: [297, 420], A2: [420, 594] }

const render = (uuids: string[], s: PosterSettings, pixelsPerMm: number) =>
	engine.api.poster.render(uuids, s.size, s.orientation, s.borders, pixelsPerMm) ?? null

export function PosterDialog({ onClose }: { onClose: () => void }) {
	const options = useMemo(() => JSON.parse(engine.api.poster.options()) as PosterOptions | null, [])
	const [settings, setSettings] = useStoredState<PosterSettings>('antares.poster', {
		size: 'A4',
		orientation: 'landscape',
		borders: false,
		dpi: 300,
	})
	const [excluded, setExcluded] = useState<Set<string>>(new Set())
	const [preview, setPreview] = useState<string | null>(null)
	const change = (changes: Partial<PosterSettings>) => setSettings({ ...settings, ...changes })

	const circuits = options?.circuits ?? []
	const selected = circuits.filter((c) => !excluded.has(c.uuid)).map((c) => c.uuid)
	const selectedKey = selected.join(',')
	const [shortSide, longSide] = PAGE_MM[settings.size] ?? PAGE_MM.A4
	const landscape = settings.orientation === 'landscape'
	const [widthMm, heightMm] = landscape ? [longSide, shortSide] : [shortSide, longSide]

	// Render the preview after the dialog has painted, so that opening it never feels stuck
	useEffect(() => {
		const timer = window.setTimeout(() => {
			const ppm = (PREVIEW_PIXELS * (window.devicePixelRatio || 1)) / longSide
			setPreview(render(selectedKey ? selectedKey.split(',') : [], settings, ppm))
		}, 30)
		return () => window.clearTimeout(timer)
	}, [selectedKey, settings, longSide])

	if (!options) return null

	const fileName = `${options.project.replace(/[^\w\- ]+/g, '').trim() || 'project'} poster.png`
	const pixels = (mm: number) => Math.floor((mm / MM_PER_INCH) * settings.dpi)

	const savePng = () => {
		const url = render(selected, settings, settings.dpi / MM_PER_INCH)
		if (!url) return
		void fetch(url)
			.then((r) => r.blob())
			.then((blob) => download(fileName, blob, 'image/png'))
	}

	const print = () => {
		const url = render(selected, settings, PRINT_DPI / MM_PER_INCH)
		if (url) printImage(url, `${settings.size} ${settings.orientation}`, `Poster of “${options.project}”`)
	}

	const toggle = (uuid: string) => {
		const next = new Set(excluded)
		if (next.has(uuid)) next.delete(uuid)
		else next.add(uuid)
		setExcluded(next)
	}

	return (
		<DialogFrame title={`Poster of “${options.project}”`} onCancel={onClose} wide>
			<div className="poster-layout">
				<div className="poster-options">
					<div className="poster-group">
						<span className="poster-label">Page size</span>
						<div className="segmented small" role="radiogroup" aria-label="Page size">
							{options.sizes.map((size) => (
								<button
									key={size}
									type="button"
									role="radio"
									aria-checked={settings.size === size}
									className={settings.size === size ? 'selected' : ''}
									onClick={() => change({ size })}
								>
									{size}
								</button>
							))}
						</div>
					</div>
					<div className="poster-group">
						<span className="poster-label">Orientation</span>
						<div className="segmented small" role="radiogroup" aria-label="Orientation">
							{options.orientations.map((o) => (
								<button
									key={o.id}
									type="button"
									role="radio"
									aria-checked={settings.orientation === o.id}
									className={settings.orientation === o.id ? 'selected' : ''}
									onClick={() => change({ orientation: o.id })}
								>
									{o.label}
								</button>
							))}
						</div>
					</div>
					<label className="poster-switch">
						<span>Borders around circuits</span>
						<span className="switch">
							<input type="checkbox" checked={settings.borders} onChange={(e) => change({ borders: e.target.checked })} />
							<span className="switch-track" />
						</span>
					</label>
					<div className="poster-group poster-circuits-group">
						<div className="poster-circuits-head">
							<span className="poster-label">
								Circuits <span className="poster-count">{`${selected.length} of ${circuits.length}`}</span>
							</span>
							<button
								type="button"
								className="poster-link"
								onClick={() => setExcluded(selected.length === circuits.length ? new Set(circuits.map((c) => c.uuid)) : new Set())}
							>
								{selected.length === circuits.length ? 'None' : 'All'}
							</button>
						</div>
						<div className="poster-circuits">
							{circuits.map((c) => (
								<label key={c.uuid} className="poster-circuit">
									<input type="checkbox" checked={!excluded.has(c.uuid)} onChange={() => toggle(c.uuid)} />
									<span className="truncate">{c.name}</span>
								</label>
							))}
							{circuits.length === 0 && <div className="empty-small">This project has no circuits.</div>}
						</div>
					</div>
				</div>
				<div className="poster-preview">
					<div className="poster-page" style={{ aspectRatio: `${widthMm} / ${heightMm}` }}>
						{preview && <img src={preview} alt="Poster preview" />}
					</div>
					<span className="poster-caption">
						{settings.size} {landscape ? 'landscape' : 'portrait'} · {widthMm} × {heightMm} mm
					</span>
				</div>
			</div>
			<div className="dialog-actions poster-actions">
				<label className="poster-resolution">
					<span>PNG resolution</span>
					<select
						className="input select"
						value={settings.dpi}
						onChange={(e) => change({ dpi: Number(e.target.value) })}
						title={`${pixels(widthMm)} × ${pixels(heightMm)} pixels`}
					>
						{options.resolutions.map((dpi) => (
							<option key={dpi} value={dpi}>
								{dpi} dpi
							</option>
						))}
					</select>
				</label>
				<span className="poster-pixels">
					{pixels(widthMm)} × {pixels(heightMm)} px
				</span>
				<span className="poster-spacer" />
				<button type="button" className="btn" onClick={savePng} disabled={selected.length === 0}>
					<Download size={14} /> Download PNG
				</button>
				<button type="button" className="btn btn-primary" onClick={print} disabled={selected.length === 0}>
					<Printer size={14} /> Print…
				</button>
			</div>
		</DialogFrame>
	)
}

/** Prints [url] on one page of [pageSize] (CSS `@page` size, e.g. "A4 landscape") through a hidden frame. */
function printImage(url: string, pageSize: string, title: string) {
	const frame = document.createElement('iframe')
	frame.setAttribute('aria-hidden', 'true')
	frame.className = 'poster-print-frame'
	document.body.appendChild(frame)
	const doc = frame.contentDocument
	const win = frame.contentWindow
	if (!doc || !win) return frame.remove()
	doc.title = title
	const style = doc.createElement('style')
	style.textContent = `@page { size: ${pageSize}; margin: 0 }
html, body { margin: 0; height: 100% }
img { display: block; width: 100%; height: 100%; object-fit: contain }`
	doc.head.appendChild(style)
	const img = doc.createElement('img')
	img.onload = () => {
		win.addEventListener('afterprint', () => window.setTimeout(() => frame.remove(), 0))
		win.focus()
		win.print()
	}
	img.src = url
	doc.body.appendChild(img)
}
