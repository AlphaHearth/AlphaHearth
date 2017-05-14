package info.hearthsim.brazier.utils;

import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.Player;
import info.hearthsim.brazier.game.Secret;
import info.hearthsim.brazier.game.minions.Minion;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Comparing to {@link TestAgent}, which executes test code statically,
 * {@code DynamicTestAgent} executes the code dynamically to test {@link Game#copy()}.
 *
 * @see TestAgent
 */
public class DynamicTestAgent extends TestAgent {
    private static final StackTraceElement[] EMPTY_STACK = new StackTraceElement[0];

    private final List<ScriptWrapper> scripts = new ArrayList<>();

    public DynamicTestAgent() {
        this(true);
    }

    public DynamicTestAgent(boolean player1First) {
        super(player1First);
    }

    /* Dynamic Test Methods */

    /**
     * Statically runs the given test {@code Script} and adds it to the script list for
     * future dynamic test.
     */
    private void add(Script script) {
        // script.exec();
        scripts.add(new ScriptWrapper(script));
    }

    /**
     * Executes the script added to this agent to test {@link Game#copy()}.
     */
    public void execScripts() {
        if (scripts.isEmpty())
            return;
        // execScript(scripts.get(0));
        // The first element is ignored,
        // as such scene has already been tested statically.
        Game currentGame = super.getGame();
        for (int p1 = 0; p1 < scripts.size(); p1++) {
            StackTraceElement branchPoint = scripts.get(p1).invokePoint;
            Game copiedGame = currentGame.copy();
            super.setGame(copiedGame);
            for (int p2 = p1; p2 < scripts.size(); p2++) {
                ScriptWrapper script = scripts.get(p2);
                try {
                    script.exec();
                } catch (Throwable thr) {
                    AssertionError err = new AssertionError("Error occurred in iter #" + p1
                        + " when executing script from "
                        + printInvokePoint(script.invokePoint)
                        + ", branched before "
                        + printInvokePoint(branchPoint),
                        thr);
                    err.setStackTrace(EMPTY_STACK);
                    throw err;
                }
            }
            super.setGame(currentGame);
            execScript(scripts.get(p1));
        }
    }

    /**
     * Executes the given {@link ScriptWrapper} and throws a meaningful {@code AssertionError}
     * if any {@code Throwable} is thrown in the {@link ScriptWrapper#exec()} method.
     */
    private void execScript(ScriptWrapper script) {
        try {
            script.exec();
        } catch (Throwable thr) {
            Error err = new AssertionError("Error occurred when executing script from "
                + printInvokePoint(script.invokePoint), thr);
            err.setStackTrace(EMPTY_STACK);
            throw err;
        }
    }

    /**
     * Extracts useful information from the given {@link StackTraceElement} and
     * returns as {@code String}.
     */
    private String printInvokePoint(StackTraceElement invokePoint) {
        if (invokePoint == null)
            return "(Unknown location)";
        return String.format("(%s:%d)", invokePoint.getFileName(), invokePoint.getLineNumber());
    }

    /* Game Play Methods */

    /**
     * Sets the current player to the player with the given name
     */
    public void setCurrentPlayer(String playerName) {
        add(() -> super.setCurrentPlayer(playerName));
    }

    /**
     * Ends the current turn.
     */
    public void endTurn() {
        add(super::endTurn);
    }

    public void playMinionCard(String playerName, int cardIndex, int minionPos) {
        playMinionCard(playerName, cardIndex, minionPos, "");
    }

    public void playCard(String playerName, int cardIndex, String target) {
        add(() -> super.playCard(playerName, cardIndex, target));
    }

    public void playCard(String playerName, int cardIndex) {
        add(() -> super.playCard(playerName, cardIndex));
    }

    public void playMinionCard(String playerName, int cardIndex, int minionPos, String target) {
        add(() -> super.playMinionCard(playerName, cardIndex, minionPos, target));
    }

    /**
     * Designates the given player to play the given minion card on the given location.
     *
     * @param playerName the name of the player
     * @param cardName   the name of the minion card to be played
     * @param minionPos  the location on which the minion is to be placed
     * @throws IllegalArgumentException if {@code minionPos < 0}.
     */
    public void playMinionCard(String playerName, String cardName, int minionPos) {
        playMinionCard(playerName, cardName, minionPos, "");
    }

    /**
     * Designates the given player to play the given non minion card, with a specific target.
     *
     * @param playerName the name of the player
     * @param cardName   the name of the card.
     * @param target     the name of the target; {@code ""} for no target.
     */
    public void playNonMinionCard(String playerName, String cardName, String target) {
        add(() -> super.playNonMinionCard(playerName, cardName, target));
    }

    /**
     * Designates the given player to play the specific card
     *
     * @param playerName the name of the player
     * @param cardName   the name of the card
     */
    public void playCard(String playerName, String cardName) {
        add(() -> super.playCard(playerName, cardName));
    }

    /**
     * Designates the given player to play the specific minion card with a specific minion position
     * and target.
     *
     * @param playerName the name of the player.
     * @param cardName   the name of the minion card.
     * @param minionPos  the position the minion should be put.
     * @param target     the name of the target; {@code ""} (empty string) for no target.
     * @throws IllegalArgumentException if {@code minionPos < 0}.
     */
    public void playMinionCard(String playerName, String cardName, int minionPos, String target) {
        add(() -> super.playMinionCard(playerName, cardName, minionPos, target));
    }

    /**
     * Puts the cards with the given names to the deck of the player with the given name.
     */
    public void deck(String playerName, String... cardNames) {
        add(() -> super.deck(playerName, cardNames));
    }

    /**
     * Adds the cards with the given names to the hand of the player with the given name.
     */
    public void addToHand(String playerName, String... cardNames) {
        add(() -> super.addToHand(playerName, cardNames));
    }

    /**
     * Sets the health and armor point of the player with the given name to the given values.
     */
    public void setHeroHp(String playerName, int hp, int armor) {
        add(() -> super.setHeroHp(playerName, hp, armor));
    }

    /**
     * Sets the current mana of the player with the given name.
     */
    public void setMana(String playerName, int mana) {
        add(() -> super.setMana(playerName, mana));
    }

    /**
     * Refreshes the attack of characters of both players.
     */
    public void refreshAttacks() {
        add(super::refreshAttacks);
    }

    public void addRoll(int possibilityCount, int rollResult) {
        add(() -> super.addRoll(possibilityCount, rollResult));
    }

    /** Attacks the target with the given name with the attacker with the given name. */
    public void attack(String attacker, String target) {
        add(() -> super.attack(attacker, target));
    }

    public void addCardChoice(int choiceIndex, String... cardNames) {
        add(() -> super.addCardChoice(choiceIndex, cardNames));
    }

    /**
     * Decreases the mana cost of all the cards in the hand of the player with the given name
     * by {@code 1}.
     */
    public void decreaseManaCostOfHand(String playerName) {
        add(() -> super.decreaseManaCostOfHand(playerName));
    }

    /* Expect Methods */

    /**
     * Uses the given {@link Consumer} of {@link Player} to check the current state of the
     * given player.
     *
     * @param playerName the name of the player to be checked.
     * @param check      the test script, usually designated by a lambda expression.
     */
    public void expectPlayer(String playerName, Consumer<? super Player> check) {
        add(() -> super.expectPlayer(playerName, check));
    }

    /**
     * Expects the {@link Minion} with the given name by using the given {@link Consumer}
     * of {@code Minion}.
     *
     * @throws AssertionError if there is no such minion.
     */
    public void expectMinion(String target, Consumer<? super Minion> check) {
        add(() -> super.expectMinion(target, check));
    }

    /**
     * Expects the game does not end yet.
     */
    public void expectGameContinues() {
        add(super::expectGameContinues);
    }

    /**
     * Expects the game is over and players with the given names are dead.
     */
    public void expectHeroDeath(String... expectedDeadPlayerNames) {
        add(() -> super.expectHeroDeath(expectedDeadPlayerNames));
    }

    /**
     * Expects the deck of the player with the given name has and only has the cards with the given names.
     */
    public void expectDeck(String playerName, String... cardNames) {
        add(() -> super.expectDeck(playerName, cardNames));
    }

    /**
     * Expects the list of {@link Secret}s the player with the given name hae being exactly
     * same as the given array of secret names.
     */
    public void expectSecret(String playerName, String... secretNames) {
        add(() -> super.expectSecret(playerName, secretNames));
    }

    /**
     * Expects the hand of the player with the given name having the exact cards as the
     * given array of card names.
     */
    public void expectHand(String playerName, String... cardNames) {
        add(() -> super.expectHand(playerName, cardNames));
    }

    /**
     * Expects the player with the given name having the given amount of mana left.
     */
    public void expectMana(String playerName, int expectedMana) {
        add(() -> super.expectMana(playerName, expectedMana));
    }

    /**
     * Expects the player with the given name having the given amount of health and aromr point.
     */
    public void expectHeroHp(String playerName, int expectedHp, int expectedArmor) {
        add(() -> super.expectHeroHp(playerName, expectedHp, expectedArmor));
    }

    /**
     * Expects the player with the given name having given amount of attack and no weapon.
     */
    public void expectNoWeapon(String playerName, int attack) {
        add(() -> super.expectNoWeapon(playerName, attack));
    }

    /**
     * Expects the player with the given name having a weapon with the given amount of attack and durability.
     */
    public void expectWeapon(String playerName, int expectedAttack, int expectedDurability) {
        add(() -> super.expectWeapon(playerName, expectedAttack, expectedDurability));
    }

    /**
     * Expects the minions of the player with the given name satisfying the given array of
     * {@link MinionExpectations}.
     */
    public void expectBoard(String playerName, MinionExpectations... minionDescrs) {
        add(() -> super.expectBoard(playerName, minionDescrs));
    }

    private static final class ScriptWrapper {
        private StackTraceElement invokePoint = null;
        private final Script script;

        ScriptWrapper(Script script) {
            this.script = script;
            Throwable thr = new Throwable();
            StackTraceElement[] stackTrace = thr.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (!element.getClassName().startsWith("info.hearthsim.brazier.utils")) {
                    if (!element.getClassName().startsWith("info.hearthsim.brazier")) {
                        RuntimeException ex = new IllegalStateException("Adding dynamic script from other project!", thr);
                        ex.setStackTrace(EMPTY_STACK);
                        throw ex;
                    }
                    try {
                        Class testClass = Class.forName(element.getClassName());
                        Method testMethod = testClass.getMethod(element.getMethodName());
                        if (testMethod.isAnnotationPresent(Test.class)) {
                            this.invokePoint = element;
                            break;
                        }
                    } catch (ClassNotFoundException | NoSuchMethodException ex) {
                        // Ignore
                        // TODO Log it as warning message
                    }
                }
            }
        }

        public void exec() {
            script.exec();
        }
    }

    @FunctionalInterface
    private interface Script {
        public void exec();
    }
}
