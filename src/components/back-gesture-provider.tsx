import { type ComponentRef, useRef } from "react";
import { findNodeHandle, Platform, StyleSheet, View } from "react-native";

import { BackGestureContext, type BackGestureEventHandler } from "../context";
import { useProviderContext } from "../hooks/use-provider-context";
import BackGestureView from "../specs/BackGestureViewNativeComponent";
import type { BackGestureProviderProps } from "../types";

const styles = StyleSheet.create({
    root: { flex: 1 },
});

const detached = () => {};

export function BackGestureProvider({
    children,
    stackAnimation = "slide",
    sheetAnimation = true,
    style,
}: BackGestureProviderProps) {
    const viewRef = useRef<ComponentRef<typeof BackGestureView>>(null);
    const { context, nativeState } = useProviderContext(
        (handler: BackGestureEventHandler) => {
            const viewTag = findNodeHandle(viewRef.current);
            if (viewTag == null) return detached;
            handler.workletEventHandler.registerForEvents(viewTag);
            return () =>
                handler.workletEventHandler.unregisterFromEvents(viewTag);
        },
    );

    if (Platform.OS !== "android") {
        return (
            <BackGestureContext.Provider value={context}>
                <View style={[styles.root, style]}>{children}</View>
            </BackGestureContext.Provider>
        );
    }

    return (
        <BackGestureContext.Provider value={context}>
            <BackGestureView
                ref={viewRef}
                style={[styles.root, style]}
                stackAnimation={stackAnimation}
                sheetAnimation={sheetAnimation}
                interceptsBack={nativeState.interceptsBack}
                systemBackAllowed={nativeState.systemBackAllowed}
            >
                {children}
            </BackGestureView>
        </BackGestureContext.Provider>
    );
}
