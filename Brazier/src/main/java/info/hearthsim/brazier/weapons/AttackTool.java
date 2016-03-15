package info.hearthsim.brazier.weapons;

import info.hearthsim.brazier.actions.undo.UndoAction;

public interface AttackTool {
    /**
     * Refreshes the character's attack state at start of turn: that is,
     * exhausted character's attack count will be reset.
     */
    public UndoAction refreshStartOfTurn();

    /**
     * Refreshes the character's attack state at end of turn: that is,
     * frozen character will be unfrozen.
     */
    public UndoAction refreshEndOfTurn();

    /**
     * Gets the attack point of this entity.
     */
    public int getAttack();

    /**
     * Returns if the entity can be used to attack.
     */
    public boolean canAttackWith();

    /**
     * Returns if the entity can retaliate.
     */
    public boolean canRetaliateWith();

    /**
     * Returns if the target being attacked by this entity can retaliate.
     */
    public boolean canTargetRetaliate();

    /**
     * Returns if the entity can also damage the minion on the left of its target.
     */
    public boolean attacksLeft();

    /**
     * Returns if the entity can also damage the minion on the right of its target.
     */
    public boolean attacksRight();

    /**
     * Increases the attack count of this entity.
     */
    public UndoAction incAttackCount();

    /**
     * Freezes the entity.
     */
    public UndoAction freeze();

    /**
     * Returns if the entity is frozen.
     */
    public boolean isFrozen();
}