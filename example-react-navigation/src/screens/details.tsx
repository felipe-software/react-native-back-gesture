import { useLayoutEffect } from "react";
import { StyleSheet, Text, View } from "react-native";

import { palette } from "../components/palette";
import { Row } from "../components/row";
import { Section } from "../components/section";
import type { RootScreenProps } from "../navigation";

export function Details({ navigation, route }: RootScreenProps<"Details">) {
    const { depth } = route.params;

    useLayoutEffect(() => {
        navigation.setOptions({ title: `Screen ${depth}` });
    }, [depth, navigation]);

    return (
        <View style={styles.screen}>
            <Text style={styles.depth} testID="screen-depth">
                {depth}
            </Text>
            <Section title="Next">
                <Row
                    detail="Push one more screen onto the stack"
                    onPress={() =>
                        navigation.push("Details", { depth: depth + 1 })
                    }
                    testID="push-deeper"
                    title={`Push screen ${depth + 1}`}
                />
            </Section>
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
    depth: {
        color: palette.accent,
        fontSize: 96,
        fontWeight: "700",
        paddingHorizontal: 20,
    },
});
