// Parameter definitions of the open circuit, shown as inspector tab. Parameter values of placed
// sub-circuits are edited in the properties tab (Kotlin `WebPropertiesParams`).
import { circuitPanels, register } from '../registry'
import { ParamsPanel } from './ParamsPanel'
import './params.css'

register(circuitPanels, (p) => p.id, { id: 'params', label: 'Params', component: ParamsPanel })
