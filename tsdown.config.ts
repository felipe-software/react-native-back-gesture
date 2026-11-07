import { defineConfig } from "tsdown";

export default defineConfig({
    clean: true,
    deps: {
        neverBundle: true,
    },
    dts: {
        resolver: "tsc",
    },
    // Native bundlers take src through the `react-native` condition, where codegen reads the spec.
    // Everything else (web, server rendering, Node) gets this build, which never imports native-only modules.
    entry: { index: "src/index.web.ts" },
    format: ["esm", "cjs"],
    outDir: "dist",
    platform: "neutral",
    sourcemap: true,
});
