import { Pressable, StyleSheet, Text } from "react-native";

import { palette } from "./palette";

interface RowProps {
    title: string;
    detail: string;
    onPress: () => void;
    testID?: string;
}

export function Row({ title, detail, onPress, testID }: RowProps) {
    return (
        <Pressable
            accessibilityRole="button"
            android_ripple={{ color: palette.border }}
            onPress={onPress}
            style={styles.row}
            testID={testID}
        >
            <Text style={styles.title}>{title}</Text>
            <Text style={styles.detail}>{detail}</Text>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    row: {
        backgroundColor: palette.surface,
        paddingHorizontal: 20,
        paddingVertical: 16,
        gap: 4,
    },
    title: { color: palette.text, fontSize: 17, fontWeight: "600" },
    detail: { color: palette.muted, fontSize: 14 },
});
