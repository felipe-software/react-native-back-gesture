import { useRouter } from "expo-router";
import { ScrollView, StyleSheet } from "react-native";
import type { BackGestureStackAnimation } from "react-native-back-gesture";

import { useAnimationSettings } from "../animation-settings";
import { ChoiceRow } from "../components/choice-row";
import { palette } from "../components/palette";
import { Row } from "../components/row";
import { Section } from "../components/section";
import { SwitchRow } from "../components/switch-row";

const STACK_ANIMATIONS: {
    value: BackGestureStackAnimation;
    title: string;
    detail: string;
}[] = [
    {
        value: "slide",
        title: "Slide",
        detail: "Default. The screen slides away with your finger",
    },
    {
        value: "scale",
        title: "Scale",
        detail: "Android's shrink, with rounded corners",
    },
    {
        value: "none",
        title: "None",
        detail: "Back still works, without a gesture animation",
    },
];

export default function Home() {
    const router = useRouter();
    const {
        stackAnimation,
        sheetAnimation,
        setStackAnimation,
        toggleSheetAnimation,
    } = useAnimationSettings();

    return (
        <ScrollView
            contentContainerStyle={styles.content}
            style={styles.screen}
        >
            <Section title="Built-in animations">
                <Row
                    detail="Native stack screen, slides away as you swipe back"
                    onPress={() => router.push("/details/1")}
                    testID="push-screen"
                    title="Push a screen"
                />
                <Row
                    detail="react-native-screens form sheet"
                    onPress={() => router.push("/form-sheet")}
                    testID="open-form-sheet"
                    title="Open a native sheet"
                />
            </Section>
            <Section title="useBackGesture">
                <Row
                    detail="A Reanimated sheet driven by the gesture progress"
                    onPress={() => router.push("/custom-sheet")}
                    testID="open-custom-sheet-screen"
                    title="Custom sheet"
                />
            </Section>
            <Section title="Stack animation">
                {STACK_ANIMATIONS.map(({ value, title, detail }) => (
                    <ChoiceRow
                        detail={detail}
                        key={value}
                        onSelect={() => setStackAnimation(value)}
                        selected={stackAnimation === value}
                        testID={`stack-animation-${value}`}
                        title={title}
                    />
                ))}
            </Section>
            <Section title="Sheet animation">
                <SwitchRow
                    onToggle={toggleSheetAnimation}
                    testID="toggle-sheet-animation"
                    title="Material shrink"
                    value={sheetAnimation}
                />
            </Section>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    screen: { backgroundColor: palette.background },
    content: { gap: 24, paddingVertical: 24 },
});
