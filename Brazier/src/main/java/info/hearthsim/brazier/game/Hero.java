package info.hearthsim.brazier.game;

import info.hearthsim.brazier.TargeterDef;
import info.hearthsim.brazier.util.UndoAction;
import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.abilities.AuraAwareBoolProperty;
import info.hearthsim.brazier.abilities.HpProperty;
import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.game.weapons.AttackTool;
import info.hearthsim.brazier.game.weapons.Weapon;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

import org.jtrim.utils.ExceptionHelper;

/**
 * The hero in a game, controlled by the specific player.
 */
public final class Hero implements Character<Hero> {
    private final EntityId heroId;
    private final Player owner;
    private final long birthDate;
    private HeroPower heroPower;

    private final HpProperty hp;
    private int currentArmor;

    private final AuraAwareBoolProperty immune;

    private final HeroAttackTool attackTool;
    private final Set<Keyword> keywords;
    private Keyword heroClass;

    private boolean poisoned;

    public Hero(Player owner, int maxHp, int startingArmor,
        Keyword heroClass, Collection<? extends Keyword> keywords) {
        this(owner, new HpProperty(maxHp), startingArmor, heroClass, keywords);
    }

    public Hero(Player owner, HpProperty hp, int startingArmor,
        Keyword heroClass, Collection<? extends Keyword> keywords) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkNotNullArgument(hp, "hp");
        ExceptionHelper.checkArgumentInRange(startingArmor, 0, Integer.MAX_VALUE, "startingArmor");
        ExceptionHelper.checkNotNullArgument(heroClass, "heroClass");

        this.heroId = new EntityId();
        this.heroPower = new HeroPower(this, CardDescr.DO_NOTHING);
        this.owner = owner;
        this.hp = hp;
        this.currentArmor = startingArmor;
        this.attackTool = new HeroAttackTool();
        this.immune = new AuraAwareBoolProperty(false);
        this.heroClass = heroClass;
        this.keywords = copySet(keywords);
        this.poisoned = false;
        this.birthDate = owner.getGame().getCurrentTime();

        ExceptionHelper.checkNotNullElements(this.keywords, "keywords");
    }

    /**
     * Creates a copy of the given {@code Hero} with the given new {@link Player owner}.
     */
    private Hero(Player newOwner, Hero hero) {
        ExceptionHelper.checkNotNullArgument(newOwner, "newOwner");
        ExceptionHelper.checkNotNullArgument(hero, "hero");

        this.heroId = hero.heroId;
        this.heroPower = hero.heroPower.copyFor(this);
        this.owner = newOwner;
        this.hp = hero.hp.copy();
        this.currentArmor = hero.currentArmor;
        this.attackTool = new HeroAttackTool(hero.attackTool);
        this.immune = hero.immune.copy();
        this.heroClass = hero.heroClass;
        this.keywords = hero.keywords; // This field is unmodifiable
        this.poisoned = hero.poisoned;
        this.birthDate = hero.birthDate;
    }

    /**
     * Returns a copy of this {@code Hero} with the given new {@link Player owner}.
     */
    public Hero copyFor(Game newGame, Player newOwner) {
        return new Hero(newOwner, this);
    }

    private static <T> Set<T> copySet(Collection<? extends T> other) {
        int otherSize = other.size();
        if (otherSize == 0) {
            return Collections.emptySet();
        }
        if (otherSize == 1) {
            return Collections.singleton(other.iterator().next());
        }

        Set<T> result = new HashSet<>(other);
        return Collections.unmodifiableSet(result);
    }

    /**
     * Refreshes the {@code Hero} at start of turn by refreshing its attack count
     * and hero power.
     */
    public void refresh() {
        attackTool.refreshStartOfTurn();
        heroPower.refresh();
    }

    public void updateAuras() {
        hp.applyAura();
    }

    @Override
    public long getBirthDate() {
        return birthDate;
    }

    public void refreshEndOfTurn() {
        attackTool.refreshEndOfTurn();
    }

    @Override
    public void kill() {
        if (poisoned)
            return;

        poisoned = true;
    }

    @Override
    public Damage createDamage(int damage) {
        return new Damage(this, damage);
    }

    public Keyword getHeroClass() {
        return heroClass;
    }

    public void setHeroClass(Keyword newClass) {
        ExceptionHelper.checkNotNullArgument(newClass, "newClass");

        heroClass = newClass;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public boolean isDead() {
        return poisoned || hp.isDead();
    }

    @Override
    public boolean isDamaged() {
        return hp.isDamaged();
    }

    @Override
    public boolean isTargetable(TargeterDef targeterDef) {
        if (immune.getValue()) {
            // Currently there are no useful things to target an immune hero.
            // TODO Can healing spell like Lesser Healing target an immune hero?
            return false;
        }
        if (targeterDef.isDirectAttack() && targeterDef.hasSameOwner(this)) {
            return false;
        }

        return !targeterDef.isDirectAttack() || !getOwner().getBoard().hasNonStealthTaunt();
    }

    public AuraAwareBoolProperty getImmuneProperty() {
        return immune;
    }

    /**
     * Returns if the hero is <b>Immune</b>.
     */
    public boolean isImmune() {
        return immune.getValue();
    }

    public int getExtraAttackForThisTurn() {
        return attackTool.extraAttack;
    }

    public UndoAction<Hero> addExtraAttackForThisTurn(int amount) {
        return UndoAction.of(this, (h) -> h.attackTool, (at) -> at.addAttack(amount));
    }

    @Override
    public AttackTool getAttackTool() {
        return attackTool;
    }

    @Override
    public EntityId getEntityId() {
        return heroId;
    }

    public HeroPower getHeroPower() {
        return heroPower;
    }

    public void setHeroPower(CardDescr power) {
        ExceptionHelper.checkNotNullArgument(power, "power");

        this.heroPower = new HeroPower(this, power);
    }

    public HpProperty getHp() {
        return hp;
    }

    public int getMaxHp() {
        return hp.getMaxHp();
    }

    public void setMaxHp(int maxHp) {
        hp.setMaxHp(maxHp);
    }

    public int getCurrentHp() {
        return hp.getCurrentHp();
    }

    public void setCurrentHp(int currentHp) {
        hp.setCurrentHp(currentHp);
    }

    public int getCurrentArmor() {
        return currentArmor;
    }

    public void setCurrentArmor(int currentArmor) {
        ExceptionHelper.checkArgumentInRange(currentArmor, 0, Integer.MAX_VALUE, "currentArmor");
        this.currentArmor = currentArmor;
    }

    public void armorUp(int armor) {
        if (armor > 0) {
            setCurrentArmor(getCurrentArmor() + armor);
            getGame().getEvents().triggerEvent(
                SimpleEventType.ARMOR_GAINED,
                new ArmorGainedEvent(this, armor));
        } else {
            setCurrentArmor(Math.max(0, getCurrentArmor() + armor));
        }
    }

    public static int doPreparedDamage(
        Damage damage,
        Character target,
        ToIntFunction<Damage> damageMethod) {
        ExceptionHelper.checkNotNullArgument(damage, "damage");
        ExceptionHelper.checkNotNullArgument(target, "target");
        ExceptionHelper.checkNotNullArgument(damageMethod, "damageMethod");

        DamageRequest request = new DamageRequest(damage, target);
        target.getGame().getEvents().triggerEventNow(SimpleEventType.PREPARE_DAMAGE, request);

        return damageMethod.applyAsInt(request.getDamage());
    }

    @Override
    public boolean isLethalDamage(int damage) {
        if (immune.getValue()) {
            return false;
        }
        return damage > getCurrentArmor() + getCurrentHp();
    }

    @Override
    public int damage(Damage damage) {
        return doPreparedDamage(damage, this, this::applyDamage);
    }

    private int applyDamage(Damage damage) {
        int attack = damage.getDamage();

        if (attack == 0)
            return 0;

        if (attack > 0 && immune.getValue())
            return 0;


        final int originalArmor = getCurrentArmor();
        final int originalHp = getCurrentHp();

        int newHp;
        int newArmor;

        if (attack < 0) {
            // Healing path

            newArmor = originalArmor;
            newHp = Math.min(getMaxHp(), originalHp - attack);
        } else {
            // Damage path

            if (originalArmor >= attack) {
                newHp = originalHp;
                newArmor = originalArmor - attack;
            } else {
                newArmor = 0;

                int remainingDamage = attack - originalArmor;
                newHp = originalHp - remainingDamage;
            }
        }

        setCurrentArmor(newArmor);
        setCurrentHp(newHp);

        GameEvents events = getOwner().getGame().getEvents();

        int damageDealt = originalHp - newHp;
        if (damageDealt != 0) {
            SimpleEventType eventType = damageDealt < 0
                ? SimpleEventType.HERO_HEALED
                : SimpleEventType.HERO_DAMAGED;
            DamageEvent event = new DamageEvent(damage.getSource(), this, damageDealt);
            events.triggerEvent(eventType, event);
        }

        return damageDealt;
    }

    @Override
    public Set<Keyword> getKeywords() {
        return keywords;
    }

    private final class HeroAttackTool implements AttackTool {
        private int attackCount;
        private int extraAttack;
        private final FreezeManager freezeManager;

        public HeroAttackTool() {
            this.freezeManager = new FreezeManager();
            this.attackCount = 0;
            this.extraAttack = 0;
        }

        /**
         * Creates a copy of the given {@code HeroAttackTool}.
         */
        private HeroAttackTool(HeroAttackTool other) {
            this.freezeManager = other.freezeManager.copy();
            this.attackCount = other.attackCount;
            this.extraAttack = other.extraAttack;
        }

        private Weapon tryGetWeapon() {
            return getOwner().tryGetWeapon();
        }

        @Override
        public int getAttack() {
            return getOwner().getWeaponAttack() + extraAttack;
        }

        public UndoAction<HeroAttackTool> addAttack(int attackAddition) {
            extraAttack += attackAddition;
            return (hat) -> hat.extraAttack -= attackAddition;
        }

        @Override
        public int getMaxAttackCount() {
            Weapon currentWeapon = tryGetWeapon();
            return currentWeapon != null
                ? currentWeapon.getBaseDescr().getMaxAttackCount()
                : 1;
        }

        @Override
        public boolean canAttackWith() {
            return !freezeManager.isFrozen()
                && getAttack() > 0
                && attackCount < getMaxAttackCount();
        }

        @Override
        public boolean canRetaliateWith() {
            Weapon weapon = tryGetWeapon();
            return weapon != null && weapon.canRetaliateWith();
        }

        @Override
        public boolean canTargetRetaliate() {
            Weapon weapon = tryGetWeapon();
            return weapon == null || weapon.canTargetRetaliate();
        }

        @Override
        public void incAttackCount() {
            Weapon currentWeapon = tryGetWeapon();
            if (currentWeapon != null)
                currentWeapon.decreaseCharges();

            attackCount++;
        }

        @Override
        public void freeze() {
            freezeManager.freeze();
        }

        @Override
        public boolean isFrozen() {
            return freezeManager.isFrozen();
        }

        @Override
        public void refreshStartOfTurn() {
            int prevAttackCount = attackCount;
            int prevExtraAttack = extraAttack;

            attackCount = 0;
            extraAttack = 0;
        }

        public void refreshEndOfTurn() {
            freezeManager.endTurn(attackCount);
        }

        @Override
        public boolean attacksLeft() {
            return false;
        }

        @Override
        public boolean attacksRight() {
            return false;
        }
    }
}
