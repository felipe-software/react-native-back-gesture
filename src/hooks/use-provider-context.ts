import { useMemo, useState } from "react";
import { useSharedValue } from "react-native-reanimated";

import type {
    BackGestureContextValue,
    BackGestureEventHandler,
} from "../context";
import {
    createBackRegistry,
    NO_HANDLER,
    type BackRegistryState,
} from "../registry";

export interface NativeBackState {
    interceptsBack: boolean;
    systemBackAllowed: boolean;
}

export function useProviderContext(
    attach: (handler: BackGestureEventHandler) => () => void,
) {
    const activeHandler = useSharedValue(NO_HANDLER);
    const [nativeState, setNativeState] = useState<NativeBackState>({
        interceptsBack: false,
        systemBackAllowed: false,
    });
    const [registry] = useState(() =>
        createBackRegistry((state: BackRegistryState) => {
            activeHandler.set(state.activeHandler);
            setNativeState((current) => {
                const unchanged =
                    current.interceptsBack === state.interceptsBack &&
                    current.systemBackAllowed === state.systemBackAllowed;
                if (unchanged) return current;
                return {
                    interceptsBack: state.interceptsBack,
                    systemBackAllowed: state.systemBackAllowed,
                };
            });
        }),
    );
    const [stableAttach] = useState(() => attach);

    const context = useMemo<BackGestureContextValue>(
        () => ({ ...registry, activeHandler, attach: stableAttach }),
        [activeHandler, registry, stableAttach],
    );

    return { context, nativeState };
}
