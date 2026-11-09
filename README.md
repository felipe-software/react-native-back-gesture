<img src="assets/icon.svg" width="64" height="64" alt="">

# react-native-back-gesture

[![npm version](https://img.shields.io/npm/v/react-native-back-gesture?style=flat-square)](https://www.npmjs.com/package/react-native-back-gesture) [![license](https://img.shields.io/npm/l/react-native-back-gesture?style=flat-square)](https://www.npmjs.com/package/react-native-back-gesture)
[![React Native >=0.76](https://img.shields.io/badge/React%20Native-%3E%3D0.76-61DAFB?style=flat-square&logo=react&logoColor=white)](https://reactnative.dev/) [![Reanimated 3–4](https://img.shields.io/badge/Reanimated-3%20%E2%80%93%204-8A2BE2?style=flat-square)](https://docs.swmansion.com/react-native-reanimated/) [![Runs with Expo](https://img.shields.io/badge/Runs%20with%20Expo-4630EB?style=flat-square&logo=expo&logoColor=white)](https://expo.dev/) [![Expo Router](https://img.shields.io/badge/Expo%20Router-compatible-000020?style=flat-square&logo=expo&logoColor=white)](https://docs.expo.dev/router/introduction/) [![React Navigation](https://img.shields.io/badge/React%20Navigation-native%20stack-6B52AE?style=flat-square)](https://reactnavigation.org/docs/native-stack-navigator)

## Installation

```sh
npm install react-native-back-gesture
```

Needs `react-native-reanimated`, `react-native-screens` and the New Architecture. Rebuild the native app afterwards, then opt in to predictive back:

```jsonc
// app.json (Expo)
{ "expo": { "android": { "predictiveBackGestureEnabled": true } } }
```

```xml
<!-- AndroidManifest.xml (bare React Native) -->
<application android:enableOnBackInvokedCallback="true">
```

## Usage

### Stack screens and native sheets

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

```tsx
// React Navigation
<BackGestureProvider>
    <NavigationContainer>{/* … */}</NavigationContainer>
</BackGestureProvider>
```

### Animations

```ts
type StackAnimation =
    | "slide"       // default: the top screen slides away
    | "scale"       // shrinks with the finger, like Android
    | "ios"         // the previous screen trails 30% behind
    | "card"        // the previous screen comes forward from behind
    | "shared-axis" // both screens shift and cross-fade
    | "none";
```

```tsx
<BackGestureProvider
    stackAnimation="ios"
    sheetAnimation={false} // animate native sheets yourself
>
    <Stack screenOptions={{ animation: "ios_from_right" }} />
</BackGestureProvider>
```

### Your own UI

```tsx
import Animated, { useAnimatedStyle } from "react-native-reanimated";
import { useBackGesture } from "react-native-back-gesture";

function Sheet({ open, onClose }: { open: boolean; onClose: () => void }) {
    const { progress, edge } = useBackGesture({
        enabled: open, // takes over back gestures while true
        onBack: onClose, // runs when the user commits the gesture
    });

    const style = useAnimatedStyle(() => {
        const direction = edge.value === "right" ? -1 : 1;
        return {
            transform: [
                { translateX: direction * progress.value * 12 },
                { scale: 1 - progress.value * 0.1 },
            ],
        };
    });

    return <Animated.View style={style}>{/* … */}</Animated.View>;
}
```

```tsx
// Only while this screen is on top
const focused = useIsFocused();
useBackGesture({ enabled: focused && menuOpen, onBack: closeMenu });
```

### Closing the app

```tsx
// Android's back-to-home animation on the home screen
useSystemBackAllowed(usePathname() === "/");
```

## Compatibility

| | Supported |
| --- | --- |
| React Native | `>=0.76`, New Architecture |
| Reanimated | `>=3.16 <5` |
| React Native Screens | `>=4.0` |
| Android 14+ | Animated with gesture progress |
| Android 13 and older | No animation; `onBack` still runs |
| iOS, web | No-op |

Full reference: **[API.md](API.md)**.

## Development

```sh
bun install
bun run example:android                  # Expo Router
bun run example-react-navigation:android # React Navigation
```

## License

MIT
