// Typed access to the Kotlin `WebExport` API (images, waveforms and circuit bundles).
import { engine } from '../../../core/engine'

export interface ThemeInfo {
	id: string
	dark: boolean
}

export type ImportResult = { result: 'success' | 'conflict' | 'error'; message: string }

const api = () => engine.api.exporter

export const exportApi = {
	/** The editor's themes, the current one first. */
	themes(): ThemeInfo[] {
		return JSON.parse(api().themes()) as ThemeInfo[]
	},

	/** Size of the drawing including a margin, in drawing units; null if it is empty. */
	drawingSize(): { width: number; height: number } | null {
		return JSON.parse(api().drawingSize()) as { width: number; height: number } | null
	},

	/** SVG document of the shown drawing; empty if there is nothing to export. */
	svg: (theme: string, transparent: boolean): string => api().svg(theme, transparent),

	/** `data:` URL of a raster image of the shown drawing; empty if there is nothing to export. */
	rasterImage: (width: number, height: number, maxScale: number, theme: string, transparent: boolean, type: string): string =>
		api().rasterImage(width, height, maxScale, theme, transparent, type),

	vcd(): { vcd: string } | { error: string } | null {
		return JSON.parse(api().vcd()) as { vcd: string } | { error: string } | null
	},

	circuitBundle(uuid: string): { xml: string; name: string } | { error: string } | null {
		return JSON.parse(api().circuitBundle(uuid)) as { xml: string; name: string } | { error: string } | null
	},

	importCircuitBundle(xml: string, name: string, replace: boolean): ImportResult | null {
		return JSON.parse(api().importCircuitBundle(xml, name, replace)) as ImportResult | null
	},
}
