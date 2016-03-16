package info.hearthsim.brazier.weapons;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.abilities.AbilityList;
import info.hearthsim.brazier.abilities.AuraAwareIntProperty;
import info.hearthsim.brazier.actions.undo.UndoAction;
import info.hearthsim.brazier.events.GameEventAction;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.events.GameActionEvents;
import info.hearthsim.brazier.actions.undo.UndoableResult;
import info.hearthsim.brazier.Game;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jtrim.utils.ExceptionHelper;

public final class Weapon implements DestroyableEntity, DamageSource, LabeledEntity {
    private final Player owner;
    private final WeaponDescr baseDescr;
    private final CharacterAbilities<Weapon> abilities;
    private final Ability<Weapon> deathRattle;
    private final long birthDate;

    private final AuraAwareIntProperty attack;
    private int durability;

    private final AtomicBoolean scheduledToDestroy;

    public Weapon(Player owner, WeaponDescr weaponDescr) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkNotNullArgument(weaponDescr, "weaponDescr");

        this.owner = owner;
        this.baseDescr = weaponDescr;
        this.attack = new AuraAwareIntProperty(weaponDescr.getAttack());
        this.durability = weaponDescr.getDurability();
        this.birthDate = owner.getOwner().getGame().getCurrentTime();
        this.abilities = new CharacterAbilities<>(this);
        this.scheduledToDestroy = new AtomicBoolean(false);

        GameEventAction<? super Weapon, ? super Weapon> deathRattleAction = baseDescr.tryGetDeathRattle();
        this.deathRattle = deathRattleAction != null ? deathRattleToAbility(deathRattleAction) : null;
    }

    private Weapon(Player newOwner, Weapon other) {
        ExceptionHelper.checkNotNullArgument(newOwner, "newOwner");
        ExceptionHelper.checkNotNullArgument(other, "other");

        this.owner = newOwner;
        this.baseDescr = other.baseDescr;
        this.attack = other.attack.copy();
        this.durability = other.durability;
        this.birthDate = other.birthDate;
        PreparedResult<CharacterAbilities<Weapon>> newAbilities = other.abilities.copyFor(this);
        this.abilities = newAbilities.getResult();
        newAbilities.activate();
        this.scheduledToDestroy = new AtomicBoolean(other.isScheduledToDestroy());
        this.deathRattle = other.deathRattle;
    }

    /**
     * Returns a copy of this {@code Weapon} with the given new {@link Player owner}.
     */
    public Weapon copyFor(Player newOwner) {
        return new Weapon(newOwner, this);
    }

    /**
     * Activates the weapon's passive abilities for the current {@code Game}.
     */
    public UndoAction activatePassiveAbilities() {
        AbilityList<Weapon> ownedAbilities = abilities.getOwned();

        UndoAction.Builder result = new UndoAction.Builder();

        result.addUndo(ownedAbilities.addAndActivateAbility(baseDescr.getEventActionDefs()));

        Ability<? super Weapon> ability = baseDescr.tryGetAbility();
        if (ability != null) {
            result.addUndo(ownedAbilities.addAndActivateAbility(ability));
        }

        if (deathRattle != null) {
            result.addUndo(ownedAbilities.addAndActivateAbility(deathRattle));
        }

        return result;
    }

    public UndoAction deactivateAllAbilities() {
        return abilities.deactivateAll();
    }

    @Override
    public UndoAction scheduleToDestroy() {
        if (!scheduledToDestroy.compareAndSet(false, true)) {
            return UndoAction.DO_NOTHING;
        }
        return () -> scheduledToDestroy.set(false);
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
    public UndoableResult<Damage> createDamage(int damage) {
        return new UndoableResult<>(new Damage(this, damage));
    }

    public UndoAction increaseDurability() {
        return increaseDurability(1);
    }

    public UndoAction increaseDurability(int amount) {
        if (durability == Integer.MAX_VALUE || amount == 0) {
            return UndoAction.DO_NOTHING;
        }

        durability += amount;
        return () -> durability -= amount;
    }

    public UndoAction decreaseCharges() {
        if (durability == Integer.MAX_VALUE) {
            return UndoAction.DO_NOTHING;
        }

        durability--;
        return () -> durability++;
    }

    public int getAttack() {
        return attack.getValue();
    }

    public AuraAwareIntProperty getBuffableAttack() {
        return attack;
    }

    public UndoAction setAttack(int attack) {
        return this.attack.setValueTo(attack);
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
    public UndoAction destroy() {
        UndoAction eventUndo = owner.getGame().getEvents()
            .triggerEvent(SimpleEventType.WEAPON_DESTROYED, this);

        // TODO: If we want to deactivate the abilities first, we have to
        //       adjust death-rattle handling not to get disabled.
        UndoAction deactivateUndo = deactivateAllAbilities();

        return () -> {
            deactivateUndo.undo();
            eventUndo.undo();
        };
    }

    private static Ability<Weapon> deathRattleToAbility(
            GameEventAction<? super Weapon, ? super Weapon> deathRattle) {
        ExceptionHelper.checkNotNullArgument(deathRattle, "deathRattle");

        return (Weapon self) -> {
            GameActionEvents<Weapon> listeners = self.getGame().getEvents()
                    .simpleListeners(SimpleEventType.WEAPON_DESTROYED);
            return listeners.addAction((Game game, Weapon target) -> {
                if (target == self) {
                    return deathRattle.alterGame(game, self, target);
                }
                else {
                    return UndoAction.DO_NOTHING;
                }
            });
        };
    }

    @Override
    public String toString() {
        return "Weapon{" + ", attack=" + attack + ", durability=" + durability + '}';
    }
}
