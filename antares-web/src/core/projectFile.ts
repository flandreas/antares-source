// Converts between exported project JSON (from the Kotlin editor) and the desktop's `.acp` project files,
// which are zip archives of the project directory: `<uuid>/library.xml`, `<uuid>/<circuit>.cir` and
// images as `<uuid>/<image>.<png|jpg|gif|svg>`. The editor keeps images as `data:` URLs.
import { strFromU8, strToU8, unzipSync, zipSync } from 'fflate'
import { bytesToDataUrl, dataUrlToBytes, imageMimeType } from './dataUrl'

interface ExportedProject {
	format: string
	version: number
	uuid: string
	name: string
	files: Record<string, string>
}

export function projectToAcp(json: string): Uint8Array {
	const project = JSON.parse(json) as ExportedProject
	const entries: Record<string, Uint8Array> = {}
	for (const [name, content] of Object.entries(project.files)) {
		const image = imageMimeType(name) && content.startsWith('data:')
		entries[`${project.uuid}/${name}`] = image ? dataUrlToBytes(content) : strToU8(content)
	}
	return zipSync(entries, { level: 6 })
}

/**
 * Limits for unpacking `.acp` files, so that a malicious archive (a "zip bomb") can't exhaust memory.
 * fflate allocates each file's output with its declared size and doesn't grow it, so checking the declared sizes is
 * enough. The largest sample project is about 1 MB unpacked.
 */
const MAX_ARCHIVE_SIZE = 50 * 1024 * 1024
const MAX_UNPACKED_SIZE = 200 * 1024 * 1024
const MAX_FILES = 10_000

/** Unpacks [data], failing if it exceeds the limits above. */
function unzipBounded(data: Uint8Array): Record<string, Uint8Array> {
	if (data.length > MAX_ARCHIVE_SIZE) throw new Error('The project file is too large')
	let files = 0
	let size = 0
	return unzipSync(data, {
		filter: (file) => {
			size += file.originalSize
			if (++files > MAX_FILES || size > MAX_UNPACKED_SIZE) throw new Error('The project file is too large when unpacked')
			return true
		},
	})
}

/** Reads a `.acp` file and returns the project in the editor's import JSON format plus its name. */
export function acpToProject(data: Uint8Array): { json: string; name: string; uuid: string } {
	const entries = unzipBounded(data)
	const libraryPath = Object.keys(entries).find((p) => p === 'library.xml' || p.endsWith('/library.xml'))
	if (!libraryPath) throw new Error('No library.xml in project file')
	const dir = libraryPath.slice(0, libraryPath.length - 'library.xml'.length)
	const files: Record<string, string> = {}
	for (const [path, bytes] of Object.entries(entries)) {
		if (path.startsWith(dir) && !path.endsWith('/') && !path.slice(dir.length).includes('/')) {
			const name = path.slice(dir.length)
			const mime = imageMimeType(name)
			files[name] = mime ? bytesToDataUrl(bytes, mime) : strFromU8(bytes)
		}
	}
	const root = new DOMParser().parseFromString(files['library.xml'], 'text/xml').documentElement
	const uuid = root.getAttribute('uuid') ?? ''
	const name =
		root.querySelector(":scope > folder > libraryFolder > name > translation[lang='en']")?.getAttribute('text') ??
		root.querySelector(':scope > folder > libraryFolder > name > translation')?.getAttribute('text') ??
		'Imported project'
	if (!uuid || root.tagName !== 'project') throw new Error('Not an Antares project file')
	return { json: JSON.stringify({ format: 'antares-project', version: 1, uuid, name, files }), name, uuid }
}
