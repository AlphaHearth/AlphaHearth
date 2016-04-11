package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;
import info.hearthsim.brazier.game.*;

/**
 * Move designating the current player playing its hero power with a potential target.
 */
public class HeroPowerPlaying implements SingleMove {

    private final PlayerId playerId;
    private final EntityId target;

    public HeroPowerPlaying(PlayerId playerId) {
        this(playerId, null);
    }

    public HeroPowerPlaying(PlayerId playerId, EntityId target) {
        this.playerId = playerId;
        this.target = target;
    }

    /**
     * Returns the target; returns {@code null} if there is no target.
     */
    public EntityId getTarget() {
        return target;
    }

    /**
     * Returns if there is any target for this move.
     */
    public boolean hasTarget() {
        return target == null;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    public String toString(Board board) {
        Game game = board.getGame();
        StringBuilder builder = new StringBuilder(playerId + " uses hero power");
        if (target != null) {
            builder.append(" with target ");
            Entity eTarget = game.findEntity(target);
            if (eTarget instanceof Hero)
                builder.append(eTarget.getOwner().getPlayerId().getName());
            else
                builder.append(eTarget);
        }
        return builder.toString();
    }

    public String toString() {
        return String.format("HeroPowerPlaying[playerId: %s, target: %s]", playerId, target);
    }
}
