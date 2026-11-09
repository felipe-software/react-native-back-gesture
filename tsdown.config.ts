import { defineConfig } from "tsdown";

export default defineConfig({
    clean: true,
    deps: {
        neverBundle: true,
    },
    dts: {
        resolver: "tsc",
    },
    entry: { index: "src/index.web.ts" },
    format: ["esm", "cjs"],
    outDir: "dist",
    platform: "neutral",
    sourcemap: true,
});
