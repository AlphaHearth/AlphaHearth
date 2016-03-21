package info.hearthsim.brazier.minions;

import info.hearthsim.brazier.abilities.AuraAwareBoolProperty;
import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.Damage;
import info.hearthsim.brazier.Silencable;
import info.hearthsim.brazier.abilities.AuraAwareIntProperty;
import info.hearthsim.brazier.abilities.HpProperty;
import info.hearthsim.brazier.events.DamageEvent;
import info.hearthsim.brazier.events.SimpleEventType;
import org.jtrim.utils.ExceptionHelper;

/**
 * The body of a {@link Minion}
 */
public final class MinionBody implements Silencable {
    private final Minion owner;
    private final MinionDescr baseStats;

    private final HpProperty hp;
    private boolean poisoned;

    private boolean taunt;
    private boolean divineShield;
    private final AuraAwareBoolProperty stealth;
    private final AuraAwareBoolProperty untargetable;
    private final AuraAwareBoolProperty immune;
    private final AuraAwareIntProperty minHp;

    /**
     * Creates a {@code MinionBody} with the given {@code Minion} and {@code MinionDescr},
     * which reflects the status of a just-summoned specific kind of minion.
     *
     * @param owner the owner ({@code Minion}) of this {@code MinionBody}
     * @param baseStats the {@code MinionDescr} of this minion
     */
    public MinionBody(Minion owner, MinionDescr baseStats) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkNotNullArgument(baseStats, "baseStats");

        this.owner = owner;
        this.baseStats = baseStats;

        this.hp = new HpProperty(baseStats.getHp());
        this.taunt = baseStats.isTaunt();
        this.divineShield = baseStats.isDivineShield();
        this.poisoned = false;
        this.untargetable = new AuraAwareBoolProperty(!baseStats.isTargetable());
        this.stealth = new AuraAwareBoolProperty(baseStats.isStealth());
        this.immune = new AuraAwareBoolProperty(false);
        this.minHp = new AuraAwareIntProperty(Integer.MIN_VALUE);
    }

    private MinionBody(Minion owner, MinionBody other) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkNotNullArgument(other, "other");

        this.owner = owner;
        this.baseStats = other.baseStats;
        this.hp = other.hp.copy();
        this.poisoned = other.poisoned;
        this.taunt = other.taunt;
        this.divineShield = other.divineShield;
        this.stealth = other.stealth.copy();
        this.untargetable = other.untargetable.copy();
        this.immune = other.immune.copy();
        this.minHp = other.minHp.copy();
    }

    public MinionBody copyFor(Minion minion) {
        return new MinionBody(minion, this);
    }

    public void poison() {
        if (!poisoned)
            poisoned = true;
    }

    public AuraAwareBoolProperty getUntargetableProperty() {
        return untargetable;
    }

    public AuraAwareBoolProperty getStealthProperty() {
        return stealth;
    }

    public AuraAwareBoolProperty getImmuneProperty() {
        return immune;
    }

    public AuraAwareIntProperty getMinHpProperty() {
        return minHp;
    }

    public boolean isTargetable() {
        return !untargetable.getValue();
    }

    public MinionDescr getBaseStats() {
        return baseStats;
    }

    public HpProperty getHp() {
        return hp;
    }

    public int getCurrentHp() {
        return hp.getCurrentHp();
    }

    public int getMaxHp() {
        return hp.getMaxHp();
    }

    public boolean isTaunt() {
        return taunt;
    }

    public boolean isDead() {
        return poisoned || hp.isDead();
    }

    public boolean isDivineShield() {
        return divineShield;
    }

    public boolean isStealth() {
        return stealth.getValue();
    }

    public boolean isImmune() {
        return immune.getValue();
    }

    public void setDivineShield(boolean newValue) {
        divineShield = newValue;
    }

    public void setStealth(boolean newValue) {
        stealth.setValueTo(newValue);
    }

    public void setTaunt(boolean newValue) {
        taunt = newValue;
    }

    public int damage(Damage damage) {
        int attack = damage.getDamage();
        if (attack == 0)
            return 0;

        if (attack > 0 && immune.getValue())
            return 0;

        if (divineShield && attack > 0) {
            divineShield = false;
            return 0;
        }

        int currentHp = hp.getCurrentHp();
        int newHp = currentHp - attack;
        newHp = Math.max(minHp.getValue(), Math.min(newHp, hp.getMaxHp()));
        hp.setCurrentHp(newHp);
        int damageDone = currentHp - newHp;

        GameEvents events = owner.getGame().getEvents();
        DamageEvent event = new DamageEvent(damage.getSource(), owner, damageDone);

        SimpleEventType eventType = damageDone < 0
                ? SimpleEventType.MINION_HEALED
                : SimpleEventType.MINION_DAMAGED;
        events.triggerEvent(eventType, event);

        return event.getDamageDealt();
    }

    @Override
    public void silence() {
        stealth.silence();
        untargetable.silence();
        taunt = false;
        divineShield = false;

        hp.silence();
    }

    public boolean isLethalDamage(int damage) {
        if (divineShield || isImmune()) {
            return false;
        }
        return damage >= getCurrentHp();
    }

    public void applyAuras() {
        hp.applyAura();
    }
}
