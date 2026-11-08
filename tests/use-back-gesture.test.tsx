import { cleanup, render } from "@testing-library/react-native";
import { afterEach, beforeEach, describe, expect, mock, test } from "bun:test";
import { createElement, type ReactNode } from "react";

import type { BackGesture, UseBackGestureOptions } from "../src/types";
import {
    dispatchNativeEvent,
    platform,
    pressBack,
    registeredHandlerCount,
} from "./setup";

const { BackGestureProvider } =
    await import("../src/components/back-gesture-provider");
const { BackGestureProvider: WebBackGestureProvider } =
    await import("../src/components/back-gesture-provider.web");
const { useBackGesture } = await import("../src/hooks/use-back-gesture");
const { useSystemBackAllowed } =
    await import("../src/hooks/use-system-back-allowed");

const gestures: Record<string, BackGesture> = {};

function Handler({
    name,
    ...options
}: UseBackGestureOptions & { name: string }) {
    gestures[name] = useBackGesture(options);
    return null;
}

function SystemBack({ allowed }: { allowed: boolean }) {
    useSystemBackAllowed(allowed);
    return null;
}

const inProvider = (children: ReactNode) =>
    createElement(BackGestureProvider, null, children);

const nativeProps = (renderer: Awaited<ReturnType<typeof render>>) =>
    renderer.container.queryAll((node) => node.type === "BackGestureView")[0]!
        .props;

afterEach(cleanup);
beforeEach(() => {
    platform.OS = "android";
});

describe("useBackGesture", () => {
    test("drives only the newest enabled handler", async () => {
        await render(
            inProvider([
                createElement(Handler, {
                    key: "a",
                    name: "a",
                    onBack: () => {},
                }),
                createElement(Handler, {
                    key: "b",
                    name: "b",
                    onBack: () => {},
                }),
            ]),
        );

        dispatchNativeEvent("onBackStarted", 0.1, "right");
        dispatchNativeEvent("onBackProgressed", 0.6, "right");

        expect(gestures.b!.progress.value).toBe(0.6);
        expect(gestures.b!.edge.value).toBe("right");
        expect(gestures.a!.progress.value).toBe(0);
    });

    test("settles to zero on cancel and holds its value after the back is invoked", async () => {
        await render(
            inProvider(createElement(Handler, { name: "a", onBack: () => {} })),
        );

        dispatchNativeEvent("onBackStarted", 0.2);
        dispatchNativeEvent("onBackProgressed", 0.5);
        dispatchNativeEvent("onBackCancelled", 0.5);
        expect(gestures.a!.progress.value).toBe(0);

        dispatchNativeEvent("onBackStarted", 0.3);
        dispatchNativeEvent("onBackProgressed", 0.8);
        dispatchNativeEvent("onBackInvoked", 0.8);
        dispatchNativeEvent("onBackProgressed", 0.1);
        expect(gestures.a!.progress.value).toBe(0.8);
    });

    test("resets progress when it is enabled again", async () => {
        const renderer = await render(
            inProvider(createElement(Handler, { name: "a", onBack: () => {} })),
        );
        dispatchNativeEvent("onBackStarted", 0.4);
        dispatchNativeEvent("onBackInvoked", 0.4);

        await renderer.rerender(
            inProvider(
                createElement(Handler, {
                    enabled: false,
                    name: "a",
                    onBack: () => {},
                }),
            ),
        );
        expect(gestures.a!.progress.value).toBe(0.4);

        await renderer.rerender(
            inProvider(createElement(Handler, { name: "a", onBack: () => {} })),
        );
        expect(gestures.a!.progress.value).toBe(0);
    });

    test("calls the latest onBack of the newest enabled handler", async () => {
        const first = mock(() => {});
        const stale = mock(() => {});
        const latest = mock(() => {});
        const renderer = await render(
            inProvider([
                createElement(Handler, { key: "a", name: "a", onBack: first }),
                createElement(Handler, { key: "b", name: "b", onBack: stale }),
            ]),
        );
        await renderer.rerender(
            inProvider([
                createElement(Handler, { key: "a", name: "a", onBack: first }),
                createElement(Handler, { key: "b", name: "b", onBack: latest }),
            ]),
        );

        expect(pressBack()).toBe(true);
        expect(latest).toHaveBeenCalledTimes(1);
        expect(stale).not.toHaveBeenCalled();
        expect(first).not.toHaveBeenCalled();
    });

    test("a disabled handler leaves back to the app", async () => {
        const onBack = mock(() => {});
        const renderer = await render(
            inProvider(
                createElement(Handler, { enabled: false, name: "a", onBack }),
            ),
        );

        expect(pressBack()).toBe(false);
        expect(onBack).not.toHaveBeenCalled();
        expect(nativeProps(renderer).interceptsBack).toBe(false);
    });

    test("tells the native view when a handler intercepts back", async () => {
        const renderer = await render(
            inProvider(createElement(Handler, { name: "a", onBack: () => {} })),
        );
        expect(nativeProps(renderer).interceptsBack).toBe(true);
    });

    test("detaches its event handler on unmount", async () => {
        const renderer = await render(
            inProvider(createElement(Handler, { name: "a", onBack: () => {} })),
        );
        expect(registeredHandlerCount()).toBe(1);
        await renderer.unmount();
        expect(registeredHandlerCount()).toBe(0);
    });

    test("still handles back presses without a provider", async () => {
        const warn = mock(() => {});
        const originalWarn = console.warn;
        console.warn = warn;
        const onBack = mock(() => {});
        await render(createElement(Handler, { name: "a", onBack }));
        console.warn = originalWarn;

        expect(pressBack()).toBe(true);
        expect(onBack).toHaveBeenCalledTimes(1);
        expect(warn).toHaveBeenCalledTimes(1);
    });
});

describe("useSystemBackAllowed", () => {
    test("allows system back only while no handler is enabled", async () => {
        const renderer = await render(
            inProvider(createElement(SystemBack, { allowed: true })),
        );
        expect(nativeProps(renderer).systemBackAllowed).toBe(true);

        await renderer.rerender(
            inProvider([
                createElement(SystemBack, { allowed: true, key: "system" }),
                createElement(Handler, {
                    key: "a",
                    name: "a",
                    onBack: () => {},
                }),
            ]),
        );
        expect(nativeProps(renderer).systemBackAllowed).toBe(false);
    });
});

describe("BackGestureProvider", () => {
    test("renders a plain view outside Android", async () => {
        platform.OS = "ios";
        const renderer = await render(inProvider(null));
        expect(
            renderer.container.queryAll(
                (node) => node.type === "BackGestureView",
            ),
        ).toHaveLength(0);
        expect(
            renderer.container.queryAll((node) => node.type === "View"),
        ).toHaveLength(1);
    });

    test("slides stack screens and animates sheets by default", async () => {
        const renderer = await render(inProvider(null));
        expect(nativeProps(renderer).stackAnimation).toBe("slide");
        expect(nativeProps(renderer).sheetAnimation).toBe(true);
    });

    test("forwards the animation options", async () => {
        const renderer = await render(
            createElement(BackGestureProvider, {
                sheetAnimation: false,
                stackAnimation: "scale",
            }),
        );
        expect(nativeProps(renderer).stackAnimation).toBe("scale");
        expect(nativeProps(renderer).sheetAnimation).toBe(false);
    });
});

describe("BackGestureProvider on web", () => {
    test("renders a plain view and still provides handlers", async () => {
        const warn = mock(() => {});
        const originalWarn = console.warn;
        console.warn = warn;
        const onBack = mock(() => {});
        const renderer = await render(
            createElement(
                WebBackGestureProvider,
                null,
                createElement(Handler, { name: "a", onBack }),
            ),
        );
        console.warn = originalWarn;

        expect(
            renderer.container.queryAll((node) => node.type === "View"),
        ).toHaveLength(1);
        expect(pressBack()).toBe(true);
        expect(onBack).toHaveBeenCalledTimes(1);
        expect(warn).not.toHaveBeenCalled();
    });
});
