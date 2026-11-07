import type { ReactNode } from "react";
import type { StyleProp, ViewStyle } from "react-native";
import type { SharedValue } from "react-native-reanimated";

export type BackGestureEdge = "left" | "right";

export type BackGestureStackAnimation = "slide" | "scale" | "none";

export interface BackGesture {
    progress: SharedValue<number>;
    edge: SharedValue<BackGestureEdge>;
}

export interface UseBackGestureOptions {
    onBack: () => void;
    enabled?: boolean;
}

export interface BackGestureProviderProps {
    children?: ReactNode;
    stackAnimation?: BackGestureStackAnimation;
    sheetAnimation?: boolean;
    style?: StyleProp<ViewStyle>;
}
