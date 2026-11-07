import { StyleSheet, Text, View } from "react-native";

import { palette } from "../components/palette";

export default function FormSheet() {
    return (
        <View style={styles.sheet} testID="form-sheet">
            <Text style={styles.title}>Native sheet</Text>
            <Text style={styles.body}>
                A react-native-screens form sheet. Swipe back and it shrinks the
                way Material sheets do. Turn off “Material shrink” on the home
                screen to compare.
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    sheet: { backgroundColor: palette.surface, flex: 1, gap: 12, padding: 24 },
    title: { color: palette.text, fontSize: 22, fontWeight: "700" },
    body: { color: palette.muted, fontSize: 15, lineHeight: 21 },
});
