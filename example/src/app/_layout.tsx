import { Stack, usePathname } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { useMemo, useState } from "react";
import {
    BackGestureProvider,
    type BackGestureStackAnimation,
    useSystemBackAllowed,
} from "react-native-back-gesture";

import {
    AnimationSettingsContext,
    type AnimationSettings,
} from "../animation-settings";

function SystemBackOnHome() {
    useSystemBackAllowed(usePathname() === "/");
    return null;
}

export default function RootLayout() {
    const [stackAnimation, setStackAnimation] =
        useState<BackGestureStackAnimation>("slide");
    const [sheetAnimation, setSheetAnimation] = useState(true);
    const animationSettings = useMemo<AnimationSettings>(
        () => ({
            stackAnimation,
            sheetAnimation,
            setStackAnimation,
            toggleSheetAnimation: () => setSheetAnimation((on) => !on),
        }),
        [stackAnimation, sheetAnimation],
    );

    return (
        <AnimationSettingsContext.Provider value={animationSettings}>
            <BackGestureProvider
                stackAnimation={stackAnimation}
                sheetAnimation={sheetAnimation}
            >
                <StatusBar style="dark" />
                <SystemBackOnHome />
                <Stack>
                    <Stack.Screen
                        name="index"
                        options={{ title: "Back Gesture" }}
                    />
                    <Stack.Screen
                        name="details/[depth]"
                        options={{ title: "Details" }}
                    />
                    <Stack.Screen
                        name="form-sheet"
                        options={{
                            presentation: "formSheet",
                            sheetAllowedDetents: [0.5],
                            headerShown: false,
                        }}
                    />
                    <Stack.Screen
                        name="custom-sheet"
                        options={{ title: "Custom sheet" }}
                    />
                </Stack>
            </BackGestureProvider>
        </AnimationSettingsContext.Provider>
    );
}
