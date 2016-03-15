package info.hearthsim.brazier.cards;

import info.hearthsim.brazier.Keyword;
import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.actions.ManaCostAdjuster;
import info.hearthsim.brazier.actions.PlayActionDef;
import info.hearthsim.brazier.actions.TargetNeed;
import info.hearthsim.brazier.actions.TargetlessAction;
import info.hearthsim.brazier.actions.undo.UndoableUnregisterAction;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.HearthStoneEntity;
import info.hearthsim.brazier.Keywords;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.weapons.WeaponDescr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.jtrim.collections.CollectionsEx;
import org.jtrim.utils.ExceptionHelper;

/**
 * A description of a card, which describes a kind of cards in hearthstone.
 * <p>
 * Unlike {@link Card} who stands for in-game card and different {@code Card} instances may have the same name,
 * {@code CardDescr} stands for the game-irrelevant properties of a kind of card, by which different {@code CardDescr}
 * must have different names.
 */
public final class CardDescr implements HearthStoneEntity {
    public static final CardDescr DO_NOTHING = new Builder(new CardId(""), CardType.UNKNOWN, 0).create();

    private final CardId cardId;
    private final int manaCost;
    private final String name;
    private final String description;
    private final Keyword cardClass;
    private final Set<Keyword> keywords;
    private final CardType cardType;
    private final CardRarity rarity;
    private final int overload;
    private final MinionDescr minion;
    private final WeaponDescr weapon;

    private final List<ManaCostAdjuster> manaCostAdjusters;
    private final List<TargetlessAction<? super Card>> onDrawActions;
    private final Ability<? super Card> inHandAbility;
    private final List<PlayActionDef<Card>> onPlayActions;
    private final List<CardProvider> chooseOneActionsRef;

    private final AtomicReference<List<CardDescr>> chooseOneActions;

    private CardDescr(Builder builder) {
        this.manaCost = builder.manaCost;
        this.overload = builder.overload;
        this.cardId = builder.cardId;
        this.name = builder.displayName;
        this.cardType = builder.cardType;
        this.description = builder.description;
        this.rarity = builder.rarity;
        this.cardClass = builder.cardClass;
        this.minion = builder.minion;
        this.weapon = builder.weapon;
        this.keywords = readOnlyCopySet(builder.getCombinedKeywords());
        this.onDrawActions = CollectionsEx.readOnlyCopy(builder.onDrawActions);
        this.onPlayActions = CollectionsEx.readOnlyCopy(builder.onPlayActions);
        this.inHandAbility = builder.inHandAbility;
        this.manaCostAdjusters = CollectionsEx.readOnlyCopy(builder.manaCostAdjusters);
        this.chooseOneActionsRef = CollectionsEx.readOnlyCopy(builder.chooseOneActions);
        this.chooseOneActions = new AtomicReference<>(chooseOneActionsRef.isEmpty() ? Collections.emptyList() : null);

        if (this.cardType == CardType.MINION && this.minion == null) {
            throw new IllegalStateException("Must have a minion when the card tpye is MINION.");
        }
        if (this.cardType != CardType.MINION && this.minion != null) {
            throw new IllegalStateException("May not have a minion when the card tpye is not MINION.");
        }
    }

    public String getName() {
        return name;
    }

    public CardRarity getRarity() {
        return rarity;
    }

    public Keyword getCardClass() {
        return cardClass;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getOverload() {
        return overload;
    }

    @Override
    public CardId getId() {
        return cardId;
    }

    /**
     * Returns the in-hand ability of the card; returns {@code null} if the card has no such ability.
     */
    public Ability<? super Card> tryGetInHandAbility() {
        return inHandAbility;
    }

    /**
     * Returns the in-hand ability of the card. This method will never return {@code null}.
     */
    public Ability<? super Card> getInHandAbility() {
        return inHandAbility != null
                ? inHandAbility
                : (card) -> UndoableUnregisterAction.DO_NOTHING;
    }

    public TargetNeed getCombinedTargetNeed(Player player) {
        ExceptionHelper.checkNotNullArgument(player, "player");

        TargetNeed need = PlayActionDef.combineNeeds(player, onPlayActions);
        if (minion != null) {
            for (PlayActionDef<Minion> battleCry: minion.getBattleCries()) {
                if (battleCry.getRequirement().meetsRequirement(player)) {
                    need = need.combine(battleCry.getTargetNeed());
                }
            }
        }
        return need;
    }

    public CardType getCardType() {
        return cardType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public Set<Keyword> getKeywords() {
        return keywords;
    }

    public List<TargetlessAction<? super Card>> getOnDrawActions() {
        return onDrawActions;
    }

    public List<PlayActionDef<Card>> getOnPlayActions() {
        return onPlayActions;
    }

    /**
     * Returns the <b>Choose one</b> choices of this card.
     */
    public List<CardDescr> getChooseOneChoices() {
        List<CardDescr> result = chooseOneActions.get();
        if (result == null) {
            result = new ArrayList<>(chooseOneActionsRef.size());
            for (CardProvider cardRef: chooseOneActionsRef) {
                result.add(cardRef.getCard());
            }
            result = Collections.unmodifiableList(result);
            if (!chooseOneActions.compareAndSet(null, result)) {
                result = chooseOneActions.get();
            }
        }
        return result;
    }

    /**
     * Returns if the card will do something when it's played by the given {@link Player}.
     * Returns {@code true} if the card can completeSummon a minion or the given {@code Player} meets the
     * requirement of the battle cry or choose-one effect of the card.
     */
    public boolean doesSomethingWhenPlayed(Player player) {
        if (minion != null) {
            return true;
        }

        for (PlayActionDef<?> action: onPlayActions) {
            if (action.getRequirement().meetsRequirement(player)) {
                return true;
            }
        }

        for (CardDescr optional: getChooseOneChoices()) {
            if (optional.doesSomethingWhenPlayed(player)) {
                return true;
            }
        }

        return false;
    }

    public List<ManaCostAdjuster> getManaCostAdjusters() {
        return manaCostAdjusters;
    }

    public MinionDescr getMinion() {
        return minion;
    }

    public WeaponDescr getWeapon() {
        return weapon;
    }

    @Override
    public String toString() {
        return "Card: " + cardId;
    }

    private <T> Set<T> readOnlyCopySet(Collection<? extends T> src) {
        if (src.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(src));
    }

    public static final class Builder {
        private final CardId cardId;
        private final CardType cardType;
        private final int manaCost;
        private int overload;
        private String displayName;
        private Keyword cardClass;
        private String description;
        private CardRarity rarity;

        private final List<TargetlessAction<? super Card>> onDrawActions;
        private final List<PlayActionDef<Card>> onPlayActions;
        private final List<ManaCostAdjuster> manaCostAdjusters;
        private final List<CardProvider> chooseOneActions;
        private Ability<? super Card> inHandAbility;

        private final Set<Keyword> keywords;

        private MinionDescr minion;
        private WeaponDescr weapon;

        public Builder(CardId cardId, CardType cardType, int manaCost) {
            ExceptionHelper.checkNotNullArgument(cardId, "cardId");
            ExceptionHelper.checkNotNullArgument(cardType, "cardType");

            this.manaCost = manaCost;
            this.cardId = cardId;
            this.cardType = cardType;
            this.displayName = cardId.getName();
            this.description = "";
            this.cardClass = Keywords.CLASS_NEUTRAL;
            this.onDrawActions = new LinkedList<>();
            this.onPlayActions = new LinkedList<>();
            this.manaCostAdjusters = new LinkedList<>();
            this.chooseOneActions = new LinkedList<>();
            this.keywords = new HashSet<>();
            this.rarity = CardRarity.COMMON;
            this.minion = null;
            this.weapon = null;
            this.overload = 0;
            this.inHandAbility = null;
        }

        public void addChooseOneAction(CardProvider cardRef) {
            ExceptionHelper.checkNotNullArgument(cardRef, "cardRef");
            chooseOneActions.add(cardRef);
        }

        public void setOverload(int overload) {
            ExceptionHelper.checkArgumentInRange(overload, 0, Integer.MAX_VALUE, "overload");
            this.overload = overload;
        }

        public void setDisplayName(String displayName) {
            ExceptionHelper.checkNotNullArgument(displayName, "name");
            this.displayName = displayName;
        }

        public void setCardClass(Keyword cardClass) {
            ExceptionHelper.checkNotNullArgument(cardClass, "cardClass");
            this.cardClass = cardClass;
        }

        public void setRarity(CardRarity rarity) {
            ExceptionHelper.checkNotNullArgument(rarity, "rarity");
            this.rarity = rarity;
        }

        public void addKeyword(Keyword keyword) {
            ExceptionHelper.checkNotNullArgument(keyword, "keyword");
            keywords.add(keyword);
        }

        public void setDescription(String description) {
            ExceptionHelper.checkNotNullArgument(description, "description");
            this.description = description;
        }

        public void addOnDrawAction(TargetlessAction<? super Card> onDrawAction) {
            ExceptionHelper.checkNotNullArgument(onDrawAction, "onDrawAction");
            this.onDrawActions.add(onDrawAction);
        }

        public void addOnPlayAction(PlayActionDef<Card> onPlayAction) {
            ExceptionHelper.checkNotNullArgument(onPlayAction, "onPlayAction");
            this.onPlayActions.add(onPlayAction);
        }

        public void addManaCostAdjuster(ManaCostAdjuster manaCostAdjuster) {
            ExceptionHelper.checkNotNullArgument(manaCostAdjuster, "manaCostAdjuster");
            this.manaCostAdjusters.add(manaCostAdjuster);
        }

        public void setInHandAbility(Ability<? super Card> inHandAbility) {
            this.inHandAbility = inHandAbility;
        }

        public void setMinion(MinionDescr minion) {
            this.minion = minion;
        }

        public void setWeapon(WeaponDescr weapon) {
            this.weapon = weapon;
        }

        private Set<Keyword> getCombinedKeywords() {
            if (minion == null && weapon == null) {
                return keywords;
            }

            Set<Keyword> result = new HashSet<>(keywords);
            if (minion != null) {
                result.addAll(minion.getKeywords());
            }
            if (weapon != null) {
                result.addAll(weapon.getKeywords());
            }
            return result;
        }

        public CardDescr create() {
            return new CardDescr(this);
        }
    }
}
