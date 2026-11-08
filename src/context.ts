import { createContext } from "react";
import type { SharedValue } from "react-native-reanimated";

import type { BackRegistry } from "./registry";

export interface BackGestureEventHandler {
    workletEventHandler: {
        registerForEvents: (viewTag: number) => void;
        unregisterFromEvents: (viewTag: number) => void;
    };
}

export interface BackGestureContextValue extends BackRegistry {
    activeHandler: SharedValue<number>;
    attach: (handler: BackGestureEventHandler) => () => void;
}

export const BackGestureContext = createContext<BackGestureContextValue | null>(
    null,
);
