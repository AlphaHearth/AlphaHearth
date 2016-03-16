package info.hearthsim.brazier;

import info.hearthsim.brazier.abilities.ActiveAura;
import info.hearthsim.brazier.abilities.ActiveAuraList;
import info.hearthsim.brazier.actions.AttackRequest;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.weapons.AttackTool;
import info.hearthsim.brazier.weapons.Weapon;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jtrim.utils.ExceptionHelper;

/**
 * An instance of {@code Game} is essentially a hearthstone game between two players.
 */
public final class Game {
    private static final Random RNG = new SecureRandom();
    private static final RandomProvider DEFAULT_RANDOM_PROVIDER =
        (int bound) -> bound > 1 ? RNG.nextInt(bound) : 0;

    private RandomProvider randomProvider;
    private UserAgent userAgent;
    private final HearthStoneDb db;
    private final Player player1;
    private final Player player2;
    private GameResult gameResult;

    private final ActiveAuraList activeAuras;

    private final GameEvents events;

    private final AtomicLong currentTime;

    private Player currentPlayer;

    /**
     * Constructs an instance of {@code Game} with the given two players and hearthstone database
     *
     * @param db the hearthstone database
     * @param player1Id the id of player 1
     * @param player2Id the id of player 2
     */
    public Game(HearthStoneDb db, PlayerId player1Id, PlayerId player2Id) {
        ExceptionHelper.checkNotNullArgument(db, "db");

        this.db = db;
        this.currentTime = new AtomicLong(Long.MIN_VALUE);
        this.player1 = new Player(this, player1Id);
        this.player2 = new Player(this, player2Id);
        this.activeAuras = new ActiveAuraList();
        this.gameResult = null;

        this.events = new GameEvents(this);
        this.randomProvider = DEFAULT_RANDOM_PROVIDER;
        this.currentPlayer = player1;

        this.userAgent = (boolean allowCancel, List<? extends CardDescr> cards) -> {
            return cards.get(randomProvider.roll(cards.size()));
        };
    }

    /**
     * Creates a copy of the given {@code Game}.
     */
    private Game(Game other) {
        ExceptionHelper.checkNotNullArgument(other, "other");

        this.db = other.db;
        this.currentTime = new AtomicLong(other.currentTime.longValue());
        this.player1 = other.player1.copyFor(this);
        this.player2 = other.player2.copyFor(this);
        this.activeAuras = other.activeAuras.copy();
        this.gameResult = other.gameResult;

        this.events = new GameEvents(this); // FIXME

        this.randomProvider = other.randomProvider;
        PlayerId curPlayerId = other.currentPlayer.getPlayerId();
        if (curPlayerId == player1.getPlayerId())
            this.currentPlayer = player1;
        else if (curPlayerId == player2.getPlayerId())
            this.currentPlayer = player2;
        else
            throw new AssertionError();

        this.userAgent = other.userAgent;
    }

    public HearthStoneDb getDb() {
        return db;
    }

    public void setRandomProvider(RandomProvider randomProvider) {
        ExceptionHelper.checkNotNullArgument(randomProvider, "randomProvider");
        // We wrap the random provider to avoid generating a random number
        // when there is only one possibility. This helps test code and simplifies
        // AI.
        this.randomProvider = (bound) -> bound > 1 ? randomProvider.roll(bound) : 0;
    }

    public RandomProvider getRandomProvider() {
        return randomProvider;
    }

    public long getCurrentTime() {
        return currentTime.getAndIncrement();
    }

    public boolean isGameOver() {
        return gameResult != null;
    }

    /**
     * Returns the {@code GameResult}; {@code null} if the game is not over yet.
     */
    public GameResult tryGetGameResult() {
        return gameResult;
    }

    /**
     * Updates the {@code gameResult} property of this {@code Game} based on the
     * current state of both heroes. If any hero is dead, the property will be updated;
     * otherwise, nothing will be done.
     */
    private UndoAction updateGameOverState() {
        if (gameResult != null) {
            // Once the game is over, we cannot change the result.
            return UndoAction.DO_NOTHING;
        }

        boolean player1Dead = player1.getHero().isDead();
        boolean player2Dead = player2.getHero().isDead();
        if (!player1Dead && !player2Dead) {
            return UndoAction.DO_NOTHING;
        }

        List<PlayerId> deadPlayers = new ArrayList<>(2);
        if (player1Dead) {
            deadPlayers.add(player1.getPlayerId());
        }
        if (player2Dead) {
            deadPlayers.add(player2.getPlayerId());
        }

        this.gameResult = new GameResult(deadPlayers);
        return () -> gameResult = null;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public UndoAction setCurrentPlayerId(PlayerId newPlayerId) {
        ExceptionHelper.checkNotNullArgument(newPlayerId, "newPlayerId");

        Player prevPlayer = currentPlayer;
        currentPlayer = getPlayer(newPlayerId);
        return () -> currentPlayer = prevPlayer;
    }

    /**
     * Ends the current turn.
     */
    public UndoAction endTurn() {
        UndoAction.Builder result = new UndoAction.Builder();

        result.addUndo(currentPlayer.endTurn());

        Player nextPlayer = getOpponent(currentPlayer.getPlayerId());
        Player origCurrentPlayer = currentPlayer;
        currentPlayer = nextPlayer;
        result.addUndo(() -> currentPlayer = origCurrentPlayer);

        result.addUndo(nextPlayer.startNewTurn());

        return result;
    }

    /**
     * Returns the {@code Character} with the given {@code TargetId}.
     *
     * @param target the {@code TargetId} of the wanted target.
     * @return the {@code Character}.
     */
    public Character findTarget(TargetId target) {
        if (target == null) {
            return null;
        }

        Hero hero1 = player1.getHero();
        if (target.equals(hero1.getTargetId())) {
            return hero1;
        }

        Hero hero2 = player2.getHero();
        if (target.equals(hero2.getTargetId())) {
            return hero2;
        }

        Minion result = player1.getBoard().findMinion(target);
        if (result != null) {
            return result;
        }

        return player2.getBoard().findMinion(target);
    }

    /**
     * Returns all {@code Character}s in this {@code Game}, including
     * both {@link Hero}s and all {@link Minion}s.
     */
    public List<Character> getTargets() {
        BoardSide player1Minions = player1.getBoard();
        BoardSide player2Minions = player2.getBoard();
        List<Character> targets =
            new ArrayList<>(player1Minions.getMinionCount() + player2Minions.getMinionCount() + 2);
        targets.add(player1.getHero());
        targets.add(player2.getHero());
        player1Minions.collectMinions(targets);
        player2Minions.collectMinions(targets);

        return targets;
    }

    /**
     * Returns all {@code Character}s in this {@code Game} which satisfy the given {@link Predicate},
     * including {@link Hero}s and {@link Minion}s.
     */
    public List<Character> getTargets(Predicate<? super Character> filter) {
        ExceptionHelper.checkNotNullArgument(filter, "filter");

        return getTargets().stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Returns if the given target is still on the board.
     */
    private boolean isTargetExist(Character character) {
        return findTarget(character.getTargetId()) != null;
    }

    public UndoAction attack(TargetId attackerId, TargetId defenderId) {
        ExceptionHelper.checkNotNullArgument(attackerId, "attackerId");
        ExceptionHelper.checkNotNullArgument(defenderId, "defenderId");

        Character attacker = findTarget(attackerId);
        if (attacker == null) {
            return UndoAction.DO_NOTHING;
        }

        Character defender = findTarget(defenderId);
        if (defender == null) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction.Builder result = new UndoAction.Builder();

        AttackRequest attackRequest = new AttackRequest(attacker, defender);
        result.addUndo(events.triggerEventNow(SimpleEventType.ATTACK_INITIATED, attackRequest));

        result.addUndo(resolveDeaths());
        if (isGameOver()) {
            return result;
        }

        Character newDefender = attackRequest.getTarget();
        if (newDefender == null) {
            return result;
        }

        if (!isTargetExist(attacker) || !isTargetExist(newDefender)) {
            return result;
        }

        // We request all this info prior attacking, because we do not want
        // damage triggers to alter these values.
        AttackTool attackTool = attacker.getAttackTool();
        int attack = attackTool.getAttack();
        boolean swipLeft = attackTool.attacksLeft();
        boolean swipeRight = attackTool.attacksRight();

        UndoAction attackUndo = events.doAtomic(() -> resolveAttackNonAtomic(attacker, newDefender));
        result.addUndo(attackUndo);

        if (swipLeft || swipeRight) {
            result.addUndo(doSwipeAttack(attack, swipLeft, swipeRight, attacker, newDefender));
        }

        return result;
    }

    private static UndoAction doSwipeAttack(
            int attack,
            boolean swipeLeft,
            boolean swipeRight,
            Character attacker,
            Character target) {
        if (!(target instanceof Minion)) {
            return UndoAction.DO_NOTHING;
        }
        Minion minionTarget = (Minion) target;
        BoardSide targetBoard = minionTarget.getOwner().getBoard();
        int targetLoc = targetBoard.indexOf(minionTarget.getTargetId());

        UndoAction.Builder builder = new UndoAction.Builder();
        UndoableResult<Damage> damageRef = attacker.createDamage(attack);
        builder.addUndo(damageRef.getUndoAction());

        if (swipeLeft && targetLoc > 0)
            builder.addUndo(targetBoard.getMinion(targetLoc - 1).damage(damageRef.getResult()));

        if (swipeRight && targetLoc < targetBoard.getMaxSize() - 1)
            builder.addUndo(targetBoard.getMinion(targetLoc + 1).damage(damageRef.getResult()));

        return builder;
    }

    private static UndoAction dealDamage(AttackTool weapon, Character attacker, Character target) {
        UndoableResult<Damage> damageRef = attacker.createDamage(weapon.getAttack());
        UndoAction damageUndo = target.damage(damageRef.getResult());
        UndoAction swingDecUndo = weapon.incAttackCount();
        return () -> {
            swingDecUndo.undo();
            damageUndo.undo();
            damageRef.undo();
        };
    }

    private UndoAction resolveAttackNonAtomic(Character attacker, Character defender) {
        AttackTool attackerWeapon = attacker.getAttackTool();
        if (!attackerWeapon.canAttackWith()) {
            throw new IllegalArgumentException("Attacker is not allowed to attack with its weapon.");
        }

        UndoAction attackUndo = dealDamage(attackerWeapon, attacker, defender);

        AttackTool defenderWeapon = defender.getAttackTool();
        UndoAction defendUndo;
        if (attackerWeapon.canTargetRetaliate()
                && defenderWeapon.canRetaliateWith()) {
            defendUndo = dealDamage(defenderWeapon, defender, attacker);
        }
        else {
            defendUndo = UndoAction.DO_NOTHING;
        }

        return () -> {
            defendUndo.undo();
            attackUndo.undo();
        };
    }

    private UndoableResult<List<Weapon>> removeDeadWeapons() {
        UndoableResult<Weapon> deadWeaponResult1 = player1.removeDeadWeapon();
        UndoableResult<Weapon> deadWeaponResult2 = player2.removeDeadWeapon();

        Weapon deadWeapon1 = deadWeaponResult1.getResult();
        Weapon deadWeapon2 = deadWeaponResult2.getResult();
        if (deadWeapon1 == null && deadWeapon2 == null) {
            return new UndoableResult<>(Collections.emptyList(), UndoAction.DO_NOTHING);
        }

        List<Weapon> result = new ArrayList<>(2);
        if (deadWeapon1 != null) {
            result.add(deadWeapon1);
        }
        if (deadWeapon2 != null) {
            result.add(deadWeapon2);
        }

        return new UndoableResult<>(result, () -> {
            deadWeaponResult2.undo();
            deadWeaponResult1.undo();
        });
    }

    public UndoableUnregisterAction addAura(ActiveAura aura) {
        return activeAuras.addAura(aura);
    }

    private UndoAction updateAllAuras() {
        UndoAction.Builder result = new UndoAction.Builder();
        result.addUndo(activeAuras.updateAllAura(this));
        result.addUndo(player1.updateAuras());
        result.addUndo(player2.updateAuras());
        return result;
    }

    public UndoAction endPhase() {
        UndoableResult<Boolean> deathResults = resolveDeaths();
        if (!deathResults.getResult()) {
            return deathResults;
        }

        UndoAction.Builder builder = new UndoAction.Builder();
        builder.addUndo(deathResults.getUndoAction());

        do {
            deathResults = resolveDeaths();
            builder.addUndo(deathResults.getUndoAction());
        } while (deathResults.getResult() && !isGameOver());

        return new UndoableResult<>(true, builder);
    }

    private UndoableResult<Boolean> resolveDeaths() {
        UndoAction auraUndo = updateAllAuras();
        UndoableResult<Boolean> deathResolution = resolveDeathsWithoutAura();
        return new UndoableResult<>(deathResolution.getResult(), () -> {
            deathResolution.undo();
            auraUndo.undo();
        });
    }

    private UndoableResult<Boolean> resolveDeathsWithoutAura() {
        UndoAction.Builder builder = new UndoAction.Builder();

        builder.addUndo(updateGameOverState());

        if (isGameOver()) {
            // We could finish the death-rattles but why would we?
            return new UndoableResult<>(false, builder);
        }

        List<Minion> deadMinions = new ArrayList<>();
        player1.getBoard().collectMinions(deadMinions, Minion::isDead);
        player2.getBoard().collectMinions(deadMinions, Minion::isDead);

        UndoableResult<List<Weapon>> deadWeaponsResult = removeDeadWeapons();
        builder.addUndo(deadWeaponsResult.getUndoAction());

        List<Weapon> deadWeapons = deadWeaponsResult.getResult();

        if (deadWeapons.isEmpty() && deadMinions.isEmpty()) {
            return new UndoableResult<>(false, builder);
        }

        List<DestroyableEntity> deadEntities = new ArrayList<>(deadWeapons.size() + deadMinions.size());
        for (Minion minion: deadMinions) {
            Graveyard graveyard = minion.getOwner().getGraveyard();
            builder.addUndo(graveyard.addDeadMinion(minion));

            deadEntities.add(minion);
        }

        for (Weapon weapon: deadWeapons) {
            deadEntities.add(weapon);
        }

        BornEntity.sortEntities(deadEntities);

        for (DestroyableEntity dead: deadEntities) {
            builder.addUndo(dead.scheduleToDestroy());
        }
        for (DestroyableEntity dead: deadEntities) {
            builder.addUndo(dead.destroy());
        }

        return new UndoableResult<>(true, builder);
    }

    public GameEvents getEvents() {
        return events;
    }

    public void setUserAgent(UserAgent userAgent) {
        ExceptionHelper.checkNotNullArgument(userAgent, "userAgent");
        this.userAgent = userAgent;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the opponent of the given player.
     *
     * @param playerId the {@code PlayerId} of the given player.
     * @return the opponent of the given player.
     *
     * @throws IllegalArgumentException if the given player is unknown
     *                                  (other than {@code player1} and {@code player2}).
     */
    public Player getOpponent(PlayerId playerId) {
        ExceptionHelper.checkNotNullArgument(playerId, "playerId");

        if (playerId.equals(player1.getPlayerId())) {
            return player2;
        }
        if (playerId.equals(player2.getPlayerId())) {
            return player1;
        }

        throw new IllegalArgumentException("Unknown player: " + playerId.getName());
    }

    /**
     * Gets {@code Player} based on the given {@code PlayerId}.
     *
     * @param playerId the given {@code PlayerId}.
     * @return the corresponding {@code Player}.
     *
     * @throws IllegalArgumentException if the given {@code PlayerId} is unknown
     *                                  (other than {@code player1} and {@code player2}).
     */
    public Player getPlayer(PlayerId playerId) {
        ExceptionHelper.checkNotNullArgument(playerId, "playerId");

        if (player1.getPlayerId().equals(playerId)) {
            return player1;
        }
        if (player2.getPlayerId().equals(playerId)) {
            return player2;
        }

        throw new IllegalArgumentException("Unknown player: " + playerId.getName());
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}
