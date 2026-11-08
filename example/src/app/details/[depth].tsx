import { Stack, useLocalSearchParams, useRouter } from "expo-router";
import { StyleSheet, Text, View } from "react-native";

import { palette } from "../../components/palette";
import { Row } from "../../components/row";
import { Section } from "../../components/section";

export default function Details() {
    const router = useRouter();
    const { depth } = useLocalSearchParams<{ depth: string }>();
    const level = Number(depth);

    return (
        <View style={styles.screen}>
            <Stack.Screen options={{ title: `Screen ${level}` }} />
            <Text style={styles.depth} testID="screen-depth">
                {level}
            </Text>
            <Text style={styles.hint}>
                Swipe from either edge. The screen slides away with your finger;
                let go early to cancel.
            </Text>
            <Section title="Next">
                <Row
                    detail="Push one more screen onto the stack"
                    onPress={() => router.push(`/details/${level + 1}`)}
                    testID="push-deeper"
                    title={`Push screen ${level + 1}`}
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
    hint: {
        color: palette.muted,
        fontSize: 15,
        lineHeight: 21,
        paddingHorizontal: 20,
    },
});
