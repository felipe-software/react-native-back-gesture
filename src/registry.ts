export const NO_HANDLER = 0;

export interface BackRegistryState {
    activeHandler: number;
    interceptsBack: boolean;
    systemBackAllowed: boolean;
}

export interface BackRegistry {
    enable: (handler: number) => () => void;
    allowSystemBack: () => () => void;
}

export function createBackRegistry(
    onChange: (state: BackRegistryState) => void,
): BackRegistry {
    const enabledHandlers: number[] = [];
    let systemBackVotes = 0;

    const publish = () => {
        const interceptsBack = enabledHandlers.length > 0;
        onChange({
            activeHandler: enabledHandlers.at(-1) ?? NO_HANDLER,
            interceptsBack,
            systemBackAllowed: systemBackVotes > 0 && !interceptsBack,
        });
    };

    const releaseOnce = (release: () => void) => {
        let released = false;
        return () => {
            if (released) return;
            released = true;
            release();
            publish();
        };
    };

    return {
        enable(handler) {
            enabledHandlers.push(handler);
            publish();
            return releaseOnce(() => {
                enabledHandlers.splice(enabledHandlers.lastIndexOf(handler), 1);
            });
        },
        allowSystemBack() {
            systemBackVotes += 1;
            publish();
            return releaseOnce(() => {
                systemBackVotes -= 1;
            });
        },
    };
}
