package com.github.mrdai.alphahearth.move;

import info.hearthsim.brazier.EntityId;
import info.hearthsim.brazier.PlayerId;

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
}
