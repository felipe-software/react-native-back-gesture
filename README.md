<img src="assets/icon.svg" width="64" height="64" alt="">

# react-native-back-gesture

[![npm version](https://img.shields.io/npm/v/react-native-back-gesture?style=flat-square)](https://www.npmjs.com/package/react-native-back-gesture) [![license](https://img.shields.io/npm/l/react-native-back-gesture?style=flat-square)](https://www.npmjs.com/package/react-native-back-gesture)
[![React Native >=0.76](https://img.shields.io/badge/React%20Native-%3E%3D0.76-61DAFB?style=flat-square&logo=react&logoColor=white)](https://reactnative.dev/) [![Reanimated 3–4](https://img.shields.io/badge/Reanimated-3%20%E2%80%93%204-8A2BE2?style=flat-square)](https://docs.swmansion.com/react-native-reanimated/) [![Runs with Expo](https://img.shields.io/badge/Runs%20with%20Expo-4630EB?style=flat-square&logo=expo&logoColor=white)](https://expo.dev/) [![Expo Router](https://img.shields.io/badge/Expo%20Router-compatible-000020?style=flat-square&logo=expo&logoColor=white)](https://docs.expo.dev/router/introduction/) [![React Navigation](https://img.shields.io/badge/React%20Navigation-native%20stack-6B52AE?style=flat-square)](https://reactnavigation.org/docs/native-stack-navigator)

Android predictive back for React Native. While the user swipes back, pushed screens follow the gesture and bottom sheets shrink like Material sheets. Your own UI gets the gesture progress as a Reanimated shared value.

> Android 14 and newer animate. On Android 13 and older, iOS and web, back works as it did before.

## Features

- Native stack screens from Expo Router and React Navigation follow the gesture: slide, iOS, card, shared axis or scale.
- Native bottom sheets, including `react-native-screens` form sheets, play the Material back animation.
- `useBackGesture` gives your own sheets, modals and drawers the progress as a `SharedValue`, updated on the UI thread.
- The system back-to-home animation on screens where back should leave the app.

## Installation

```sh
npm install react-native-back-gesture
```

It needs `react-native-reanimated`, `react-native-screens` and the New Architecture. Expo Router and React Navigation's native stack already install both packages. Rebuild the native app afterwards.

Then opt in to predictive back: `"predictiveBackGestureEnabled": true` under `expo.android` in `app.json`, or `android:enableOnBackInvokedCallback="true"` on `<application>` in `AndroidManifest.xml`.

## Usage

Put `BackGestureProvider` above the navigator. That alone animates stack screens and native sheets.

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

With React Navigation, put it around `NavigationContainer`.

### Your own UI

While enabled, the handler owns the back gesture: `progress` follows the finger from `0` to `1`, and `onBack` runs when the user lets go past the threshold.

```tsx
import Animated, { useAnimatedStyle } from "react-native-reanimated";
import { useBackGesture } from "react-native-back-gesture";

function Sheet({ open, onClose }: { open: boolean; onClose: () => void }) {
    const { progress } = useBackGesture({ enabled: open, onBack: onClose });
    const style = useAnimatedStyle(() => ({
        transform: [{ scale: 1 - progress.value * 0.1 }],
    }));

    return <Animated.View style={style}>{/* … */}</Animated.View>;
}
```

### Closing the app

Call `useSystemBackAllowed(true)` on screens where back should leave the app, so Android can show its back-to-home preview. For example, in the root layout:

```tsx
useSystemBackAllowed(usePathname() === "/");
```

## Animations

| `stackAnimation` | What happens |
| --- | --- |
| `"slide"` (default) | The top screen slides away over a fading scrim. |
| `"scale"` | The top screen shrinks and follows the finger, like Android's own animation. |
| `"ios"` | Like iOS: the previous screen trails 30% behind. Pair it with `animation: "ios_from_right"`. |
| `"card"` | The previous screen comes forward from behind. |
| `"shared-axis"` | Both screens shift a little and cross-fade. |
| `"none"` | No animation; the screen pops once the gesture is committed. |

```tsx
<BackGestureProvider stackAnimation="ios">
```

`sheetAnimation={false}` turns off the sheet animation, so you can drive the sheet yourself with `useBackGesture`.

## Compatibility

| | Supported |
| --- | --- |
| React Native | `>=0.76`, New Architecture |
| Reanimated | `>=3.16 <5` |
| React Native Screens | `>=4.0` |
| Android 14+ | Animated with gesture progress |
| Android 13 and older | Back presses still reach React Native and `onBack` |
| iOS, web | No-op; iOS keeps its own swipe back |

JS stacks like `@react-navigation/stack` and windows like React Native's `Modal` aren't animated automatically. Drive them with `useBackGesture`.

## Documentation

**[API.md](API.md)** lists every prop, option and type, and explains how each gesture picks what to animate.

## Development

```sh
bun install
bun run example:android
```

[`example`](example) uses Expo Router, and [`example-react-navigation`](example-react-navigation) uses React Navigation's native stack.

## License

MIT
