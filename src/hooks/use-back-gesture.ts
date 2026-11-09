import {
    useContext,
    useEffect,
    useLayoutEffect,
    useMemo,
    useRef,
    useState,
} from "react";
import { BackHandler, Platform } from "react-native";
import {
    cancelAnimation,
    Easing,
    useEvent,
    useSharedValue,
    withTiming,
} from "react-native-reanimated";

import { BackGestureContext, type BackGestureEventHandler } from "../context";
import { NO_HANDLER } from "../registry";
import type {
    BackGesture,
    BackGestureEdge,
    UseBackGestureOptions,
} from "../types";

interface NativeBackGestureEvent {
    progress: number;
    edge: string;
}

const EVENT_NAMES = [
    "onBackStarted",
    "onBackProgressed",
    "onBackCancelled",
    "onBackInvoked",
];
const CANCEL_DURATION_MS = 180;

let lastHandlerId = NO_HANDLER;
let warnedMissingProvider = false;

export function useBackGesture({
    onBack,
    enabled = true,
}: UseBackGestureOptions): BackGesture {
    const context = useContext(BackGestureContext);
    const [handlerId] = useState(() => ++lastHandlerId);
    const progress = useSharedValue(0);
    const edge = useSharedValue<BackGestureEdge>("left");
    const ownsGesture = useSharedValue(false);
    const noActiveHandler = useSharedValue(NO_HANDLER);
    const activeHandler = context?.activeHandler ?? noActiveHandler;
    const onBackRef = useRef(onBack);

    useLayoutEffect(() => {
        onBackRef.current = onBack;
    });

    const eventHandler = useEvent<NativeBackGestureEvent>((event) => {
        "worklet";
        const name = event.eventName;
        if (name.endsWith("onBackStarted")) {
            ownsGesture.value = activeHandler.value === handlerId;
            if (!ownsGesture.value) return;
            cancelAnimation(progress);
            edge.value = event.edge === "right" ? "right" : "left";
            progress.value = event.progress;
            return;
        }
        if (!ownsGesture.value) return;
        if (name.endsWith("onBackProgressed")) {
            progress.value = event.progress;
        } else if (name.endsWith("onBackCancelled")) {
            ownsGesture.value = false;
            progress.value = withTiming(0, {
                duration: CANCEL_DURATION_MS,
                easing: Easing.out(Easing.quad),
            });
        } else if (name.endsWith("onBackInvoked")) {
            ownsGesture.value = false;
        }
    }, EVENT_NAMES);

    useEffect(() => {
        if (!context) return undefined;
        return context.attach(
            eventHandler as unknown as BackGestureEventHandler,
        );
    }, [context, eventHandler]);

    useEffect(() => {
        if (!enabled) return undefined;
        if (
            __DEV__ &&
            !context &&
            Platform.OS === "android" &&
            !warnedMissingProvider
        ) {
            warnedMissingProvider = true;
            console.warn(
                "useBackGesture needs a <BackGestureProvider> above it to report gesture progress. onBack still runs on back presses.",
            );
        }
        progress.set(0);
        const release = context?.enable(handlerId);
        const subscription = BackHandler.addEventListener(
            "hardwareBackPress",
            () => {
                onBackRef.current();
                return true;
            },
        );
        return () => {
            subscription.remove();
            release?.();
        };
    }, [context, enabled, handlerId, progress]);

    return useMemo(() => ({ progress, edge }), [progress, edge]);
}
