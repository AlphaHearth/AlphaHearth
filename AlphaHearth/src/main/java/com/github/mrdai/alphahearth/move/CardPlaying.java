package com.github.mrdai.alphahearth.move;

import info.hearthsim.brazier.PlayerId;
import info.hearthsim.brazier.TargetId;

public class CardPlaying implements SingleMove {
    private final PlayerId playerId;
    private final int cardIndex;
    private final int minionLocation;
    private final TargetId target;

    public CardPlaying(PlayerId playerId, int cardIndex) {
        this(playerId, cardIndex, -1, null);
    }

    public CardPlaying(PlayerId playerId, int cardIndex, TargetId target) {
        this(playerId, cardIndex, -1, target);
    }

    public CardPlaying(PlayerId playerId, int cardIndex, int minionLocation) {
        this(playerId, cardIndex, minionLocation, null);
    }

    public CardPlaying(PlayerId playerId, int cardIndex, int minionLocation, TargetId target) {
        this.playerId = playerId;
        this.cardIndex = cardIndex;
        this.minionLocation = minionLocation;
        this.target = target;
    }

    /**
     * Returns the index of the card to be played.
     */
    public int getCardIndex() {
        return cardIndex;
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

    public int getMinionLocation() {
        return minionLocation;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }
}
