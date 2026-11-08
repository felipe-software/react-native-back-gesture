import { StyleSheet, View } from "react-native";

import { BackGestureContext } from "../context";
import { useProviderContext } from "../hooks/use-provider-context";
import type { BackGestureProviderProps } from "../types";

const styles = StyleSheet.create({
    root: { flex: 1 },
});

const detached = () => () => {};

export function BackGestureProvider({
    children,
    style,
}: BackGestureProviderProps) {
    const { context } = useProviderContext(detached);

    return (
        <BackGestureContext.Provider value={context}>
            <View style={[styles.root, style]}>{children}</View>
        </BackGestureContext.Provider>
    );
}
