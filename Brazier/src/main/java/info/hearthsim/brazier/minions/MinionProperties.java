package info.hearthsim.brazier.minions;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.abilities.*;
import info.hearthsim.brazier.events.EventAction;
import info.hearthsim.brazier.Game;
import info.hearthsim.brazier.weapons.AttackTool;

import java.util.ArrayList;
import java.util.List;

import org.jtrim.utils.ExceptionHelper;

public final class MinionProperties implements Silencable {
    private final Minion minion;
    private final MinionAttackTool attackTool;
    private final MinionBody body;
    private final AbilityList<Minion> abilities;
    private List<EventAction<? super Minion, ? super Minion>> deathRattles;
    private boolean activated;
    private boolean silenced = false;

    public MinionProperties(Minion minion, MinionDescr baseDescr) {
        ExceptionHelper.checkNotNullArgument(minion, "minion");
        ExceptionHelper.checkNotNullArgument(baseDescr, "baseDescr");

        this.minion = minion;
        this.attackTool = new MinionAttackTool(baseDescr);
        this.body = new MinionBody(minion, baseDescr);
        this.abilities = new AbilityList<>(minion);
        this.deathRattles = new ArrayList<>();
        this.activated = false;

        EventAction<? super Minion, ? super Minion> baseDeathRattle = baseDescr.tryGetDeathRattle();
        if (baseDeathRattle != null) {
            this.deathRattles.add(baseDeathRattle);
        }
    }

    private MinionProperties(
        Minion minion,
        MinionProperties baseProperties,
        AbilityList<Minion> abilities) {

        ExceptionHelper.checkNotNullArgument(minion, "minion");
        ExceptionHelper.checkNotNullArgument(baseProperties, "baseProperties");
        ExceptionHelper.checkNotNullArgument(abilities, "abilities");

        this.minion = minion;
        this.attackTool = new MinionAttackTool(baseProperties.attackTool);
        this.body = baseProperties.body.copyFor(minion);
        this.abilities = abilities;
        this.deathRattles = new ArrayList<>(baseProperties.deathRattles);
        this.activated = false;
        this.silenced = baseProperties.silenced;
    }

    /**
     * Returns a copy of this {@code MinionProperties} for the given new minion.
     */
    public MinionProperties copyFor(Minion newMinion) {
        return copyFor(newMinion, false, false);
    }

    /**
     * Returns a copy of this {@code MinionProperties} for the given new minion.
     *
     * @param copyAbilities whether to copy the abilities added to this {@code MinionProperties}
     * @param copyActivated whether to copy the {@code activated} field of this {@code MinionProperties},
     *                      or set it to {@code false}.
     */
    public MinionProperties copyFor(Minion newMinion, boolean copyAbilities, boolean copyActivated) {
        AbilityList<Minion> newAbilities;
        newAbilities = abilities.copyFor(newMinion, copyAbilities);
        MinionProperties result = new MinionProperties(newMinion, this, newAbilities);
        if (copyActivated)
            result.activated = activated;
        return result;
    }

    /**
     * Sets the minion to be exhausted.
     */
    public void exhaust() {
        attackTool.exhausted = true;
    }

    public void activatePassiveAbilities() {
        if (activated || silenced)
            return;

        activated = true;

        MinionDescr baseStats = body.getBaseStats();
        addAndActivateAbility(baseStats.getEventActionDefs());

        Ability<? super Minion> ability = baseStats.tryGetAbility();
        if (ability != null)
            addAndActivateAbility(ability);
    }

    public void deactivateAllAbilities() {
        if (!activated)
            return;
        activated = false;
        abilities.deactivate();
    }

    public void refreshStartOfTurn() {
        attackTool.refreshStartOfTurn();
    }

    public void refreshEndOfTurn() {
        attackTool.refreshEndOfTurn();
    }

    public void updateAuras() {
        body.applyAuras();
    }

    private Game getGame() {
        return minion.getGame();
    }

    public void triggerDeathRattles() {
        triggerDeathRattles(1);
    }

    public void triggerDeathRattles(int numberOfTriggers) {
        if (deathRattles.isEmpty())
            return;

        for (EventAction<? super Minion, ? super Minion> deathRattle : new ArrayList<>(deathRattles)) {
            for (int i = 0; i < numberOfTriggers; i++)
                deathRattle.trigger(minion, minion);
        }
    }

    private void removeDeathRattles() {
        deathRattles.clear();
    }

    @Override
    public void silence() {
        silenced = true;
        abilities.deactivate();
        attackTool.silence();
        body.silence();
        removeDeathRattles();
    }

    public boolean isFrozen() {
        return attackTool.isFrozen();
    }

    public boolean isCharge() {
        return attackTool.charge.getValue();
    }

    public AttackTool getAttackTool() {
        return attackTool;
    }

    public AuraAwareIntProperty getBuffableAttack() {
        return attackTool.attack;
    }

    public MinionBody getBody() {
        return body;
    }

    public void addAndActivateAbility(Ability<? super Minion> abilityRegisterTask) {
        addAndActivateAbility(abilityRegisterTask, false, false);
    }

    public void addAndActivateAbility(Ability<? super Minion> abilityRegisterTask, boolean toCopy, boolean needsReactivate) {
        if (minion.isDestroyed())
            return;

        abilities.addAndActivateAbility(abilityRegisterTask, toCopy, needsReactivate);
    }

    public void setAttackFinalizer(OwnedIntPropertyBuff<? super Minion> newAttackFinalizer) {
        attackTool.setAttackFinalizer(newAttackFinalizer);
    }

    public void setCharge(boolean newCharge) {
        attackTool.setCharge(newCharge);
    }

    public AuraAwareIntProperty getMaxAttackCountProperty() {
        return attackTool.maxAttackCount;
    }

    public AuraAwareBoolProperty getChargeProperty() {
        return attackTool.charge;
    }

    public boolean isDeathRattle() {
        return !deathRattles.isEmpty();
    }

    public void addDeathRattle(EventAction<? super Minion, ? super Minion> deathRattle) {
        ExceptionHelper.checkNotNullArgument(deathRattle, "deathRattle");

        deathRattles.add(deathRattle);
    }

    private final class MinionAttackTool implements AttackTool {
        private final FreezeManager freezeManager;
        private final AuraAwareIntProperty attack;
        private boolean canAttack;
        private int attackCount;
        private final AuraAwareIntProperty maxAttackCount;
        private boolean exhausted;
        private final AuraAwareBoolProperty charge;
        private OwnedIntPropertyBuff<? super Minion> attackFinalizer;

        private boolean attackLeft;
        private boolean attackRight;

        public MinionAttackTool(MinionDescr baseDescr) {
            this.attack = new AuraAwareIntProperty(baseDescr.getAttack(), 0);
            this.freezeManager = new FreezeManager();
            this.maxAttackCount = new AuraAwareIntProperty(baseDescr.getMaxAttackCount());
            this.attackCount = 0;
            this.canAttack = baseDescr.isCanAttack();
            this.exhausted = true;
            this.charge = new AuraAwareBoolProperty(baseDescr.isCharge());
            this.attackLeft = baseDescr.isAttackLeft();
            this.attackRight = baseDescr.isAttackRight();
            this.attackFinalizer = baseDescr.getAttackFinalizer();
        }

        public MinionAttackTool(MinionAttackTool base) {
            this.attack = base.attack.copy();
            this.canAttack = base.canAttack;
            this.attackCount = base.attackCount;
            this.maxAttackCount = base.maxAttackCount.copy();
            this.freezeManager = base.freezeManager.copy();
            this.exhausted = base.exhausted;
            this.charge = base.charge.copy();
            this.attackLeft = base.attackLeft;
            this.attackRight = base.attackRight;
            this.attackFinalizer = base.attackFinalizer;
        }

        public void setAttackFinalizer(OwnedIntPropertyBuff<? super Minion> newAttackFinalizer) {
            ExceptionHelper.checkNotNullArgument(newAttackFinalizer, "newAttackFinalizer");
            attackFinalizer = newAttackFinalizer;
        }

        public void setCharge(boolean newCharge) {
            charge.setValueTo(newCharge);
        }

        private void removeSwipeAttack() {
            if (!attackLeft && !attackRight)
                return;

            attackLeft = false;
            attackRight = false;
        }

        public void silence() {
            if (!canAttack)
                canAttack = true;

            attackFinalizer = OwnedIntPropertyBuff.IDENTITY;

            removeSwipeAttack();
            maxAttackCount.silence();
            freezeManager.silence();
            attack.silence();
            charge.silence();
        }

        @Override
        public void refreshStartOfTurn() {
            attackCount = 0;
            exhausted = false;
        }

        @Override
        public void refreshEndOfTurn() {
            freezeManager.endTurn(attackCount);
        }

        @Override
        public int getMaxAttackCount() {
            return maxAttackCount.getValue();
        }

        @Override
        public void freeze() {
            freezeManager.freeze();
        }

        @Override
        public int getAttack() {
            return attackFinalizer.buffProperty(minion, attack.getValue());
        }

        @Override
        public boolean canAttackWith() {
            if (!canAttack) {
                return false;
            }

            if (exhausted && !charge.getValue()) {
                return false;
            }

            return attackCount < maxAttackCount.getValue() && !isFrozen() && getAttack() > 0;
        }

        @Override
        public boolean canRetaliateWith() {
            return true;
        }

        @Override
        public boolean canTargetRetaliate() {
            return true;
        }

        @Override
        public boolean attacksLeft() {
            return attackLeft;
        }

        @Override
        public boolean attacksRight() {
            return attackRight;
        }

        @Override
        public void incAttackCount() {
            body.setStealth(false);
            attackCount++;
        }

        @Override
        public boolean isFrozen() {
            return freezeManager.isFrozen();
        }
    }
}
