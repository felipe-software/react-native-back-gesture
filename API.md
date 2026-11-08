# API reference

Every export of `react-native-back-gesture`, taken from its TypeScript types. The
[README](README.md) explains when to use each one.

## `<BackGestureProvider />`

Wrap the app once, above the navigator.

| Prop | Type | Default | Description |
| --- | --- | --- | --- |
| `children` | `ReactNode` | — | The app. |
| `stackAnimation` | `"slide" \| "scale" \| "ios" \| "card" \| "shared-axis" \| "none"` | `"slide"` | How native stack screens (`react-native-screens`) follow the gesture. The [README](README.md#animations) describes each one. |
| `sheetAnimation` | `boolean` | `true` | Plays the Material back animation on visible native bottom sheets. |
| `style` | `StyleProp<ViewStyle>` | `{ flex: 1 }` | Style of the wrapping view. |

`"ios"` matches `react-native-screens`' `ios_from_right` animation. Set both so pushes and back-button pops look like the gesture:

```tsx
<BackGestureProvider stackAnimation="ios">
    <Stack screenOptions={{ animation: "ios_from_right" }} />
</BackGestureProvider>
```

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

Both shared values are updated on the UI thread, without a round trip through JavaScript. Read them in any worklet, for example `useAnimatedStyle` or `useAnimatedReaction`.

`progress` is eased with the curve Android uses for its own back animations. Because it keeps its value after `onBack`, a closing animation can start where the gesture left off. If your UI stays on screen after `onBack`, reset it with `progress.set(0)`.

A screen stays mounted while other screens are pushed on top of it. To handle back only while it is visible, add focus to `enabled`:

```tsx
const focused = useIsFocused();
useBackGesture({ enabled: focused && pageOpen, onBack: closePage });
```

## `useSystemBackAllowed(allowed)`

| Argument | Type | Description |
| --- | --- | --- |
| `allowed` | `boolean` | While `true` for any mounted caller and no `useBackGesture` handler is enabled, back gestures go straight to Android, which plays its back-to-home animation and closes the app. React Native doesn't receive them. |

While it is allowed, `BackHandler` listeners don't run. Native sheets aren't detected ahead of time, so pass `false` while one is open on that screen.

## How a gesture picks what to animate

At the start of each gesture, in this order:

1. **An enabled `useBackGesture` handler.** The most recently enabled one gets the progress and `onBack`.
2. **A visible native bottom sheet** (`BottomSheetBehavior`). It plays the Material back animation, then the back press reaches React Native, which closes it.
3. **A native stack with a screen to go back to.** The top screen plays the `stackAnimation` with the previous screen revealed under it. When the gesture is committed, the back press reaches React Native, which pops the screen.
4. **Nothing.** The back press reaches React Native as usual.

## Platform notes

- Tested on Android 12, 13, 14, 15 and 16 emulators, with Expo SDK 57 (React Native 0.86, Reanimated 4.5, Screens 4.26) and Expo SDK 58 (React Native 0.88, Reanimated 4.7, Screens 4.30). Older versions inside the supported ranges compile against the same APIs but haven't been run.
- JS-based stacks such as `@react-navigation/stack` aren't animated automatically. Use `useBackGesture` to drive their cards.
- Sheets and modals in their own window, like React Native's `Modal` or a Material `BottomSheetDialog`, get back events from that window, not from the app.

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
