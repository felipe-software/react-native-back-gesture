import { mock } from "bun:test";
import { useState } from "react";

const mockModule = (
    specifier: string,
    factory: () => Record<string, unknown>,
) => {
    const resolved = import.meta.resolve(specifier);
    mock.module(specifier, factory);
    mock.module(resolved, factory);
    if (resolved.startsWith("file:")) {
        mock.module(new URL(resolved).pathname, factory);
    }
};

(globalThis as { __DEV__?: boolean }).__DEV__ = true;

export const VIEW_TAG = 7;

export const platform: { OS: string; Version: number } = {
    OS: "android",
    Version: 36,
};

type BackPressListener = () => boolean | null | undefined;
const backPressListeners: BackPressListener[] = [];

export const backHandler = {
    addEventListener(_event: "hardwareBackPress", listener: BackPressListener) {
        backPressListeners.push(listener);
        return {
            remove() {
                backPressListeners.splice(
                    backPressListeners.indexOf(listener),
                    1,
                );
            },
        };
    },
};

export function pressBack() {
    for (const listener of [...backPressListeners].reverse()) {
        if (listener()) return true;
    }
    return false;
}

export interface MockSharedValue<T> {
    value: T;
    get: () => T;
    set: (value: T) => void;
}

type NativeEvent = { eventName: string; progress: number; edge: string };
type RegisteredHandler = { worklet: (event: NativeEvent) => void };
const registeredHandlers = new Set<RegisteredHandler>();

export function dispatchNativeEvent(name: string, progress = 0, edge = "left") {
    for (const handler of registeredHandlers) {
        handler.worklet({ eventName: `${VIEW_TAG}${name}`, progress, edge });
    }
}

export function registeredHandlerCount() {
    return registeredHandlers.size;
}

mockModule("react-native", () => ({
    BackHandler: backHandler,
    findNodeHandle: (ref: unknown) => (ref ? VIEW_TAG : null),
    Platform: platform,
    StyleSheet: {
        create<T>(styles: T) {
            return styles;
        },
    },
    View: "View",
}));

mockModule("react-native/Libraries/Utilities/codegenNativeComponent", () => ({
    default: () => "BackGestureView",
}));

mockModule("react-native-reanimated", () => ({
    cancelAnimation() {},
    Easing: {
        out: (easing: (value: number) => number) => easing,
        quad: (value: number) => value * value,
    },
    useEvent(worklet: (event: NativeEvent) => void) {
        const [handler] = useState(() => {
            const entry: RegisteredHandler = { worklet };
            return {
                workletEventHandler: {
                    registerForEvents: () => registeredHandlers.add(entry),
                    unregisterFromEvents: () =>
                        registeredHandlers.delete(entry),
                },
            };
        });
        return handler;
    },
    useSharedValue<T>(initial: T) {
        const [shared] = useState<MockSharedValue<T>>(() => ({
            value: initial,
            get() {
                return this.value;
            },
            set(value: T) {
                this.value = value;
            },
        }));
        return shared;
    },
    withTiming: (target: number) => target,
}));
