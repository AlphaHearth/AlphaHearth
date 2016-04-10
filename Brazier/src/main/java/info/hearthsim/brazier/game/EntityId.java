package info.hearthsim.brazier.game;

/**
 * A stub class, providing its {@link #hashCode()} method for distinguishing different entities in
 * a Hearthstone game. As copying a {@link Game} deeply is necessary for AI module, different
 * entities in different {@code Game}s may have same {@code EntityId}, as one of them is essentially
 * a copy of another, but different entities in a {@code Game} must have different {@code EntityId}.
 */
public class EntityId {}
