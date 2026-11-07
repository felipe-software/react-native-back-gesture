import { useState } from "react";
import { StyleSheet, Text, View } from "react-native";

import { CustomSheet } from "../components/custom-sheet";
import { palette } from "../components/palette";
import { Row } from "../components/row";
import { Section } from "../components/section";

export default function CustomSheetScreen() {
    const [open, setOpen] = useState(false);
    const [closeCount, setCloseCount] = useState(0);

    return (
        <View style={styles.screen}>
            <Section title="Sheet">
                <Row
                    detail="While it is open, back gestures close it instead of this screen"
                    onPress={() => setOpen(true)}
                    testID="open-custom-sheet"
                    title="Open the sheet"
                />
            </Section>
            <Text style={styles.hint} testID="close-count">
                Times closed: {closeCount}
            </Text>
            <CustomSheet
                onClose={() => {
                    setOpen(false);
                    setCloseCount((count) => count + 1);
                }}
                open={open}
            >
                <Text style={styles.title}>Custom sheet</Text>
                <Text style={styles.body}>
                    Drawn with Reanimated. useBackGesture hands it the gesture
                    progress, so it shrinks and moves with your finger.
                </Text>
            </CustomSheet>
        </View>
    );
}

const styles = StyleSheet.create({
    screen: {
        backgroundColor: palette.background,
        flex: 1,
        gap: 24,
        paddingVertical: 24,
    },
    hint: { color: palette.muted, fontSize: 15, paddingHorizontal: 20 },
    title: { color: palette.text, fontSize: 22, fontWeight: "700" },
    body: { color: palette.muted, fontSize: 15, lineHeight: 21 },
});
