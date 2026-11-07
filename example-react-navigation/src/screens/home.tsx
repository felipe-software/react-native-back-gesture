import { useIsFocused } from "@react-navigation/native";
import { useState } from "react";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { useSystemBackAllowed } from "react-native-back-gesture";

import { CustomSheet } from "../components/custom-sheet";
import { palette } from "../components/palette";
import { Row } from "../components/row";
import { Section } from "../components/section";
import type { RootScreenProps } from "../navigation";

export function Home({ navigation }: RootScreenProps<"Home">) {
    const focused = useIsFocused();
    const [sheetOpen, setSheetOpen] = useState(false);
    useSystemBackAllowed(focused);

    return (
        <View style={styles.screen}>
            <ScrollView contentContainerStyle={styles.content}>
                <Section title="Built-in animations">
                    <Row
                        detail="Native stack screen, slides away as you swipe back"
                        onPress={() => navigation.push("Details", { depth: 1 })}
                        testID="push-screen"
                        title="Push a screen"
                    />
                    <Row
                        detail="react-native-screens form sheet"
                        onPress={() => navigation.navigate("FormSheet")}
                        testID="open-form-sheet"
                        title="Open a native sheet"
                    />
                </Section>
                <Section title="useBackGesture">
                    <Row
                        detail="A Reanimated sheet on this screen, closed by back"
                        onPress={() => setSheetOpen(true)}
                        testID="open-custom-sheet"
                        title="Open the custom sheet"
                    />
                </Section>
            </ScrollView>
            <CustomSheet onClose={() => setSheetOpen(false)} open={sheetOpen}>
                <Text style={styles.title}>Custom sheet</Text>
                <Text style={styles.body}>
                    Back closes this sheet. With it closed, back on this screen
                    plays the system back-to-home animation.
                </Text>
            </CustomSheet>
        </View>
    );
}

const styles = StyleSheet.create({
    screen: { backgroundColor: palette.background, flex: 1 },
    content: { gap: 24, paddingVertical: 24 },
    title: { color: palette.text, fontSize: 22, fontWeight: "700" },
    body: { color: palette.muted, fontSize: 15, lineHeight: 21 },
});
