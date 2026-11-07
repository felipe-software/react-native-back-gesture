import { StyleSheet, Switch, Text, View } from "react-native";

import { palette } from "./palette";

interface SwitchRowProps {
    title: string;
    value: boolean;
    onToggle: () => void;
    testID?: string;
}

export function SwitchRow({ title, value, onToggle, testID }: SwitchRowProps) {
    return (
        <View style={styles.row}>
            <Text style={styles.title}>{title}</Text>
            <Switch
                accessibilityLabel={title}
                onValueChange={onToggle}
                testID={testID}
                thumbColor={palette.surface}
                trackColor={{ false: palette.border, true: palette.accent }}
                value={value}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    row: {
        alignItems: "center",
        backgroundColor: palette.surface,
        flexDirection: "row",
        justifyContent: "space-between",
        paddingHorizontal: 20,
        paddingVertical: 12,
    },
    title: { color: palette.text, fontSize: 17 },
});
