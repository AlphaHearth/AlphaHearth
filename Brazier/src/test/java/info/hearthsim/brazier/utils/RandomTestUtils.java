package info.hearthsim.brazier.utils;

import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionId;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public final class RandomTestUtils {
    public static <T> T singleMinionScript(
        TestAgent agent,
        String minionLocation,
        Function<Minion, T> propertyGetter,
        Consumer<TestAgent> agentConfig) {
        AtomicReference<T> resultRef = new AtomicReference<>(null);

        agentConfig.accept(agent);

        agent.expectMinion(minionLocation, (minion) -> {
            T property = propertyGetter.apply(minion);
            T prevRef = resultRef.get();
            if (prevRef != null) {
                assertEquals("Expected same minion for all runs.", prevRef, property);
            } else {
                resultRef.set(property);
            }
        });

        return resultRef.get();
    }

    public static MinionId singleMinionScript(TestAgent agent, String minionLocation,
                                              Consumer<TestAgent> scriptConfig) {
        return singleMinionScript(agent, minionLocation,
            (minion) -> minion.getBaseDescr().getId(),
            scriptConfig);
    }

    public static List<MinionId> boardMinionScript(TestAgent agent, String playerName,
                                                   Consumer<TestAgent> scriptConfig) {
        AtomicReference<List<MinionId>> resultRef = new AtomicReference<>(null);

        scriptConfig.accept(agent);

        agent.expectPlayer(playerName, (player) -> {
            List<MinionId> minions = player.getBoard().getAllMinions().stream()
                .map((minion) -> minion.getBaseDescr().getId())
                .collect(Collectors.toList());

            List<MinionId> prevRef = resultRef.get();
            if (prevRef != null) {
                assertEquals("Expected same minion for all runs.", prevRef, minions);
            } else {
                resultRef.set(minions);
            }
        });

        return resultRef.get();
    }

    private RandomTestUtils() {
        throw new AssertionError();
    }
}
