// Conversions between binary image files and the `data:` URLs that the web editor stores images as.

/** MIME types of the image file extensions supported by Antares (desktop: `ImageType`). */
export const IMAGE_TYPES: Record<string, string> = {
	png: 'image/png',
	jpg: 'image/jpeg',
	jpeg: 'image/jpeg',
	gif: 'image/gif',
	svg: 'image/svg+xml',
}

/** The MIME type of an image file name, or undefined if it isn't a supported image. */
export function imageMimeType(name: string): string | undefined {
	return IMAGE_TYPES[name.slice(name.lastIndexOf('.') + 1).toLowerCase()]
}

export function bytesToDataUrl(bytes: Uint8Array, mime: string): string {
	let binary = ''
	const chunk = 0x8000
	for (let i = 0; i < bytes.length; i += chunk) binary += String.fromCharCode(...bytes.subarray(i, i + chunk))
	return `data:${mime};base64,${btoa(binary)}`
}

export function dataUrlToBytes(url: string): Uint8Array {
	const comma = url.indexOf(',')
	const header = url.slice(0, comma)
	const payload = url.slice(comma + 1)
	if (!header.endsWith(';base64')) return new TextEncoder().encode(decodeURIComponent(payload))
	const binary = atob(payload)
	const bytes = new Uint8Array(binary.length)
	for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
	return bytes
}

/** The MIME type of a `data:` URL. */
export function dataUrlMimeType(url: string): string {
	return url.slice(5, url.indexOf(',')).split(';')[0]
}
