package info.hearthsim.brazier;

import info.hearthsim.brazier.abilities.AuraAwareIntProperty;
import info.hearthsim.brazier.abilities.BuffableBoolProperty;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.events.CardPlayedEvent;
import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.PlayActionDef;
import info.hearthsim.brazier.actions.PlayArg;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.actions.GameActionList;
import info.hearthsim.brazier.cards.PlayAction;
import info.hearthsim.brazier.events.CardPlayEvent;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.weapons.WeaponDescr;
import info.hearthsim.brazier.weapons.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

public final class Player implements PlayerProperty {
    public static final int MAX_MANA = 10;
    public static final int MAX_HAND_SIZE = 10;
    public static final int MAX_BOARD_SIZE = 7;

    private final Game game;
    private final PlayerId playerId;
    private Hero hero;
    private final Hand hand;
    private final BoardSide board;
    private final Deck deck;
    private final Graveyard graveyard;
    private final SecretContainer secrets;

    private final AuraAwareIntProperty deathRattleTriggerCount;
    private final AuraAwareIntProperty spellPower;
    private final AuraAwareIntProperty heroDamageMultiplier;
    private final BuffableBoolProperty damagingHealAura;

    private final ManaResource manaResource;

    private int fatigue;

    private int cardsPlayedThisTurn;
    private int minionsPlayedThisTurn;

    private final FlagContainer auraFlags;

    private Weapon weapon;

    public Player(Game game, PlayerId playerId) {
        ExceptionHelper.checkNotNullArgument(game, "game");
        ExceptionHelper.checkNotNullArgument(playerId, "playerId");

        this.game = game;
        this.playerId = playerId;
        this.hero = new Hero(this, 30, 0, Keywords.CLASS_BOSS, Collections.emptySet());
        this.board = new BoardSide(this, MAX_BOARD_SIZE);
        this.hand = new Hand(this, MAX_HAND_SIZE);
        this.manaResource = new ManaResource();
        this.fatigue = 0;
        this.spellPower = new AuraAwareIntProperty(0);
        this.heroDamageMultiplier = new AuraAwareIntProperty(1);
        this.damagingHealAura = new BuffableBoolProperty(() -> false);
        this.cardsPlayedThisTurn = 0;
        this.minionsPlayedThisTurn = 0;
        this.secrets = new SecretContainer(this);
        this.deathRattleTriggerCount = new AuraAwareIntProperty(1);
        this.auraFlags = new FlagContainer();
        this.weapon = null;
        this.graveyard = new Graveyard();
        this.deck = new Deck(this);
    }

    private Player(Game game, Player other) {
        ExceptionHelper.checkNotNullArgument(game, "game");
        ExceptionHelper.checkNotNullArgument(other, "other");

        this.game = game;
        this.playerId = other.playerId;
        this.hero = other.hero.copyFor(this);
        this.board = other.board.copyFor(this);
        this.hand = other.hand.copyFor(this);
        this.manaResource = other.manaResource.copy();
        this.fatigue = other.fatigue;
        this.spellPower = other.spellPower.copy();
        this.heroDamageMultiplier = other.heroDamageMultiplier.copy();
        this.damagingHealAura = other.damagingHealAura.copy();
        this.cardsPlayedThisTurn = other.cardsPlayedThisTurn;
        this.minionsPlayedThisTurn = other.minionsPlayedThisTurn;
        this.secrets = other.secrets.copyFor(this);
        this.deathRattleTriggerCount = other.deathRattleTriggerCount;
        this.auraFlags = other.auraFlags.copy();
        this.weapon = other.weapon.copyFor(this);
        this.graveyard = other.graveyard.copyFor(this);
        this.deck = other.deck.copyFor(this);
    }

    /**
     * Returns a copy of this {@code Player} for the given new {@code Game}.
     */
    public Player copyFor(Game game) {
        return new Player(game, this);
    }

    public FlagContainer getAuraFlags() {
        return auraFlags;
    }

    public UndoAction startNewTurn() {
        UndoAction.Builder result = new UndoAction.Builder();

        int origCardsPlayedThisTurn = cardsPlayedThisTurn;
        cardsPlayedThisTurn = 0;
        result.addUndo(() -> cardsPlayedThisTurn = origCardsPlayedThisTurn);

        int origMinionsPlayedThisTurn = minionsPlayedThisTurn;
        minionsPlayedThisTurn = 0;
        result.addUndo(() -> minionsPlayedThisTurn = origMinionsPlayedThisTurn);

        result.addUndo(manaResource.refresh());
        result.addUndo(drawCardToHand());
        result.addUndo(board.refreshStartOfTurn());
        result.addUndo(hero.refresh());

        GameEvents events = getGame().getEvents();
        result.addUndo(events.triggerEvent(SimpleEventType.TURN_STARTS, this));

        return result;
    }

    public UndoAction endTurn() {
        GameEvents events = getGame().getEvents();
        UndoAction eventUndo = events.triggerEvent(SimpleEventType.TURN_ENDS, this);

        UndoAction refreshHeroUndo = hero.refreshEndOfTurn();
        UndoAction boardRefreshUndo = board.refreshEndOfTurn();
        graveyard.refreshEndOfTurn();
        return () -> {
            boardRefreshUndo.undo();
            refreshHeroUndo.undo();
            eventUndo.undo();
        };
    }

    public UndoAction updateAuras() {
        UndoAction heroUndo = hero.updateAuras();
        UndoAction boardUndo = board.updateAuras();
        return () -> {
            boardUndo.undo();
            heroUndo.undo();
        };
    }

    public Player getOpponent() {
        return game.getOpponent(playerId);
    }

    public SecretContainer getSecrets() {
        return secrets;
    }

    @Override
    public Player getOwner() {
        return this;
    }

    @Override
    public Game getGame() {
        return game;
    }

    public int getCardsPlayedThisTurn() {
        return cardsPlayedThisTurn;
    }

    public int getMinionsPlayedThisTurn() {
        return minionsPlayedThisTurn;
    }

    private void getOnPlayActions(
            CardDescr cardDescr,
            List<PlayAction<Card>> result) {
        for (PlayActionDef<Card> actionDef: cardDescr.getOnPlayActions()) {
            if (actionDef.getRequirement().meetsRequirement(this)) {
                result.add(actionDef.getAction());
            }
        }
    }

    private List<PlayAction<Card>> getOnPlayActions(
            PlayArg<Card> arg,
            CardDescr chooseOneChoice) {

        CardDescr cardDescr = arg.getActor().getCardDescr();

        int playActionCount = cardDescr.getOnPlayActions().size()
                + (chooseOneChoice != null ? chooseOneChoice.getOnPlayActions().size() : 0);

        List<PlayAction<Card>> result = new ArrayList<>(playActionCount);
        getOnPlayActions(cardDescr, result);
        if (chooseOneChoice != null) {
            getOnPlayActions(chooseOneChoice, result);
        }

        return result;
    }

    private UndoAction executeCardPlayActions(PlayArg<Card> arg, List<? extends PlayAction<Card>> actions) {
        if (actions.isEmpty()) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction.Builder result = new UndoAction.Builder(actions.size());
        for (PlayAction<Card> actionDef: actions) {
            result.addUndo(actionDef.doPlay(game, arg));
        }
        return result;
    }

    public UndoAction playCardEffect(Card card) {
        return playCard(card, 0, new PlayTargetRequest(playerId), false);
    }

    public UndoAction playCardEffect(Card card, PlayTargetRequest targetRequest) {
        return playCard(card, 0, targetRequest, false);
    }

    public UndoAction playCard(Card card, int manaCost, PlayTargetRequest targetRequest) {
        return playCard(card, manaCost, targetRequest, true);
    }

    private UndoAction playCard(Card card, int manaCost, PlayTargetRequest targetRequest, boolean playCardEvents) {
        ExceptionHelper.checkNotNullArgument(card, "card");
        ExceptionHelper.checkNotNullArgument(targetRequest, "target");

        Character originalTarget = game.findTarget(targetRequest.getTargetId());
        PlayArg<Card> originalCardPlayArg = new PlayArg<>(card, originalTarget);

        // We request the on play actions before doing anything because
        // the requirements for playing a card action may no longer met
        // after firing events. However, in this case we must complete the
        // action under these circumstances (when its requirement is not met).
        List<PlayAction<Card>> onPlayActions = getOnPlayActions(originalCardPlayArg, targetRequest.getChoseOneChoice());

        UndoAction.Builder result = new UndoAction.Builder();
        result.addUndo(manaResource.spendMana(manaCost, card.getCardDescr().getOverload()));

        cardsPlayedThisTurn++;
        result.addUndo(() -> cardsPlayedThisTurn--);

        GameEvents events = game.getEvents();

        CardPlayEvent playEvent = new CardPlayEvent(originalCardPlayArg, manaCost);

        Minion minion = card.getMinion();
        if (minion != null) {
            minionsPlayedThisTurn++;
            result.addUndo(() -> minionsPlayedThisTurn--);

            int minionLocation = targetRequest.getMinionLocation();

            UndoAction reserveUndo
                    = board.tryAddToBoard(minion, minionLocation);
            // reserveUndo shouldn't be null if we were allowed to play this card.
            if (reserveUndo != null) {
                result.addUndo(reserveUndo::undo);
            }

            if (playCardEvents) {
                result.addUndo(events.triggerEventNow(SimpleEventType.START_PLAY_CARD, playEvent));
            }

            if (!playEvent.isVetoedPlay() && reserveUndo != null) {
                PlayArg<Card> cardPlayArg = playEvent.getCardPlayArg();
                result.addUndo(board.completeSummon(minion, cardPlayArg.getTarget()));
                result.addUndo(executeCardPlayActions(cardPlayArg, onPlayActions));
            }
        }
        else {
            result.addUndo(events.triggerEventNow(SimpleEventType.START_PLAY_CARD, playEvent));
            if (!playEvent.isVetoedPlay()) {
                PlayArg<Card> cardPlayArg = playEvent.getCardPlayArg();
                result.addUndo(executeCardPlayActions(cardPlayArg, onPlayActions));
            }
        }

        if (playCardEvents && !playEvent.isVetoedPlay()) {
            result.addUndo(events.triggerEvent(SimpleEventType.DONE_PLAY_CARD, new CardPlayedEvent(card, manaCost)));
        }

        return result;
    }

    public int getWeaponAttack() {
        return weapon != null ? weapon.getAttack() : 0;
    }

    public Weapon tryGetWeapon() {
        return weapon;
    }

    public UndoableResult<Weapon> removeDeadWeapon() {
        Weapon weaponInHand = tryGetWeapon();
        if (weaponInHand == null || weaponInHand.getDurability() > 0) {
            return new UndoableResult<>(null);
        }

        weapon = null;
        return new UndoableResult<>(weaponInHand, () -> {
            weapon = weaponInHand;
        });
    }

    public UndoAction destroyWeapon() {
        if (tryGetWeapon() == null) {
            return UndoAction.DO_NOTHING;
        }

        return equipWeapon(null);
    }

    public UndoAction equipWeapon(WeaponDescr newWeaponDescr) {
        Weapon currentWeapon = tryGetWeapon();

        Weapon newWeapon = newWeaponDescr != null
                ? new Weapon(this, newWeaponDescr)
                : null;
        this.weapon = newWeapon;
        UndoAction abilityActivateUndo = newWeapon != null
                ? newWeapon.activatePassiveAbilities()
                : UndoAction.DO_NOTHING;

        UndoAction weaponKillUndo;
        if (currentWeapon != null) {
            weaponKillUndo = currentWeapon.destroy();
        }
        else {
            weaponKillUndo = UndoAction.DO_NOTHING;
        }

        return () -> {
            weaponKillUndo.undo();
            abilityActivateUndo.undo();
            this.weapon = currentWeapon;
        };
    }

    /**
     * Summons a certain minion to the player's board.
     */
    public UndoAction summonMinion(MinionDescr minionDescr) {
        return summonMinion(new Minion(this, minionDescr));
    }

    /**
     * Summons a certain minion to the given location of the player's board.
     */
    public UndoAction summonMinion(MinionDescr minionDescr, int index) {
        return summonMinion(new Minion(this, minionDescr), index);
    }

    /**
     * Summons a certain minion to the player's board.
     */
    public UndoAction summonMinion(Minion minion) {
        UndoAction reservationUndo = board.tryAddToBoard(minion);
        if (reservationUndo == null) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction summonUndo = board.completeSummon(minion);
        return () -> {
            summonUndo.undo();
            reservationUndo.undo();
        };
    }

    /**
     * Summons a certain minion to the given location of the player's board.
     */
    public UndoAction summonMinion(Minion minion, int index) {
        UndoAction reservationUndo = board.tryAddToBoard(minion, index);
        if (reservationUndo == null) {
            return UndoAction.DO_NOTHING;
        }

        UndoAction summonUndo = board.completeSummon(minion);
        return () -> {
            summonUndo.undo();
            reservationUndo.undo();
        };
    }

    private int prepareHeroDamage(int base) {
        if (base < 0 && damagingHealAura.getValue()) {
            return -base;
        }
        else {
            return base;
        }
    }

    private int adjustHeroDamage(int base) {
        return base * heroDamageMultiplier.getValue();
    }

    public Damage getSpellDamage(int baseDamage) {
        int preparedDamage = prepareHeroDamage(baseDamage);
        return new Damage(hero, adjustHeroDamage(preparedDamage >= 0
                ? preparedDamage + spellPower.getValue()
                : preparedDamage));
    }

    public Damage getBasicDamage(int baseDamage) {
        int preparedDamage = prepareHeroDamage(baseDamage);
        return new Damage(hero, adjustHeroDamage(preparedDamage));
    }

    /**
     * Deals fatigue damage to the hero. Every time this method is invoked, the fatigue damage
     * increases by {@code 1}.
     */
    private UndoAction doFatigueDamage() {
        fatigue++;
        UndoAction damageUndo = ActionUtils.damageCharacter(hero, fatigue, hero);
        return () -> {
            damageUndo.undo();
            fatigue--;
        };
    }

    /**
     * Adds a certain card to the player's hand.
     */
    public UndoAction addCardToHand(CardDescr card) {
        return addCardToHand(new Card(this, card));
    }

    /**
     * Adds a certain card to the player's hand.
     */
    public UndoAction addCardToHand(Card card) {
        ExceptionHelper.checkNotNullArgument(card, "card");

        UndoAction drawActionsUndo = GameActionList.executeActionsNow(game, card, card.getCardDescr().getOnDrawActions());

        GameEvents events = game.getEvents();
        UndoAction addCardUndo = hand.addCard(card, (addedCard) -> events.triggerEvent(SimpleEventType.DRAW_CARD, addedCard));

        return () -> {
            addCardUndo.undo();
            drawActionsUndo.undo();
        };
    }

    /**
     * Tries to draw a card from the deck. If there is no card left, deals fatigue damage
     * to the hero by invoking {@link #doFatigueDamage()}.
     * <p>
     * <b>Note</b>: the card will not be added to the player's hand in this method;
     * use {@link #drawCardToHand()} instead.
     *
     * @see #doFatigueDamage()
     * @see #drawCardToHand()
     */
    public UndoableResult<Card> drawFromDeck() {
        UndoableResult<Card> drawnCard = deck.tryDrawOneCard();
        if (drawnCard == null) {
            UndoAction fatigueUndo = doFatigueDamage();
            return new UndoableResult<>(null, fatigueUndo);
        }
        return drawnCard;
    }

    /**
     * Tries to draw a card from the deck and adds it to the player's hand. If there is no card
     * left, deals fatigue damage to the hero by invoking {@link #doFatigueDamage()}.
     *
     * @see #doFatigueDamage()
     */
    public UndoableResult<Card> drawCardToHand() {
        UndoableResult<Card> cardRef = drawFromDeck();
        Card card = cardRef.getResult();
        if (card == null) {
            return cardRef;
        }

        UndoAction addCardUndo = addCardToHand(card);

        return new UndoableResult<>(card, () -> {
            addCardUndo.undo();
            cardRef.undo();
        });
    }

    public Hand getHand() {
        return hand;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    /**
     * Sets the hero of the player.
     *
     * @throws IllegalArgumentException if the given hero already belong to other player.
     */
    public UndoAction setHero(Hero newHero) {
        ExceptionHelper.checkNotNullArgument(newHero, "newHero");
        if (newHero.getOwner() != this) {
            throw new IllegalArgumentException("Hero belongs to another player.");
        }

        Hero prevHero = hero;
        hero = newHero;
        return () -> hero = prevHero;
    }

    public Hero getHero() {
        return hero;
    }

    public BoardSide getBoard() {
        return board;
    }

    public ManaResource getManaResource() {
        return manaResource;
    }

    public int getMana() {
        return manaResource.getMana();
    }

    public AuraAwareIntProperty getSpellPower() {
        return spellPower;
    }

    public AuraAwareIntProperty getHeroDamageMultiplier() {
        return heroDamageMultiplier;
    }

    public AuraAwareIntProperty getDeathRattleTriggerCount() {
        return deathRattleTriggerCount;
    }

    public BuffableBoolProperty getDamagingHealAura() {
        return damagingHealAura;
    }

    public UndoAction setMana(int mana) {
        return manaResource.setMana(mana);
    }

    public Deck getDeck() {
        return deck;
    }

    public Graveyard getGraveyard() {
        return graveyard;
    }
}
