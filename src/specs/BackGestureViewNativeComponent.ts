import type { HostComponent, ViewProps } from "react-native";
import codegenNativeComponent from "react-native/Libraries/Utilities/codegenNativeComponent";
import type {
    DirectEventHandler,
    Double,
    WithDefault,
} from "react-native/Libraries/Types/CodegenTypes";

type BackGestureNativeEvent = Readonly<{
    progress: Double;
    edge: string;
}>;

export interface NativeProps extends ViewProps {
    interceptsBack?: boolean;
    systemBackAllowed?: boolean;
    stackAnimation?: WithDefault<"slide" | "scale" | "none", "slide">;
    sheetAnimation?: WithDefault<boolean, true>;
    onBackStarted?: DirectEventHandler<BackGestureNativeEvent>;
    onBackProgressed?: DirectEventHandler<BackGestureNativeEvent>;
    onBackCancelled?: DirectEventHandler<BackGestureNativeEvent>;
    onBackInvoked?: DirectEventHandler<BackGestureNativeEvent>;
}

export default codegenNativeComponent<NativeProps>(
    "BackGestureView",
) as HostComponent<NativeProps>;
