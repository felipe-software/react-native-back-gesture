import type { ReactNode } from "react";
import { StyleSheet, Text, View } from "react-native";

import { palette } from "./palette";

interface SectionProps {
    title: string;
    children: ReactNode;
}

export function Section({ title, children }: SectionProps) {
    return (
        <View style={styles.section}>
            <Text style={styles.title}>{title}</Text>
            <View style={styles.rows}>{children}</View>
        </View>
    );
}

const styles = StyleSheet.create({
    section: { gap: 8 },
    title: {
        color: palette.muted,
        fontSize: 13,
        fontWeight: "600",
        paddingHorizontal: 20,
        textTransform: "uppercase",
    },
    rows: {
        borderRadius: 16,
        gap: StyleSheet.hairlineWidth,
        marginHorizontal: 12,
        overflow: "hidden",
        backgroundColor: palette.border,
    },
});
