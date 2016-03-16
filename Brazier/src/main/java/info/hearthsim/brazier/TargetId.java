package info.hearthsim.brazier;

/**
 * A stub class, providing its {@link #hashCode()} method for distinguishing different characters in
 * a Hearthstone game. As copying a {@link Game} deeply is necessary for AI module, different
 * characters in different {@code Game}s may have same {@code TargetId}, as one of them is essentially
 * a copy of another, but different characters in a {@code Game} must have different {@code TargetId}.
 */
public final class TargetId {}
