package info.hearthsim.brazier.game;

import info.hearthsim.brazier.db.HearthStoneDb;
import info.hearthsim.brazier.RandomProvider;
import info.hearthsim.brazier.abilities.ActiveAura;
import info.hearthsim.brazier.abilities.ActiveAuraList;
import info.hearthsim.brazier.actions.AttackRequest;
import info.hearthsim.brazier.util.UndoAction;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.weapons.AttackTool;
import info.hearthsim.brazier.game.weapons.Weapon;

import java.lang.*;
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
public final class Game implements GameProperty {
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
        this.activeAuras = new ActiveAuraList(this);
        this.gameResult = null;

        this.events = new GameEvents(this);
        this.randomProvider = DEFAULT_RANDOM_PROVIDER;
        this.currentPlayer = player1;

        this.userAgent = (boolean allowCancel, List<? extends CardDescr> cards) ->
            cards.get(randomProvider.roll(cards.size()));
    }

    /**
     * Creates a copy of the given {@code Game}.
     */
    private Game(Game other) {
        ExceptionHelper.checkNotNullArgument(other, "other");

        this.db = other.db;
        this.currentTime = new AtomicLong(other.currentTime.longValue());
        this.events = other.events.copyFor(this);
        this.activeAuras = other.activeAuras.copyFor(this);
        this.player1 = other.player1.copyFor(this);
        this.player2 = other.player2.copyFor(this);
        this.gameResult = other.gameResult;

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

    /**
     * Returns a copy of this {@code Game}.
     */
    public Game copy() {
        Game copiedGame = new Game(this);
        copiedGame.updateAllAuras();
        return copiedGame;
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
    private void updateGameOverState() {
        if (gameResult != null)
            return;

        boolean player1Dead = player1.getHero().isDead();
        boolean player2Dead = player2.getHero().isDead();
        if (!player1Dead && !player2Dead)
            return;

        List<PlayerId> deadPlayers = new ArrayList<>(2);
        if (player1Dead) {
            deadPlayers.add(player1.getPlayerId());
        }
        if (player2Dead) {
            deadPlayers.add(player2.getPlayerId());
        }

        this.gameResult = new GameResult(deadPlayers);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayerId(PlayerId newPlayerId) {
        ExceptionHelper.checkNotNullArgument(newPlayerId, "newPlayerId");

        currentPlayer = getPlayer(newPlayerId);
    }

    /**
     * Ends the current turn.
     */
    public void endTurn() {
        currentPlayer.endTurn();
        updateAllAuras();
        currentPlayer = getOpponent(currentPlayer.getPlayerId());
        currentPlayer.startNewTurn();
        updateGameOverState();
    }

    public Hero getHero(EntityId id) {
        Hero hero = player1.getHero();
        if (hero.getEntityId() == id)
            return hero;

        hero = player2.getHero();
        if (hero.getEntityId() == id)
            return hero;

        return null;
    }

    public Minion getMinion(EntityId id) {
        Minion result = getLivingMinion(id);
        if (result != null) {
            return result;
        }

        return getDeadMinion(id);
    }

    public Minion getLivingMinion(EntityId id) {
        Minion result = player1.getBoard().findMinion(id);
        if (result != null) {
            return result;
        }

        return player2.getBoard().findMinion(id);
    }

    public Minion getDeadMinion(EntityId id) {
        Minion result = player1.getGraveyard().findMinion(id);
        if (result != null) {
            return result;
        }

        return player2.getGraveyard().findMinion(id);
    }

    public Character getCharacter(EntityId id) {
        Character result = getHero(id);
        if (result != null)
            return result;

        return getMinion(id);
    }

    public Weapon getWeapon(EntityId id) {
        Weapon weapon = player1.tryGetWeapon();
        if (weapon != null && weapon.getEntityId() == id)
            return weapon;
        weapon = player2.tryGetWeapon();
        if (weapon != null && weapon.getEntityId() == id)
            return weapon;
        return null;
    }

    public Card getCard(EntityId id) {
        Card result = player1.getHand().findCard((card) -> card.getEntityId() == id);
        if (result != null)
            return result;
        result = player2.getHand().findCard((card) -> card.getEntityId() == id);
        if (result != null)
            return result;
        List<Card> deckResult = player1.getDeck().getCards((card) -> card.getEntityId() == id);
        if (!deckResult.isEmpty())
            return deckResult.get(0);
        deckResult = player2.getDeck().getCards((card) -> card.getEntityId() == id);
        if (!deckResult.isEmpty())
            return deckResult.get(0);
        return null;
    }

    public Secret getSecret(EntityId id) {
        Secret result = player1.getSecrets().findById(id);
        if (result != null)
            return result;
        result = player2.getSecrets().findById(id);
        if (result != null)
            return result;
        return null;
    }

    /**
     * Returns the {@code Entity} with the given {@code EntityId}.
     */
    public Entity findEntity(EntityId id) {
        Entity result = getCharacter(id);
        if (result != null)
            return result;

        result = getSecret(id);
        if (result != null)
            return result;

        result = getWeapon(id);
        if (result != null)
            return result;

        result = getCard(id);
        if (result != null)
            return result;

        if (player1.getEntityId() == id)
            return player1;
        if (player2.getEntityId() == id)
            return player2;

        return null;
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
        Hero hero = getHero(character.getEntityId());
        Minion minion = getLivingMinion(character.getEntityId());
        return hero != null || minion != null;
    }

    public void attack(EntityId attackerId, EntityId defenderId) {
        ExceptionHelper.checkNotNullArgument(attackerId, "attackerId");
        ExceptionHelper.checkNotNullArgument(defenderId, "defenderId");

        Character attacker = getCharacter(attackerId);
        if (attacker == null)
            throw new IllegalArgumentException("Cannot find attacker for the given EntityId "
                + attackerId);

        Character defender = getCharacter(defenderId);
        if (defender == null)
            throw new IllegalArgumentException("Cannot find defender for the given EntityId "
                + defenderId);

        AttackRequest attackRequest = new AttackRequest(attacker, defender);
        events.triggerEventNow(SimpleEventType.ATTACK_INITIATED, attackRequest);

        resolveDeaths();
        if (isGameOver())
            return;

        Character newDefender = attackRequest.getTarget();
        if (newDefender == null)
            return;

        if (!isTargetExist(attacker) || !isTargetExist(newDefender))
            return;

        // We request all this info prior attacking, because we do not want
        // damage triggers to alter these values.
        AttackTool attackTool = attacker.getAttackTool();
        int attack = attackTool.getAttack();
        boolean swipLeft = attackTool.attacksLeft();
        boolean swipeRight = attackTool.attacksRight();

        events.doAtomic(() -> resolveAttackNonAtomic(attacker, newDefender));

        if (swipLeft || swipeRight)
            doSwipeAttack(attack, swipLeft, swipeRight, attacker, newDefender);
    }

    private static void doSwipeAttack(
            int attack,
            boolean swipeLeft,
            boolean swipeRight,
            Character attacker,
            Character target) {
        if (!(target instanceof Minion))
            return;

        Minion minionTarget = (Minion) target;
        BoardSide targetBoard = minionTarget.getOwner().getBoard();
        int targetLoc = targetBoard.indexOf(minionTarget.getEntityId());

        Damage damage = attacker.createDamage(attack);

        if (swipeLeft && targetLoc > 0)
            targetBoard.getMinion(targetLoc - 1).damage(damage);

        if (swipeRight && targetLoc < targetBoard.getMaxSize() - 1)
            targetBoard.getMinion(targetLoc + 1).damage(damage);
    }

    private static void dealDamage(AttackTool weapon, Character attacker, Character target) {
        Damage damage = attacker.createDamage(weapon.getAttack());
        target.damage(damage);
        weapon.incAttackCount();
    }

    private void resolveAttackNonAtomic(Character attacker, Character defender) {
        AttackTool attackerWeapon = attacker.getAttackTool();
        if (!attackerWeapon.canAttackWith()) {
            throw new IllegalStateException("Attacker is not allowed to attack with its weapon.");
        }

        AttackTool defenderWeapon = defender.getAttackTool();
        if (attackerWeapon.canTargetRetaliate()
                && defenderWeapon.canRetaliateWith()) {
            dealDamage(defenderWeapon, defender, attacker);
        }
        dealDamage(attackerWeapon, attacker, defender);
    }

    private List<Weapon> removeDeadWeapons() {
        Weapon deadWeapon1 = player1.removeDeadWeapon();
        Weapon deadWeapon2 = player2.removeDeadWeapon();

        if (deadWeapon1 == null && deadWeapon2 == null) {
            return Collections.emptyList();
        }

        List<Weapon> result = new ArrayList<>(2);
        if (deadWeapon1 != null) {
            result.add(deadWeapon1);
        }
        if (deadWeapon2 != null) {
            result.add(deadWeapon2);
        }

        return result;
    }

    public UndoAction<Game> addAura(ActiveAura aura) {
        return addAura(aura, false);
    }

    public UndoAction<Game> addAura(ActiveAura aura, boolean toCopy) {
        UndoAction<ActiveAuraList> undoRef = activeAuras.addAura(aura, toCopy);
        return (game) -> undoRef.undo(game.activeAuras);
    }

    private void updateAllAuras() {
        activeAuras.updateAllAura();
        player1.updateAuras();
        player2.updateAuras();
    }

    public void endPhase() {
        boolean deathResults = resolveDeaths();
        if (!deathResults)
            return;

        do {
            deathResults = resolveDeaths();
        } while (deathResults && !isGameOver());
    }

    private boolean resolveDeaths() {
        updateAllAuras();
        return resolveDeathsWithoutAura();
    }

    private boolean resolveDeathsWithoutAura() {
        updateGameOverState();

        if (isGameOver())
            // We could finish the death-rattles but why would we?
            return false;

        List<Minion> deadMinions = new ArrayList<>();
        player1.getBoard().collectMinions(deadMinions, Minion::isDead);
        player2.getBoard().collectMinions(deadMinions, Minion::isDead);

        List<Weapon> deadWeapons = removeDeadWeapons();

        if (deadWeapons.isEmpty() && deadMinions.isEmpty())
            return false;

        List<DestroyableEntity> deadEntities = new ArrayList<>(deadWeapons.size() + deadMinions.size());
        for (Minion minion: deadMinions) {
            Graveyard graveyard = minion.getOwner().getGraveyard();
            graveyard.addDeadMinion(minion);

            deadEntities.add(minion);
        }

        for (Weapon weapon: deadWeapons)
            deadEntities.add(weapon);

        BornEntity.sortEntities(deadEntities);

        for (DestroyableEntity dead: deadEntities)
            dead.scheduleToDestroy();

        for (DestroyableEntity dead: deadEntities)
            dead.destroy();

        return true;
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

    @Override
    public Game getGame() {
        return this;
    }
}
