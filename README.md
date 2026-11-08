<img src="assets/icon.svg" width="64" height="64" alt="">

# react-native-back-gesture

[![React Native >=0.76](https://img.shields.io/badge/React%20Native-%3E%3D0.76-61DAFB?style=flat-square&logo=react&logoColor=white)](https://reactnative.dev/) [![Reanimated 3–4](https://img.shields.io/badge/Reanimated-3%20%E2%80%93%204-8A2BE2?style=flat-square)](https://docs.swmansion.com/react-native-reanimated/) [![Runs with Expo](https://img.shields.io/badge/Runs%20with%20Expo-4630EB?style=flat-square&logo=expo&logoColor=white)](https://expo.dev/) [![Expo Router](https://img.shields.io/badge/Expo%20Router-compatible-000020?style=flat-square&logo=expo&logoColor=white)](https://docs.expo.dev/router/introduction/) [![React Navigation](https://img.shields.io/badge/React%20Navigation-native%20stack-6B52AE?style=flat-square)](https://reactnavigation.org/docs/native-stack-navigator)

Android predictive back for React Native. While the user swipes back, pushed screens slide away with the gesture and bottom sheets shrink the way Material sheets do. Your own UI gets the gesture progress as a Reanimated shared value.

> Still under development. Android 14 and newer animate; older Android versions, iOS and web fall back to normal back handling.

## Features

- Predictive back for native stack screens from Expo Router and React Navigation (anything built on `react-native-screens`): a slide by default, or an iOS-style, card, shared axis or scale animation.
- Material sheet animation for bottom sheets backed by `BottomSheetBehavior`, including `react-native-screens` form sheets.
- `useBackGesture` for your own UI: a custom sheet, modal, drawer or pager gets the gesture progress as a Reanimated `SharedValue`, updated on the UI thread.
- The system back-to-home animation on the screens where back should leave the app.
- Each built-in animation can be changed or turned off, so you can drive it yourself instead.
- No change in behavior on Android 13 and older: back presses still reach React Native, and `onBack` still runs.

## Installation

```sh
npm install react-native-back-gesture
```

It needs `react-native-reanimated` and `react-native-screens`, which Expo Router and React Navigation's native stack already install. The library has native Android code, so rebuild the app afterwards (`npx expo prebuild` or a fresh native build). It requires the New Architecture.

### Turn on predictive back

Android only reports gesture progress to apps that opt in.

With Expo, in `app.json`:

```json
{
    "expo": {
        "android": {
            "predictiveBackGestureEnabled": true
        }
    }
}
```

In a bare app, in `AndroidManifest.xml`:

```xml
<application android:enableOnBackInvokedCallback="true">
```

## Usage

### 1. Wrap the app

Put `BackGestureProvider` above your navigator. That alone animates native stack screens and native sheets.

Expo Router, in `app/_layout.tsx`:

```tsx
import { Stack } from "expo-router";
import { BackGestureProvider } from "react-native-back-gesture";

export default function RootLayout() {
    return (
        <BackGestureProvider>
            <Stack />
        </BackGestureProvider>
    );
}
```

React Navigation:

```tsx
<BackGestureProvider>
    <NavigationContainer>
        <Stack.Navigator>{/* screens */}</Stack.Navigator>
    </NavigationContainer>
</BackGestureProvider>
```

### 2. Animate your own UI with `useBackGesture`

While a handler is enabled, back gestures belong to it: `progress` follows the finger and `onBack` runs when the user lets go past the threshold. Tie `enabled` to the state that makes your UI dismissible.

```tsx
import Animated, { useAnimatedStyle } from "react-native-reanimated";
import { useBackGesture } from "react-native-back-gesture";

function Sheet({ open, onClose }: { open: boolean; onClose: () => void }) {
    const { progress, edge } = useBackGesture({ enabled: open, onBack: onClose });

    const style = useAnimatedStyle(() => ({
        transform: [
            { translateX: (edge.value === "right" ? -1 : 1) * progress.value * 12 },
            { scale: 1 - progress.value * 0.1 },
        ],
    }));

    return <Animated.View style={style}>{/* … */}</Animated.View>;
}
```

`progress` goes from `0` to `1`, eased with the curve Android uses for its own back animations. It eases back to `0` when the user cancels, and keeps its last value after `onBack`, so your closing animation starts from where the gesture left off. It returns to `0` when the next gesture starts or when the handler is enabled again. If your UI stays on screen after `onBack`, reset it yourself with `progress.set(0)`.

To react to it in other ways, read it in any worklet, for example with `useAnimatedReaction`.

A screen stays mounted when another screen is pushed on top of it. If its handler should only run while it is visible, add focus to `enabled`:

```tsx
const focused = useIsFocused();
useBackGesture({ enabled: focused && pageOpen, onBack: closePage });
```

### 3. Let the system close the app

By default React Native receives every back gesture, so Android cannot show its back-to-home preview. Call `useSystemBackAllowed(true)` where back should leave the app:

```tsx
import { usePathname } from "expo-router";
import { useSystemBackAllowed } from "react-native-back-gesture";

function SystemBackOnHome() {
    useSystemBackAllowed(usePathname() === "/");
    return null;
}
```

While it is allowed, back gestures skip React Native entirely, so `BackHandler` listeners don't run. An enabled `useBackGesture` handler turns it off. Native sheets aren't detected ahead of time, so pass `false` while one is open on that screen.

### Choosing the animations

Stack screens slide away with the gesture by default. Pick another animation with `stackAnimation`:

| `stackAnimation` | What the top screen does |
| --- | --- |
| `"slide"` (default) | Moves sideways with the gesture, revealing the previous screen under a fading scrim. |
| `"scale"` | Shrinks, rounds its corners and follows the finger, like Android's own back-to-previous-screen animation. |
| `"ios"` | Copies iOS: follows the finger one to one while the previous screen slides in from 30% to the side under a light dim, then settles with a spring. |
| `"card"` | Slides away while the previous screen comes forward from slightly smaller, with rounded corners over black. |
| `"shared-axis"` | Material's shared axis: both screens shift 30dp toward the finger while they cross-fade. |
| `"none"` | Nothing until the gesture is committed; then React Native pops the screen as usual. |

```tsx
<BackGestureProvider stackAnimation="scale">
```

`"ios"` matches `react-native-screens`' `ios_from_right` animation, so set both to make pushes and back-button pops look the same as the gesture:

```tsx
<BackGestureProvider stackAnimation="ios">
    <Stack screenOptions={{ animation: "ios_from_right" }} />
</BackGestureProvider>
```

If you'd rather animate a native sheet yourself, turn the built-in sheet animation off and drive it from `useBackGesture`:

```tsx
<BackGestureProvider sheetAnimation={false}>
```

## How it decides what animates

At the start of each gesture, in this order:

1. **An enabled `useBackGesture` handler.** The most recently enabled one gets the progress and `onBack`.
2. **A visible native bottom sheet** (`BottomSheetBehavior`). It plays the Material back animation, then the back press reaches React Native, which closes it.
3. **A native stack with a screen to go back to.** The top screen plays the `stackAnimation` with the previous screen revealed under it. When the gesture is committed, the back press reaches React Native, which pops the screen.
4. **Nothing.** The back press reaches React Native as usual.

## Platform support

| Platform | Behavior |
| --- | --- |
| Android 14+ | Animated with gesture progress. |
| Android 13 and older | No progress. Back presses reach React Native and enabled `onBack` handlers. |
| iOS, web | No-op. The provider renders a plain `View`; iOS keeps its own swipe-back. |

### Compatible versions

| Package | Supported range |
| --- | --- |
| React Native | `>=0.76`, New Architecture |
| Reanimated | `>=3.16 <5` |
| React Native Screens | `>=4.0` |

Tested on Android 12, 13, 14, 15 and 16 emulators, with Expo SDK 57 (React Native 0.86, Reanimated 4.5, Screens 4.26) and Expo SDK 58 (React Native 0.88, Reanimated 4.7, Screens 4.30). Older versions inside these ranges compile against the same APIs but haven't been run.

### Not covered

- JS-based stacks such as `@react-navigation/stack` aren't animated automatically. Use `useBackGesture` to drive their cards.
- Sheets and modals in their own window, like React Native's `Modal` or a Material `BottomSheetDialog`, get back events from that window, not from the app.

## API

See [API.md](API.md).

## Examples

- [`example`](example): Expo Router.
- [`example-react-navigation`](example-react-navigation): React Navigation native stack, no Expo Router.

```sh
bun install
bun run example:android
```

## License

MIT
