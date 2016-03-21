package info.hearthsim.brazier;

import info.hearthsim.brazier.abilities.AuraAwareBoolProperty;
import info.hearthsim.brazier.abilities.AuraAwareIntProperty;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.actions.ActionUtils;
import info.hearthsim.brazier.actions.PlayActionDef;
import info.hearthsim.brazier.actions.PlayArg;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.actions.GameActionList;
import info.hearthsim.brazier.cards.PlayAction;
import info.hearthsim.brazier.events.CardPlayEvent;
import info.hearthsim.brazier.weapons.WeaponDescr;
import info.hearthsim.brazier.weapons.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

public final class Player implements Entity<Player>, PlayerProperty {
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
    private final AuraAwareBoolProperty damagingHealAura;

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
        this.damagingHealAura = new AuraAwareBoolProperty(false);
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
        this.spellPower = other.spellPower.copy();
        this.hero = other.hero.copyFor(game, this);
        this.board = other.board.copyFor(this);
        this.hand = other.hand.copyFor(this);
        this.manaResource = other.manaResource.copy();
        this.fatigue = other.fatigue;
        this.heroDamageMultiplier = other.heroDamageMultiplier.copy();
        this.damagingHealAura = other.damagingHealAura.copy();
        this.cardsPlayedThisTurn = other.cardsPlayedThisTurn;
        this.minionsPlayedThisTurn = other.minionsPlayedThisTurn;
        this.secrets = other.secrets.copyFor(this);
        this.deathRattleTriggerCount = other.deathRattleTriggerCount;
        this.auraFlags = other.auraFlags.copy();
        if (other.weapon != null)
            this.weapon = other.weapon.copyFor(game, this);
        this.graveyard = other.graveyard.copyFor(this);
        this.deck = other.deck.copyFor(this);
    }

    /**
     * Returns a copy of this {@code Player} for the given new {@code Game}.
     */
    public Player copyFor(Game game) {
        return new Player(game, this);
    }

    /**
     * Returns a copy of this {@code Player} for the given new {@code Game}.
     * The given {@code Player} will not be used.
     */
    public Player copyFor(Game game, Player player) {
        return new Player(game, this);
    }

    public FlagContainer getAuraFlags() {
        return auraFlags;
    }

    public void startNewTurn() {
        cardsPlayedThisTurn = 0;
        minionsPlayedThisTurn = 0;

        manaResource.refresh();
        drawCardToHand();
        board.refreshStartOfTurn();
        hero.refresh();

        getGame().getEvents().triggerEvent(SimpleEventType.TURN_STARTS, this);
    }

    public void endTurn() {
        getGame().getEvents().triggerEvent(SimpleEventType.TURN_ENDS, this);

        hero.refreshEndOfTurn();
        board.refreshEndOfTurn();
        graveyard.refreshEndOfTurn();
    }

    public void updateAuras() {
        hero.updateAuras();
        board.updateAuras();
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
        for (PlayActionDef<Card> actionDef : cardDescr.getOnPlayActions()) {
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

    private void executeCardPlayActions(PlayArg<Card> arg, List<? extends PlayAction<Card>> actions) {
        if (actions.isEmpty())
            return;

        for (PlayAction<Card> actionDef : actions)
            actionDef.doPlay(arg);
    }

    public void playCardEffect(Card card) {
        playCard(card, 0, new PlayTargetRequest(playerId), false);
    }

    public void playCardEffect(Card card, PlayTargetRequest targetRequest) {
        playCard(card, 0, targetRequest, false);
    }

    public void playCard(Card card, int manaCost, PlayTargetRequest targetRequest) {
        playCard(card, manaCost, targetRequest, true);
    }

    private void playCard(Card card, int manaCost, PlayTargetRequest targetRequest, boolean playCardEvents) {
        ExceptionHelper.checkNotNullArgument(card, "card");
        ExceptionHelper.checkNotNullArgument(targetRequest, "target");

        Character originalTarget = game.getCharacter(targetRequest.getEntityId());
        PlayArg<Card> originalCardPlayArg = new PlayArg<>(card, originalTarget);

        // We request the on play actions before doing anything because
        // the requirements for playing a card action may no longer met
        // after firing events. However, in this case we must complete the
        // action under these circumstances (when its requirement is not met).
        List<PlayAction<Card>> onPlayActions = getOnPlayActions(originalCardPlayArg, targetRequest.getChoseOneChoice());

        manaResource.spendMana(manaCost, card.getCardDescr().getOverload());

        cardsPlayedThisTurn++;

        GameEvents events = game.getEvents();

        CardPlayEvent playEvent = new CardPlayEvent(originalCardPlayArg, manaCost);

        Minion minion = card.getMinion();
        if (minion != null) {
            minionsPlayedThisTurn++;
            int minionLocation = targetRequest.getMinionLocation();

            board.tryAddToBoard(minion, minionLocation);
            PlayArg<Card> cardPlayArg = playEvent.getCardPlayArg();
            board.completeSummon(minion, cardPlayArg.getTarget());
            executeCardPlayActions(cardPlayArg, onPlayActions);

            if (playCardEvents)
                events.triggerEventNow(SimpleEventType.PLAY_CARD, playEvent);
        } else {
            events.triggerEventNow(SimpleEventType.PLAY_CARD, playEvent);
            // A spell card may get countered or misdirected.
            if (!playEvent.isVetoedPlay()) {
                PlayArg<Card> cardPlayArg = playEvent.getCardPlayArg();
                executeCardPlayActions(cardPlayArg, onPlayActions);
            }
        }
    }

    public int getWeaponAttack() {
        return weapon != null ? weapon.getAttack() : 0;
    }

    public Weapon tryGetWeapon() {
        return weapon;
    }

    public Weapon removeDeadWeapon() {
        Weapon weaponInHand = tryGetWeapon();
        if (weaponInHand == null || weaponInHand.getDurability() > 0) {
            return null;
        }

        weapon = null;
        return weaponInHand;
    }

    public void destroyWeapon() {
        if (tryGetWeapon() == null)
            return;

        equipWeapon(null);
    }

    public void equipWeapon(WeaponDescr newWeaponDescr) {
        Weapon currentWeapon = tryGetWeapon();

        Weapon newWeapon = newWeaponDescr != null
            ? new Weapon(this, newWeaponDescr)
            : null;
        this.weapon = newWeapon;
        if (newWeapon != null)
            newWeapon.activatePassiveAbilities();

        if (currentWeapon != null)
            currentWeapon.destroy();
    }

    /**
     * Summons a certain minion to the player's board.
     */
    public void summonMinion(MinionDescr minionDescr) {
        summonMinion(new Minion(this, minionDescr));
    }

    /**
     * Summons a certain minion to the given location of the player's board.
     */
    public void summonMinion(MinionDescr minionDescr, int index) {
        summonMinion(new Minion(this, minionDescr), index);
    }

    /**
     * Summons a certain minion to the player's board.
     */
    public void summonMinion(Minion minion) {
        board.tryAddToBoard(minion);
        board.completeSummon(minion);
    }

    /**
     * Summons a certain minion to the given location of the player's board.
     */
    public void summonMinion(Minion minion, int index) {
        board.tryAddToBoard(minion, index);
        board.completeSummon(minion);
    }

    private int prepareHeroDamage(int base) {
        if (base < 0 && damagingHealAura.getValue()) {
            return -base;
        } else {
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
    private void doFatigueDamage() {
        fatigue++;
        ActionUtils.damageCharacter(hero, fatigue, hero);
    }

    /**
     * Adds a certain card to the player's hand.
     */
    public void addCardToHand(CardDescr card) {
        addCardToHand(new Card(this, card));
    }

    /**
     * Adds a certain card to the player's hand.
     */
    public void addCardToHand(Card card) {
        ExceptionHelper.checkNotNullArgument(card, "card");

        GameActionList.executeActionsNow(card, card.getCardDescr().getOnDrawActions());

        GameEvents events = game.getEvents();
        hand.addCard(card, (addedCard) -> events.triggerEvent(SimpleEventType.DRAW_CARD, addedCard));
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
    public Card drawFromDeck() {
        Card drawnCard = deck.tryDrawOneCard();
        if (drawnCard == null) {
            doFatigueDamage();
            return null;
        }
        return drawnCard;
    }

    /**
     * Tries to draw a card from the deck and adds it to the player's hand. If there is no card
     * left, deals fatigue damage to the hero by invoking {@link #doFatigueDamage()}.
     *
     * @see #doFatigueDamage()
     */
    public Card drawCardToHand() {
        Card card = drawFromDeck();
        if (card == null) {
            return null;
        }
        addCardToHand(card);

        return card;
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
    public void setHero(Hero newHero) {
        ExceptionHelper.checkNotNullArgument(newHero, "newHero");
        if (newHero.getOwner() != this) {
            throw new IllegalArgumentException("Hero belongs to another player.");
        }
        hero = newHero;
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

    public AuraAwareBoolProperty getDamagingHealAura() {
        return damagingHealAura;
    }

    public void setMana(int mana) {
        manaResource.setMana(mana);
    }

    public Deck getDeck() {
        return deck;
    }

    public Graveyard getGraveyard() {
        return graveyard;
    }

    @Override
    public EntityId getEntityId() {
        return playerId;
    }
}
