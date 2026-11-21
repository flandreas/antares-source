// Typed access to the Kotlin `WebHdl` API (VHDL/Verilog export of the open circuit).
import { engine } from '../../../core/engine'

export type HdlLanguage = 'vhdl' | 'verilog'

/** Defaults and labels of the export dialog, see `WebHdl.exportInfo()`. */
export interface HdlExportInfo {
	title: string
	/** Including the dot, e.g. ".vhdl" */
	fileExtension: string
	baseName: string
	waitTime: number
	minWaitTime: number
	testcases: { id: number; name: string }[]
	labels: { testcase: string; waitTime: string; delayModel: string; baseName: string }
}

export interface HdlFile {
	name: string
	content: string
}

export type HdlExportResult = { files: HdlFile[] } | { error: string }

export interface HdlExportOptions {
	baseName: string
	/** -1 for no test bench */
	testcaseId: number
	waitTime: number
	useDelayModel: boolean
}

export const hdlApi = {
	/** Returns `null` if no digital circuit is open. */
	exportInfo(language: HdlLanguage): HdlExportInfo | null {
		return JSON.parse(engine.api.hdl.exportInfo(language)) as HdlExportInfo | null
	},

	export(language: HdlLanguage, o: HdlExportOptions): HdlExportResult | null {
		return JSON.parse(engine.api.hdl.export(language, o.baseName, o.testcaseId, o.waitTime, o.useDelayModel)) as HdlExportResult | null
	},
}
