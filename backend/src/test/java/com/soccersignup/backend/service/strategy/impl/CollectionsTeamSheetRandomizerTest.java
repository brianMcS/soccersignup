package com.soccersignup.backend.service.strategy.impl;

import com.soccersignup.backend.model.GameSlot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionsTeamSheetRandomizerTest {

    private final CollectionsTeamSheetRandomizer randomizer =
            new CollectionsTeamSheetRandomizer();

    @Test
    void randomizeReturnsASeparateListWithoutChangingTheInput() {
        GameSlot firstSlot = new GameSlot();
        GameSlot secondSlot = new GameSlot();
        List<GameSlot> slots = new ArrayList<>(List.of(firstSlot, secondSlot));

        List<GameSlot> randomizedSlots = randomizer.randomize(slots);

        assertThat(randomizedSlots)
                .isNotSameAs(slots)
                .containsExactlyInAnyOrder(firstSlot, secondSlot);
        assertThat(slots).containsExactly(firstSlot, secondSlot);
    }
}
