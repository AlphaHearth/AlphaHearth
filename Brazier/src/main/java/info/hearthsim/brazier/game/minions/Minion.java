package info.hearthsim.brazier.game.minions;

import info.hearthsim.brazier.TargeterDef;
import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.abilities.AuraAwareIntProperty;
import info.hearthsim.brazier.actions.CardRef;
import info.hearthsim.brazier.db.MinionDescr;
import info.hearthsim.brazier.events.EventAction;
import info.hearthsim.brazier.events.GameEvents;
import info.hearthsim.brazier.events.SimpleEventType;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.*;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.weapons.AttackTool;
import org.jtrim.utils.ExceptionHelper;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Minion implements Character<Minion>, DestroyableEntity, Silencable, CardRef {
    private final EntityId minionId;
    private Player owner;
    private MinionProperties properties;

    private final long birthDate;

    private final AtomicBoolean scheduledToDestroy;
    private final AtomicBoolean destroyed;

    /**
     * Creates a {@code Minion} with the given {@code MinionDescr} and the given {@code Player} as its owner.
     *
     * @param owner the given {@code Player}.
     * @param baseDescr the given {@code MinionDescr}.
     */
    public Minion(Player owner, MinionDescr baseDescr) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        ExceptionHelper.checkNotNullArgument(baseDescr, "baseDescr");

        this.owner = owner;
        this.minionId = new EntityId();
        this.properties = new MinionProperties(this, baseDescr);
        this.birthDate = owner.getGame().getCurrentTime();
        this.destroyed = new AtomicBoolean(false);
        this.scheduledToDestroy = new AtomicBoolean(false);
    }

    /**
     * Creates a copy of the given {@code Minion} with the given new {@link Player owner}.
     * The new copy will have the same {@link EntityId} as the given {@code Minion}.
     *
     * @param newOwner the given new owner.
     * @param minion the given {@code Minion} to be copied.
     */
    private Minion(Player newOwner, Minion minion) {
        ExceptionHelper.checkNotNullArgument(newOwner, "newOwner");
        ExceptionHelper.checkNotNullArgument(minion, "minion");

        this.owner = newOwner;
        this.minionId = minion.minionId;
        this.birthDate = minion.birthDate;
        this.destroyed = new AtomicBoolean(minion.destroyed.get());
        this.scheduledToDestroy = new AtomicBoolean(minion.scheduledToDestroy.get());
        // All the abilities will be copied, but they will be reactivated for this new Minion.
        this.properties = minion.properties.copyFor(this, false, false);
    }

    @Override
    public void scheduleToDestroy() {
        if (!scheduledToDestroy.compareAndSet(false, true))
            return;

        getProperties().deactivateAllAbilities();
        getOwner().getBoard().scheduleToDestroy(minionId);
    }

    @Override
    public Card getCard() {
        return new Card(owner, getBaseDescr().getBaseCard());
    }

    public boolean notScheduledToDestroy() {
        return !isScheduledToDestroy();
    }

    @Override
    public boolean isScheduledToDestroy() {
        return scheduledToDestroy.get();
    }

    /**
     * Destroys (or kills) the minion. A {@link SimpleEventType#MINION_KILLED MINION_KILLED} event
     * and the minion's death-rattle effect should be triggered.
     */
    @Override
    public void destroy() {

        // By now, the `scheduleToDestroy` method has already been invoked,
        // and the `needSpace` of the dead minion has been set to `false`.
        completeKillAndDeactivate(true);
        owner.getBoard().removeFromBoard(minionId);
    }

    /**
     * Transforms this {@code Minion} to another minion described by the given {@link MinionDescr}.
     * All previously-added buffs and abilities on this minion will be removed and a brand-new version
     * of the given minion will spawn on its original location.
     */
    public void transformTo(MinionDescr newDescr) {
        ExceptionHelper.checkNotNullArgument(newDescr, "newDescr");

        properties.deactivateAllAbilities();
        properties = new MinionProperties(this, newDescr);
        properties.activatePassiveAbilities();
    }

    /**
     * Transforms this {@code Minion} to a copy of the given {@code Minion}.
     * All previously-added buffs and abilities on the given minion will be copied to the transformed minion.
     * The minion will be <em>exhausted</em> when it is transformed.
     */
    public void copyOther(Minion other) {
        ExceptionHelper.checkNotNullArgument(other, "other");

        properties.deactivateAllAbilities();
        properties = other.properties.copyFor(this);
        properties.activatePassiveAbilities();
        properties.exhaust();
    }

    /**
     * Sets the minion to be exhausted.
     */
    public void exhaust() {
        properties.exhaust();
    }

    /**
     * Returns if the minion has <b>Charge</b> effect.
     */
    public boolean isCharge() {
        return properties.isCharge();
    }

    @Override
    public long getBirthDate() {
        return birthDate;
    }

    /**
     * Returns if the minion can be targeted by the targeting attempt designated by the given {@link TargeterDef}.
     */
    @Override
    public boolean isTargetable(TargeterDef targeterDef) {
        boolean sameOwner = targeterDef.hasSameOwner(this);
        if (!sameOwner && getBody().isStealth()) {
            return false;
        }
        if (sameOwner && targeterDef.isDirectAttack()) {
            return false;
        }

        MinionBody body = properties.getBody();

        if (!body.isTargetable() && targeterDef.isHero()) {
            return false;
        }

        if (body.isTaunt()) {
            return true;
        }

        return !targeterDef.isDirectAttack() || !getOwner().getBoard().hasNonStealthTaunt();
    }

    /**
     * Adds the given {@link EventAction} as a death rattle effect to this minion.
     */
    public void addDeathRattle(EventAction<? super Minion, ? super Minion> deathRattle) {
        properties.addDeathRattle(deathRattle);
    }

    /**
     * Adds the given {@link Ability} to this minion and activates it.
     */
    public void addAndActivateAbility(Ability<? super Minion> abilityRegisterTask) {
        properties.addAndActivateAbility(abilityRegisterTask);
    }

    /**
     * Activates the passive abilities of this minion.
     */
    public void activatePassiveAbilities() {
        properties.activatePassiveAbilities();
    }

    /**
     * Returns the {@link MinionProperties} of this {@code Minion}.
     */
    public MinionProperties getProperties() {
        return properties;
    }

    /**
     * Triggers a {@link SimpleEventType#MINION_KILLED} event for this minion.
     */
    private void triggerKilledEvents() {
        GameEvents events = getOwner().getGame().getEvents();
        events.triggerEvent(SimpleEventType.MINION_KILLED, this);
    }

    /**
     * Returns the {@link Keyword}s of this {@code Minion}.
     */
    @Override
    public Set<Keyword> getKeywords() {
        return getBaseDescr().getBaseCard().getKeywords();
    }

    /**
     * Kills or removes the minion from board.
     *
     * @param triggerKill whether to trigger {@link SimpleEventType#MINION_KILLED MINION_KILLED} event
     *                    and the minion's deathrattle effect in this action.
     */
    public void completeKillAndDeactivate(boolean triggerKill) {
        if (destroyed.compareAndSet(false, true)) {
            if (triggerKill)
                triggerKilledEvents();

            properties.deactivateAllAbilities();

            if (triggerKill)
                triggerDeathRattles();
        }
    }

    /**
     * Triggers the death rattle effects of this minion. The death rattle effects may be trigger multiple times
     * due to some specific aura effect.
     * <p>
     * See minion <em>Baron Rivendare</em>.
     */
    public void triggerDeathRattles() {
        if (!properties.isDeathRattle())
            return;

        int triggerCount = getOwner().getDeathRattleTriggerCount().getValue();
        properties.triggerDeathRattles(triggerCount);
    }

    @Override
    public void silence() {
        properties.silence();
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void kill() {
        getBody().poison();
    }

    public void setOwner(Player owner) {
        ExceptionHelper.checkNotNullArgument(owner, "owner");
        this.owner = owner;
    }

    public AuraAwareIntProperty getBuffableAttack() {
        return properties.getBuffableAttack();
    }

    @Override
    public AttackTool getAttackTool() {
        return properties.getAttackTool();
    }

    /**
     * Returns {@code true} if this minion has been completely destroyed and might no longer
     * be added to the board.
     *
     * @return {@code true} if this minion has been completely destroyed and might no longer
     *   be added to the board, {@code false} otherwise
     */
    public boolean isDestroyed() {
        return destroyed.get();
    }

    @Override
    public boolean isDead() {
        return getBody().isDead();
    }

    @Override
    public boolean isDamaged() {
        MinionBody body = getBody();
        return body.getCurrentHp() < body.getMaxHp();
    }

    @Override
    public EntityId getEntityId() {
        return minionId;
    }

    public MinionDescr getBaseDescr() {
        return getBody().getBaseStats();
    }

    public MinionBody getBody() {
        return properties.getBody();
    }

    public void setCharge(boolean newCharge) {
        properties.setCharge(newCharge);
    }

    @Override
    public Damage createDamage(int damage) {
        int preparedDamage = damage;
        if (damage < 0 && getOwner().getDamagingHealAura().getValue()) {
            preparedDamage = -damage;
        }
        return new Damage(this, preparedDamage);
    }

    @Override
    public boolean isLethalDamage(int damage) {
        return getBody().isLethalDamage(damage);
    }

    @Override
    public int damage(Damage damage) {
        return Hero.doPreparedDamage(damage, this, (appliedDamage) -> getBody().damage(appliedDamage));
    }

    /**
     * Refreshes the state of the minion at start of turn. That is,
     * sleeping minion will be awakened and the number of attacks will reset.
     */
    public void refreshStartOfTurn() {
        properties.refreshStartOfTurn();
    }

    /**
     * Refreshes the state of the minion at end of turn. That is,
     * frozen minion will be unfrozen.
     */
    public void refreshEndOfTurn() {
        properties.refreshEndOfTurn();
    }

    public void updateAuras() {
        properties.updateAuras();
    }

    /**
     * Returns a copy of this {@code Minion} with the given new {@link Player owner}.
     */
    public Minion copyFor(Game newGame, Player newOwner) {
        return new Minion(newOwner, this);
    }

    @Override
    public String toString() {
        MinionBody body = getBody();
        AttackTool attackTool = getAttackTool();
        int attack = attackTool.getAttack();

        MinionName id = body.getBaseStats().getId();
        int currentHp = body.getCurrentHp();

        StringBuilder result = new StringBuilder(64);
        result.append("Minion(");
        result.append(id);
        result.append(") ");
        result.append(attack);
        result.append("/");
        result.append(currentHp);

        if (body.isTaunt()) {
            result.append(" ");
            result.append(" TAUNT");
        }

        if (properties.isFrozen()) {
            result.append(" ");
            result.append(" FROZEN");
        }

        if (!body.isTargetable()) {
            result.append(" ");
            result.append(" UNTARGETABLE");
        }

        return result.toString();
    }
}
