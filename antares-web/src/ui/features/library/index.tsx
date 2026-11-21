// Project structure, project/library properties and user libraries.
import { BookMarked, Settings2 } from 'lucide-react'
import { projectMenuActions, register } from '../registry'
import { showLibraryManager } from './LibraryManager'
import { showProperties } from './PropertiesDialog'
import './library.css'

register(
	projectMenuActions,
	(a) => a.label,
	{ label: 'Project properties…', icon: <Settings2 size={15} />, enabled: (s) => !!s.project, run: showProperties },
	{ label: 'Your libraries…', icon: <BookMarked size={15} />, enabled: () => true, run: () => void showLibraryManager() },
)
