// Converts between exported library JSON (Kotlin `WebLibraries.exportLibrary`) and the desktop's `.acl` library
// files, which like `.acp` project files are zip archives of the library directory: `<uuid>/library.xml`,
// `<uuid>/<circuit>.cir` and images. The editor keeps images as `data:` URLs.
import { strFromU8, strToU8, unzipSync, zipSync } from 'fflate'
import { bytesToDataUrl, dataUrlToBytes, imageMimeType } from '../../../core/dataUrl'

interface ExportedLibrary {
	format: string
	uuid: string
	name: string
	files: Record<string, string>
}

export const LIBRARY_EXPORT_FORMAT = 'antares-library'

export function libraryToAcl(json: string): Uint8Array {
	const library = JSON.parse(json) as ExportedLibrary
	const entries: Record<string, Uint8Array> = {}
	for (const [name, content] of Object.entries(library.files)) {
		const image = imageMimeType(name) && content.startsWith('data:')
		entries[`${library.uuid}/${name}`] = image ? dataUrlToBytes(content) : strToU8(content)
	}
	return zipSync(entries, { level: 6 })
}

/** Reads a `.acl` file and returns the library in the editor's import JSON format plus its name. */
export function aclToLibrary(data: Uint8Array): { json: string; name: string } {
	const entries = unzipSync(data)
	const libraryPath = Object.keys(entries).find((p) => p === 'library.xml' || p.endsWith('/library.xml'))
	if (!libraryPath) throw new Error('No library.xml in library file')
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
	if (root.tagName !== 'library') throw new Error('Not an Antares library file')
	const uuid = root.getAttribute('uuid') ?? ''
	const name =
		root.querySelector(":scope > folder > libraryFolder > name > translation[lang='en']")?.getAttribute('text') ??
		root.querySelector(':scope > folder > libraryFolder > name > translation')?.getAttribute('text') ??
		'Imported library'
	return { json: JSON.stringify({ format: LIBRARY_EXPORT_FORMAT, version: 1, uuid, name, files }), name }
}
