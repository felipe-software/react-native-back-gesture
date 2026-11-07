import { createContext, useContext } from "react";
import type { BackGestureStackAnimation } from "react-native-back-gesture";

export interface AnimationSettings {
    stackAnimation: BackGestureStackAnimation;
    sheetAnimation: boolean;
    setStackAnimation: (animation: BackGestureStackAnimation) => void;
    toggleSheetAnimation: () => void;
}

export const AnimationSettingsContext = createContext<AnimationSettings>({
    stackAnimation: "slide",
    sheetAnimation: true,
    setStackAnimation: () => {},
    toggleSheetAnimation: () => {},
});

export function useAnimationSettings() {
    return useContext(AnimationSettingsContext);
}
