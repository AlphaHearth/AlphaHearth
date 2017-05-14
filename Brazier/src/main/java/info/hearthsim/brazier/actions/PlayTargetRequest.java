package info.hearthsim.brazier.actions;

import info.hearthsim.brazier.db.CardDescr;
import info.hearthsim.brazier.game.EntityId;
import info.hearthsim.brazier.game.PlayerId;
import org.jtrim.utils.ExceptionHelper;

/**
 * A {@code PlayTargetRequest} contains the basic arguments of a play action, including
 * the casting player, the target and the location of the minion to summon. Most of the time,
 * it acts as containers of methods' arguments.
 */
public final class PlayTargetRequest {
    private final PlayerId castingPlayerId;

    private final int minionLocation;
    private final EntityId targetId;

    private final CardDescr choseOneChoice;

    public PlayTargetRequest(PlayerId castingPlayerId) {
        this(castingPlayerId, -1, null);
    }

    public PlayTargetRequest(
            PlayerId castingPlayerId,
            int minionLocation,
            EntityId targetId) {
        this(castingPlayerId, minionLocation, targetId, null);
    }

    public PlayTargetRequest(
            PlayerId castingPlayerId,
            int minionLocation,
            EntityId targetId,
            CardDescr choseOneChoice) {
        ExceptionHelper.checkNotNullArgument(castingPlayerId, "castingPlayerId");

        this.castingPlayerId = castingPlayerId;
        this.minionLocation = minionLocation;
        this.targetId = targetId;
        this.choseOneChoice = choseOneChoice;
    }

    public PlayerId getCastingPlayerId() {
        return castingPlayerId;
    }

    public int getMinionLocation() {
        return minionLocation;
    }

    public EntityId getEntityId() {
        return targetId;
    }

    public CardDescr getChoseOneChoice() {
        return choseOneChoice;
    }
}
