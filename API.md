# API reference

Every export of `react-native-back-gesture`, taken from its TypeScript types. The
[README](README.md) explains when to use each one.

## `<BackGestureProvider />`

Wrap the app once, above the navigator.

| Prop | Type | Default | Description |
| --- | --- | --- | --- |
| `children` | `ReactNode` | — | The app. |
| `stackAnimation` | `"slide" \| "scale" \| "ios" \| "card" \| "shared-axis" \| "none"` | `"slide"` | How native stack screens (`react-native-screens`) follow the gesture. The [README](README.md#choosing-the-animations) describes each one. |
| `sheetAnimation` | `boolean` | `true` | Plays the Material back animation on visible native bottom sheets. |
| `style` | `StyleProp<ViewStyle>` | `{ flex: 1 }` | Style of the wrapping view. |

## `useBackGesture(options)`

Takes over the back gesture while enabled.

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `onBack` | `() => void` | — | Runs on the JS thread when the user commits the gesture, or presses back on Android 13 and older (**required**). |
| `enabled` | `boolean` | `true` | Whether this handler takes back gestures. The most recently enabled handler wins. |

Returns `BackGesture`:

| Field | Type | Description |
| --- | --- | --- |
| `progress` | `SharedValue<number>` | `0` to `1` during the gesture, eased. Eases back to `0` on cancel; keeps its value after `onBack` until the next gesture or until the handler is enabled again. |
| `edge` | `SharedValue<"left" \| "right">` | The screen edge the gesture started from. |

Both shared values are updated on the UI thread, without a round trip through JavaScript.

## `useSystemBackAllowed(allowed)`

| Argument | Type | Description |
| --- | --- | --- |
| `allowed` | `boolean` | While `true` for any mounted caller and no `useBackGesture` handler is enabled, back gestures go straight to Android, which plays its back-to-home animation and closes the app. React Native doesn't receive them. |

## Types

```ts
type BackGestureEdge = "left" | "right";

type BackGestureStackAnimation =
    "slide" | "scale" | "ios" | "card" | "shared-axis" | "none";

interface BackGesture {
    progress: SharedValue<number>;
    edge: SharedValue<BackGestureEdge>;
}

interface UseBackGestureOptions {
    onBack: () => void;
    enabled?: boolean;
}

interface BackGestureProviderProps {
    children?: ReactNode;
    stackAnimation?: BackGestureStackAnimation;
    sheetAnimation?: boolean;
    style?: StyleProp<ViewStyle>;
}
```
