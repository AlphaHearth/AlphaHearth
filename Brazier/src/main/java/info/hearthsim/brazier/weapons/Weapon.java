package info.hearthsim.brazier.weapons;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.abilities.AbilityList;
import info.hearthsim.brazier.abilities.AuraAwareIntProperty;
import info.hearthsim.brazier.util.UndoAction;
import info.hearthsim.brazier.events.*;
import info.hearthsim.brazier.Game;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jtrim.utils.ExceptionHelper;

public final class Weapon implements Entity<Weapon>, DestroyableEntity, DamageSource, LabeledEntity {
    private final EntityId weaponId;
    private final Player owner;
    private final WeaponDescr baseDescr;
    private final AbilityList<Weapon> abilities;
    private final Ability<Weapon> deathRattle;
    private final long birthDate;

    private final AuraAwareIntProperty attack;
    private int durability;

    private final AtomicBoolean scheduledToDestroy;

    public Weapon(Player owner, WeaponDescr weaponDescr) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkNotNullArgument(weaponDescr, "weaponDescr");

        this.weaponId = new EntityId();
        this.owner = owner;
        this.baseDescr = weaponDescr;
        this.attack = new AuraAwareIntProperty(weaponDescr.getAttack());
        this.durability = weaponDescr.getDurability();
        this.birthDate = owner.getOwner().getGame().getCurrentTime();
        this.abilities = new AbilityList<>(this);
        this.scheduledToDestroy = new AtomicBoolean(false);

        EventAction<? super Weapon, ? super Weapon> deathRattleAction = baseDescr.tryGetDeathRattle();
        this.deathRattle = deathRattleAction != null ? deathRattleToAbility(deathRattleAction) : null;
    }

    private Weapon(Player newOwner, Weapon other) {
        ExceptionHelper.checkNotNullArgument(newOwner, "newOwner");
        ExceptionHelper.checkNotNullArgument(other, "other");

        this.weaponId = other.weaponId;
        this.owner = newOwner;
        this.baseDescr = other.baseDescr;
        this.attack = other.attack.copy();
        this.durability = other.durability;
        this.birthDate = other.birthDate;
        this.abilities = other.abilities.copyFor(this, true);
        this.scheduledToDestroy = new AtomicBoolean(other.isScheduledToDestroy());
        this.deathRattle = other.deathRattle;
    }

    /**
     * Returns a copy of this {@code Weapon} with the given new {@link Player owner}.
     */
    public Weapon copyFor(Game newGame, Player newOwner) {
        return new Weapon(newOwner, this);
    }

    /**
     * Activates the weapon's passive abilities for the current {@code Game}.
     */
    public void activatePassiveAbilities() {
        abilities.addAndActivateAbility(baseDescr.getEventActionDefs());

        Ability<? super Weapon> ability = baseDescr.tryGetAbility();
        if (ability != null)
            abilities.addAndActivateAbility(ability);

        if (deathRattle != null)
            abilities.addAndActivateAbility(deathRattle);
    }

    public void deactivateAllAbilities() {
        abilities.deactivate();
    }

    @Override
    public void scheduleToDestroy() {
        scheduledToDestroy.compareAndSet(false, true);
    }

    @Override
    public boolean isScheduledToDestroy() {
        return scheduledToDestroy.get();
    }

    @Override
    public Set<Keyword> getKeywords() {
        return getBaseDescr().getKeywords();
    }

    @Override
    public long getBirthDate() {
        return birthDate;
    }

    @Override
    public Player getOwner() {
        return owner.getOwner();
    }

    public WeaponDescr getBaseDescr() {
        return baseDescr;
    }

    @Override
    public Damage createDamage(int damage) {
        return new Damage(this, damage);
    }

    public UndoAction<Weapon> increaseDurability() {
        return increaseDurability(1);
    }

    public UndoAction<Weapon> increaseDurability(int amount) {
        if (durability == Integer.MAX_VALUE || amount == 0) {
            return UndoAction.DO_NOTHING;
        }

        durability += amount;
        return (w) -> w.durability -= amount;
    }

    public void decreaseCharges() {
        durability--;
    }

    public int getAttack() {
        return attack.getValue();
    }

    public AuraAwareIntProperty getBuffableAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack.setValueTo(attack);
    }

    public int getDurability() {
        return durability;
    }

    public boolean canRetaliateWith() {
        return baseDescr.canRetaliateWith();
    }

    public boolean canTargetRetaliate() {
        return baseDescr.canTargetRetaliate();
    }

    @Override
    public void destroy() {
        owner.getGame().getEvents().triggerEvent(SimpleEventType.WEAPON_DESTROYED, this);

        // TODO: If we want to deactivate the abilities first, we have to
        //       adjust death-rattle handling not to get disabled.
        deactivateAllAbilities();
    }

    private static Ability<Weapon> deathRattleToAbility(
            EventAction<? super Weapon, ? super Weapon> deathRattle) {
        ExceptionHelper.checkNotNullArgument(deathRattle, "deathRattle");

        return (Weapon self) -> {
            GameEventActions<Weapon> listeners = self.getGame().getEvents()
                    .simpleListeners(SimpleEventType.WEAPON_DESTROYED);
            UndoAction<GameEventActions> undoRef = listeners.register((Weapon target) -> {
                if (target == self)
                    deathRattle.trigger(self, target);
            });
            return (w) -> {
                undoRef.undo(w.getGame().getEvents().simpleListeners(SimpleEventType.WEAPON_DESTROYED));
            };
        };
    }

    @Override
    public String toString() {
        return "Weapon{" + ", attack=" + attack + ", durability=" + durability + '}';
    }

    @Override
    public EntityId getEntityId() {
        return weaponId;
    }
}
