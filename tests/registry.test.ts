import { describe, expect, test } from "bun:test";

import {
    createBackRegistry,
    NO_HANDLER,
    type BackRegistryState,
} from "../src/registry";

const track = () => {
    const states: BackRegistryState[] = [];
    const registry = createBackRegistry((state) => states.push(state));
    return { registry, latest: () => states.at(-1) };
};

describe("createBackRegistry", () => {
    test("the most recently enabled handler is active", () => {
        const { registry, latest } = track();
        registry.enable(1);
        const releaseSecond = registry.enable(2);
        expect(latest()).toEqual({
            activeHandler: 2,
            interceptsBack: true,
            systemBackAllowed: false,
        });

        releaseSecond();
        expect(latest()?.activeHandler).toBe(1);
    });

    test("releasing an older handler keeps the newest one active", () => {
        const { registry, latest } = track();
        const releaseFirst = registry.enable(1);
        registry.enable(2);
        releaseFirst();
        expect(latest()?.activeHandler).toBe(2);
    });

    test("a release only counts once", () => {
        const { registry, latest } = track();
        registry.enable(1);
        const release = registry.enable(1);
        release();
        release();
        expect(latest()?.activeHandler).toBe(1);
    });

    test("system back is allowed while any caller allows it and no handler is enabled", () => {
        const { registry, latest } = track();
        const releaseFirst = registry.allowSystemBack();
        const releaseSecond = registry.allowSystemBack();
        expect(latest()?.systemBackAllowed).toBe(true);

        const releaseHandler = registry.enable(1);
        expect(latest()?.systemBackAllowed).toBe(false);
        releaseHandler();

        releaseFirst();
        expect(latest()?.systemBackAllowed).toBe(true);
        releaseSecond();
        expect(latest()).toEqual({
            activeHandler: NO_HANDLER,
            interceptsBack: false,
            systemBackAllowed: false,
        });
    });
});
