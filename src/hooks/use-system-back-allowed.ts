import { useContext, useEffect } from "react";

import { BackGestureContext } from "../context";

export function useSystemBackAllowed(allowed: boolean) {
    const context = useContext(BackGestureContext);

    useEffect(() => {
        if (!allowed || !context) return undefined;
        return context.allowSystemBack();
    }, [allowed, context]);
}
