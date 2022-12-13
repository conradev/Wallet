import { build } from "esbuild"
import { promises as fs } from "fs"
import { basename, dirname, join } from "path"
import { fileURLToPath } from "url"

const result = await build({
    entryPoints: ["page/page.ts", "content/content.ts", "background/background.ts"],
    bundle: true,
    minify: true,
    sourcemap: "inline",
    target: "es2020",
    platform: "browser",
    outdir: "build",
    write: false
})

const buildPath = join(dirname(fileURLToPath(import.meta.url)), "build")
try { await fs.mkdir(buildPath) } catch { }

for (const out of result.outputFiles) {
    await fs.writeFile(join(buildPath, basename(out.path)), out.contents)
}
