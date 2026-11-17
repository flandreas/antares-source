import { defineConfig, type Plugin } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'
import { readFileSync } from 'node:fs'

// The Kotlin/JS build is a set of UMD modules linked as `jabbah-antares`.
// They must be pre-bundled (CommonJS -> ESM) in dev and transformed in production builds.
// Set ANTARES_KOTLIN_DEV=1 to use the unminified development build of the Kotlin code for debugging.
const kotlinDev = process.env.ANTARES_KOTLIN_DEV === '1'

const version = (JSON.parse(readFileSync(new URL('./package.json', import.meta.url), 'utf8')) as { version: string }).version

const KOTLIN_JS = /dist[\\/]js[\\/].*\.js$/

// Kotlin/JS creates a property reference on every read of a delegated property (`by lazy`, `by logger`) and then
// discards it. Creating one copies the whole KProperty prototype, which costs a large share of simulation and
// drawing time, so these dead statements are removed. Property references that are actually used are kept.
const DEAD_PROPERTY_REF = /^[ \t]*getPropertyCallableRef\((['"])[\w$]*\1, \d, tmp(?:_\d+)?, [\w$]+\([\w$]*\), null\);$/gm

// The pattern depends on the code the Kotlin compiler generates (reported upstream, see README). If a Kotlin update
// changes it, nothing is stripped any more and simulation silently gets slower, so the build fails instead.
let strippedRefs = 0

function stripDeadPropertyRefs(code: string): string {
	if (!code.includes('getPropertyCallableRef')) return code
	return code.replace(DEAD_PROPERTY_REF, () => {
		strippedRefs++
		return ''
	})
}

const NOTHING_STRIPPED =
	'No dead getPropertyCallableRef() statements were stripped from the Kotlin code. ' +
	'The Kotlin compiler output has probably changed; update DEAD_PROPERTY_REF in vite.config.ts.'

function kotlinOptimizations(): Plugin {
	return {
		name: 'antares-kotlin-optimizations',
		enforce: 'pre',
		transform(code, id) {
			return KOTLIN_JS.test(id) ? { code: stripDeadPropertyRefs(code), map: null } : null
		},
		buildEnd(error) {
			if (!error && this.meta.watchMode === false && strippedRefs === 0) this.error(NOTHING_STRIPPED)
		},
	}
}

// Restricts what the page may load and run, so that content from imported projects (e.g. documentation)
// can't run scripts or send data elsewhere even if it got past sanitizing. Only for builds: the dev server
// injects inline scripts. Inline styles are used by React components; AI requests go to OpenRouter;
// image exports fetch data: and blob: URLs.
const CONTENT_SECURITY_POLICY = [
	"default-src 'self'",
	"script-src 'self'",
	"style-src 'self' 'unsafe-inline'",
	"img-src 'self' data: blob: https:",
	"font-src 'self' data:",
	"connect-src 'self' data: blob: https://openrouter.ai",
	"worker-src 'self' blob:",
	"object-src 'none'",
	"base-uri 'self'",
	"form-action 'none'",
].join('; ')

function contentSecurityPolicy(): Plugin {
	return {
		name: 'antares-csp',
		apply: 'build',
		transformIndexHtml: () => [
			{ tag: 'meta', attrs: { 'http-equiv': 'Content-Security-Policy', content: CONTENT_SECURITY_POLICY }, injectTo: 'head-prepend' },
		],
	}
}

export default defineConfig({
	plugins: [react(), kotlinOptimizations(), contentSecurityPolicy()],
	define: { __APP_VERSION__: JSON.stringify(version) },
	resolve: kotlinDev
		? {
				alias: {
					'jabbah-antares': fileURLToPath(
						new URL('../build/antares/dist/js/developmentLibrary/jabbah-antares.js', import.meta.url),
					),
				},
			}
		: undefined,
	optimizeDeps: {
		include: ['jabbah-antares'],
		esbuildOptions: {
			plugins: [
				{
					name: 'antares-kotlin-optimizations',
					setup(build) {
						build.onLoad({ filter: KOTLIN_JS }, async ({ path }) => ({
							contents: stripDeadPropertyRefs(readFileSync(path, 'utf8')),
							loader: 'js',
						}))
						build.onEnd(() => {
							if (strippedRefs === 0) console.warn(NOTHING_STRIPPED)
						})
					},
				},
			],
		},
	},
	build: {
		commonjsOptions: { include: [/jabbah-antares/, /dist[\\/]js/, /node_modules/] },
		chunkSizeWarningLimit: 8000,
	},
	server: { port: 5317 },
})
