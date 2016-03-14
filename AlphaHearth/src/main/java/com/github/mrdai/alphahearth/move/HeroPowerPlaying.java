package com.github.mrdai.alphahearth.move;

import info.hearthsim.brazier.PlayerId;
import info.hearthsim.brazier.TargetId;

/**
 * Move designating the current player playing its hero power with a potential target.
 */
public class HeroPowerPlaying implements SingleMove {

    private final PlayerId playerId;
    private final TargetId target;

    public HeroPowerPlaying(PlayerId playerId) {
        this(playerId, null);
    }

    public HeroPowerPlaying(PlayerId playerId, TargetId target) {
        this.playerId = playerId;
        this.target = target;
    }

    /**
     * Returns the target; returns {@code null} if there is no target.
     */
    public TargetId getTarget() {
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
