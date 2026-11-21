// Images of a project (desktop: ImportImageAction, ImageGraphDesktopItemSwing). Images are imported from the
// project panel, placed into circuits by dragging their project item onto the canvas, and used by image switches.
import { useEffect, useMemo, useState } from 'react'
import { Download, ImagePlus, Pencil, Replace } from 'lucide-react'
import { engine } from '../../../core/engine'
import { dataUrlMimeType, dataUrlToBytes, IMAGE_TYPES, imageMimeType } from '../../../core/dataUrl'
import { askNewItemName, download, renameItem } from '../../projectActions'
import { useEngineVersion } from '../../hooks'
import { documentEditors, documentInspectors, newItemActions, register } from '../registry'
import './images.css'

interface ImageInfo {
	uuid: string
	name: string
	/** File extension: png, jpg, gif or svg */
	type: string
	/** The image as `data:` URL */
	url: string
}

/** Images are stored in the browser; very large files would slow down saving and exporting. */
const MAX_SIZE = 8 * 1024 * 1024

const ACCEPT = [...new Set(Object.values(IMAGE_TYPES)), ...Object.keys(IMAGE_TYPES).map((e) => `.${e}`)].join(',')

const describe = (uuid: string) => JSON.parse(engine.api.images.describe(uuid)) as ImageInfo | null

/** Lets the user pick an image file and returns it as `data:` URL, or null if cancelled or invalid. */
function pickImage(): Promise<{ name: string; url: string } | null> {
	return new Promise((resolve) => {
		const input = document.createElement('input')
		input.type = 'file'
		input.accept = ACCEPT
		input.onchange = () => {
			const file = input.files?.[0]
			if (!file) return resolve(null)
			const mime = imageMimeType(file.name) ?? (Object.values(IMAGE_TYPES).includes(file.type) ? file.type : undefined)
			if (!mime) {
				engine.message('error', 'Unsupported image', 'Choose a PNG, JPEG, GIF or SVG file.')
				return resolve(null)
			}
			if (file.size > MAX_SIZE) {
				engine.message('error', 'Image too large', `Images can have at most ${MAX_SIZE / 1024 / 1024} MB.`)
				return resolve(null)
			}
			const reader = new FileReader()
			reader.onload = () => {
				// Normalize the MIME type (e.g. some systems report SVGs as text/xml)
				const url = String(reader.result).replace(/^data:[^;,]*/, `data:${mime}`)
				resolve({ name: file.name.replace(/\.[^.]+$/, ''), url })
			}
			reader.onerror = () => resolve(null)
			reader.readAsDataURL(file)
		}
		input.click()
	})
}

async function importImage() {
	const picked = await pickImage()
	if (!picked) return
	const name = await askNewItemName('Import image', 'image', picked.name || 'Image', 'Import')
	if (!name) return
	const uuid = engine.api.images.importImage(name, picked.url)
	engine.refresh()
	if (uuid) engine.message('success', '', `Imported “${name}”. Drag it from the project panel onto a circuit to show it there.`)
}

async function replaceImage(uuid: string) {
	const picked = await pickImage()
	if (!picked) return
	engine.api.images.replaceImage(uuid, picked.url)
	engine.refresh()
}

async function downloadImage(info: ImageInfo) {
	const safe = info.name.replace(/[^\w\- ]+/g, '').trim() || 'image'
	download(`${safe}.${info.type}`, dataUrlToBytes(info.url), dataUrlMimeType(info.url))
}

function useImage(): ImageInfo | null {
	const version = useEngineVersion()
	const uuid = engine.state.document?.kind === 'image' ? engine.state.document.uuid : null
	return useMemo(() => (uuid ? describe(uuid) : null), [uuid, version])
}

type Size = { width: number; height: number }

/**
 * The size of an SVG like the editor determines it (Kotlin `ImageSizes`): `width`/`height` of the root element,
 * else its `viewBox`. Browsers give SVGs without intrinsic size an arbitrary default size instead.
 */
function svgSize(url: string): Size | null {
	try {
		const svg = new TextDecoder().decode(dataUrlToBytes(url))
		const root = new DOMParser().parseFromString(svg, 'image/svg+xml').documentElement
		const length = (name: string) => {
			const m = /^([0-9.]+)(px)?$/.exec(root.getAttribute(name)?.trim() ?? '')
			return m ? Number(m[1]) : null
		}
		const box = (root.getAttribute('viewBox') ?? '').trim().split(/[\s,]+/).map(Number)
		const vb = box.length === 4 && box[2] > 0 && box[3] > 0 ? { width: box[2], height: box[3] } : null
		const w = length('width')
		const h = length('height')
		if (w && h) return { width: w, height: h }
		if (vb && w) return { width: w, height: (w * vb.height) / vb.width }
		if (vb && h) return { width: (h * vb.width) / vb.height, height: h }
		return vb ?? { width: 100, height: 100 }
	} catch {
		return null
	}
}

/** The pixel size of an image as used in circuits, once known. */
function useImageSize(url: string | undefined): Size | null {
	const [size, setSize] = useState<Size | null>(null)
	useEffect(() => {
		setSize(null)
		if (!url) return
		if (dataUrlMimeType(url) === 'image/svg+xml') return setSize(svgSize(url))
		const img = new Image()
		img.onload = () => setSize({ width: img.naturalWidth, height: img.naturalHeight })
		img.src = url
	}, [url])
	return size
}

/** The decoded size of a `data:` URL, without decoding it. */
function byteSize(url: string): number {
	const payload = url.slice(url.indexOf(',') + 1)
	if (!url.slice(0, url.indexOf(',')).endsWith(';base64')) return new TextEncoder().encode(decodeURIComponent(payload)).length
	return Math.floor((payload.length * 3) / 4) - (payload.endsWith('==') ? 2 : payload.endsWith('=') ? 1 : 0)
}

const typeLabel = (type: string) => (type === 'jpg' ? 'JPEG' : type.toUpperCase())

/** The open image, shown in the main area. */
function ImageDocument() {
	const info = useImage()
	const size = useImageSize(info?.url)
	const editable = engine.state.document?.editable ?? false
	if (!info) return null
	return (
		<div className="image-doc">
			<div className="image-toolbar">
				<span className="image-title truncate">{info.name}</span>
				<span className="image-spacer" />
				<button type="button" className="btn btn-sm" disabled={!editable} onClick={() => void replaceImage(info.uuid)}>
					<Replace size={14} /> Replace…
				</button>
				<button type="button" className="btn btn-sm" onClick={() => void downloadImage(info)}>
					<Download size={14} /> Download
				</button>
			</div>
			<div className="image-stage">
				{size && <img className="image-preview" src={info.url} alt={info.name} width={size.width} height={size.height} draggable={false} />}
			</div>
		</div>
	)
}

/** Name, type and size of the open image (desktop: ImageIdentification properties). */
function ImageInspector() {
	const info = useImage()
	const size = useImageSize(info?.url)
	const editable = engine.state.document?.editable ?? false
	if (!info) return null
	const bytes = byteSize(info.url)
	return (
		<>
			<section className="prop-group">
				<h3>Image</h3>
				<div className="prop-row">
					<span className="prop-label">Name</span>
					<div className="prop-control image-name-row">
						<span className="prop-static truncate">{info.name}</span>
						<button
							type="button"
							className="icon-btn small"
							title="Rename"
							disabled={!editable}
							onClick={() => void renameItem({ uuid: info.uuid, name: info.name, kind: 'image' })}
						>
							<Pencil size={13} />
						</button>
					</div>
				</div>
				<div className="prop-row">
					<span className="prop-label">Format</span>
					<span className="prop-static">{typeLabel(info.type)}</span>
				</div>
				<div className="prop-row">
					<span className="prop-label">Size</span>
					<span className="prop-static">{size ? `${Math.round(size.width)} × ${Math.round(size.height)} px` : '–'}</span>
				</div>
				<div className="prop-row">
					<span className="prop-label">File size</span>
					<span className="prop-static">{bytes < 1024 ? `${bytes} bytes` : `${(bytes / 1024).toFixed(bytes < 10240 ? 1 : 0)} KB`}</span>
				</div>
			</section>
			<section className="prop-group">
				<div className="hint subtle">
					To show this image in a circuit, drag it from the project panel onto the circuit. Image switches can use it as
					their on or off picture.
				</div>
			</section>
		</>
	)
}

register(newItemActions, (a) => a.label, { label: 'Import image…', icon: <ImagePlus size={15} />, run: importImage })
documentEditors.image = ImageDocument
documentInspectors.image = ImageInspector
