// HDL export: "Export VHDL…" and "Export Verilog…" in the project menu.
import { FileCode2 } from 'lucide-react'
import { projectMenuActions, register } from '../registry'
import { showHdlExport } from './ExportHdlDialog'
import './hdl.css'

register(projectMenuActions, (a) => a.label, 
	{
		label: 'Export VHDL…',
		icon: <FileCode2 size={15} />,
		enabled: (state) => state.circuit?.type === 'Digital',
		run: () => showHdlExport('vhdl'),
	},
	{
		label: 'Export Verilog…',
		icon: <FileCode2 size={15} />,
		enabled: (state) => state.circuit?.type === 'Digital',
		run: () => showHdlExport('verilog'),
	},
)
