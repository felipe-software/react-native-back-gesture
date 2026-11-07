import { Pressable, StyleSheet, Text, View } from "react-native";

import { palette } from "./palette";

interface ChoiceRowProps {
    title: string;
    detail: string;
    selected: boolean;
    onSelect: () => void;
    testID?: string;
}

export function ChoiceRow({
    title,
    detail,
    selected,
    onSelect,
    testID,
}: ChoiceRowProps) {
    return (
        <Pressable
            accessibilityRole="radio"
            accessibilityState={{ checked: selected }}
            android_ripple={{ color: palette.border }}
            onPress={onSelect}
            style={styles.row}
            testID={testID}
        >
            <View style={styles.text}>
                <Text style={styles.title}>{title}</Text>
                <Text style={styles.detail}>{detail}</Text>
            </View>
            <View style={[styles.radio, selected && styles.radioSelected]}>
                {selected ? <View style={styles.dot} /> : null}
            </View>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    row: {
        alignItems: "center",
        backgroundColor: palette.surface,
        flexDirection: "row",
        gap: 16,
        paddingHorizontal: 20,
        paddingVertical: 16,
    },
    text: { flex: 1, gap: 4 },
    title: { color: palette.text, fontSize: 17, fontWeight: "600" },
    detail: { color: palette.muted, fontSize: 14 },
    radio: {
        alignItems: "center",
        borderColor: palette.border,
        borderRadius: 11,
        borderWidth: 2,
        height: 22,
        justifyContent: "center",
        width: 22,
    },
    radioSelected: { borderColor: palette.accent },
    dot: {
        backgroundColor: palette.accent,
        borderRadius: 5,
        height: 10,
        width: 10,
    },
});
