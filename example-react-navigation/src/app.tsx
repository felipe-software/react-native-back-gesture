import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { StatusBar } from "expo-status-bar";
import { BackGestureProvider } from "react-native-back-gesture";

import type { RootStackParamList } from "./navigation";
import { Details } from "./screens/details";
import { FormSheet } from "./screens/form-sheet";
import { Home } from "./screens/home";

const Stack = createNativeStackNavigator<RootStackParamList>();

export function App() {
    return (
        <BackGestureProvider>
            <StatusBar style="dark" />
            <NavigationContainer>
                <Stack.Navigator>
                    <Stack.Screen
                        component={Home}
                        name="Home"
                        options={{ title: "React Navigation" }}
                    />
                    <Stack.Screen component={Details} name="Details" />
                    <Stack.Screen
                        component={FormSheet}
                        name="FormSheet"
                        options={{
                            presentation: "formSheet",
                            sheetAllowedDetents: [0.5],
                            headerShown: false,
                        }}
                    />
                </Stack.Navigator>
            </NavigationContainer>
        </BackGestureProvider>
    );
}
