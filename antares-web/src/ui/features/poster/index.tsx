// Posters: a printable overview of the project's circuits (desktop: CreatePosterAction, PosterViewerSwing).
import { LayoutGrid } from 'lucide-react'
import { dialogs } from '../../Dialogs'
import { projectMenuActions, register } from '../registry'
import { PosterDialog } from './PosterDialog'
import './poster.css'

register(projectMenuActions, (a) => a.label, {
	label: 'Create poster…',
	icon: <LayoutGrid size={15} />,
	enabled: (state) => !!state.project,
	run: () => {
		void dialogs.custom<null>((done) => <PosterDialog onClose={() => done(null)} />)
	},
})
