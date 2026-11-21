// Copies the static data the web editor needs (system libraries, sample projects and
// translation bundles) from the Kotlin modules into `public/data`, and writes an index.
import { cpSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..')
const out = resolve(root, 'antares-web/public/data')

rmSync(out, { recursive: true, force: true })
mkdirSync(out, { recursive: true })

const libraries = join(root, 'antares/jvm/rsc/libraries')
cpSync(libraries, join(out, 'libraries'), { recursive: true })

const samples = join(root, 'antares/samples/projects')
cpSync(samples, join(out, 'samples'), { recursive: true, filter: (src) => !src.includes('/history') })

const translations = join(out, 'translations')
mkdirSync(translations)
const bundles = {
	'jabbah-base': 'base',
	'jabbah-draw': 'draw',
	'jabbah-edit': 'edit',
	'jabbah-execution': 'execution',
	'jabbah-app': 'app',
	'jabbah-graph': 'graph',
	antares: 'antares',
}
for (const [bundle, module] of Object.entries(bundles)) {
	for (const lang of ['en', 'de']) {
		const file = join(root, module, 'jvm/rsc', `${bundle}_${lang}.properties`)
		if (existsSync(file)) cpSync(file, join(translations, `${bundle}_${lang}.properties`))
	}
}

// Index of every library directory and its circuit files, so the app can preload them.
const index = {}
for (const [kind, dir] of [['system', join(out, 'libraries')], ['samples', join(out, 'samples')]]) {
	index[kind] = readdirSync(dir, { withFileTypes: true })
		.filter((d) => d.isDirectory())
		.map((d) => ({
			uuid: d.name,
			circuits: readdirSync(join(dir, d.name))
				.filter((f) => f.endsWith('.cir'))
				.map((f) => f.slice(0, -4)),
		}))
}
writeFileSync(join(out, 'index.json'), JSON.stringify(index))

// All system library files in one bundle, keyed by their path in the editor's file store
const system = { 'system/dictionary.xml': readFileSync(join(out, 'libraries/dictionary.xml'), 'utf8') }
for (const lib of index.system) {
	system[`system/${lib.uuid}/library.xml`] = readFileSync(join(out, 'libraries', lib.uuid, 'library.xml'), 'utf8')
	for (const c of lib.circuits) {
		system[`system/${lib.uuid}/${c}.cir`] = readFileSync(join(out, 'libraries', lib.uuid, `${c}.cir`), 'utf8')
	}
}
writeFileSync(join(out, 'system.json'), JSON.stringify(system))
rmSync(join(out, 'libraries'), { recursive: true })
console.log(`prepared data: ${index.system.length} system libraries, ${index.samples.length} samples`)
