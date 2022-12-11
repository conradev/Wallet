import { build } from "esbuild"

await build({
    entryPoints: ["page/page.ts", "content/content.ts", "background/background.ts"],
    bundle: true,
    minify: true,
    sourcemap: "inline",
    target: "es2020",
    platform: "browser",
    outdir: "build",
})
