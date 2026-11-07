import type { ReactNode } from "react";
import { useEffect } from "react";
import { Pressable, StyleSheet, useWindowDimensions } from "react-native";
import Animated, {
    useAnimatedStyle,
    useSharedValue,
    withTiming,
} from "react-native-reanimated";
import { useBackGesture } from "react-native-back-gesture";

import { palette } from "./palette";

const MAX_SHRINK = 0.1;
const EDGE_SHIFT = 12;
const CORNER_RADIUS = 28;
const SCRIM_OPACITY = 0.35;
const SLIDE_DURATION_MS = 260;

interface CustomSheetProps {
    open: boolean;
    onClose: () => void;
    children: ReactNode;
}

export function CustomSheet({ open, onClose, children }: CustomSheetProps) {
    const { height } = useWindowDimensions();
    const { progress, edge } = useBackGesture({
        enabled: open,
        onBack: onClose,
    });
    const shown = useSharedValue(0);

    useEffect(() => {
        shown.set(withTiming(open ? 1 : 0, { duration: SLIDE_DURATION_MS }));
    }, [open, shown]);

    const scrimStyle = useAnimatedStyle(() => ({
        opacity: shown.value * (1 - progress.value) * SCRIM_OPACITY,
    }));

    const sheetStyle = useAnimatedStyle(() => {
        const withFinger = edge.value === "right" ? -1 : 1;
        return {
            transform: [
                { translateY: (1 - shown.value) * height },
                { translateX: withFinger * progress.value * EDGE_SHIFT },
                { scale: 1 - progress.value * MAX_SHRINK },
            ],
            borderBottomLeftRadius: progress.value * CORNER_RADIUS,
            borderBottomRightRadius: progress.value * CORNER_RADIUS,
        };
    });

    return (
        <Animated.View
            pointerEvents={open ? "auto" : "none"}
            style={StyleSheet.absoluteFill}
        >
            <Animated.View style={[styles.scrim, scrimStyle]}>
                <Pressable
                    accessibilityLabel="Close sheet"
                    onPress={onClose}
                    style={StyleSheet.absoluteFill}
                />
            </Animated.View>
            <Animated.View
                style={[styles.sheet, sheetStyle]}
                testID="custom-sheet"
            >
                {children}
            </Animated.View>
        </Animated.View>
    );
}

const styles = StyleSheet.create({
    scrim: {
        ...StyleSheet.absoluteFill,
        backgroundColor: palette.scrim,
    },
    sheet: {
        backgroundColor: palette.surface,
        borderTopLeftRadius: CORNER_RADIUS,
        borderTopRightRadius: CORNER_RADIUS,
        bottom: 0,
        gap: 12,
        left: 0,
        padding: 24,
        paddingBottom: 48,
        position: "absolute",
        right: 0,
    },
});
