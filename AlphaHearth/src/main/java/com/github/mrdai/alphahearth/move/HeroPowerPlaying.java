package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;
import info.hearthsim.brazier.actions.PlayTargetRequest;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Move designating the current player playing its hero power with a potential target.
 */
public class HeroPowerPlaying extends AbstractSingleMove {
    private static final Logger LOG = LoggerFactory.getLogger(HeroPowerPlaying.class);

    private final PlayerId playerId;
    private final boolean isTargetFriendly;
    private final int targetIndex;

    public HeroPowerPlaying(PlayerId playerId) {
        this(playerId, null);
    }

    public HeroPowerPlaying(PlayerId playerId, Character target) {
        setConstructPoint();

        this.playerId = playerId;

        if (target == null) {
            targetIndex = -1;
            isTargetFriendly = false;
        } else {
            this.isTargetFriendly = target.getOwner().getPlayerId() == playerId;
            if (target instanceof Hero) {
                this.targetIndex = 8;
            } else {
                this.targetIndex = target.getOwner().getBoard().indexOf(target.getEntityId());
            }
        }
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    public String toString(Board board) {
        Game game = board.getGame();
        StringBuilder builder = new StringBuilder(playerId + " uses hero power");
        if (targetIndex != -1) {
            builder.append(" with target ");
            Player targetOwner = isTargetFriendly ? game.getPlayer(playerId) : game.getOpponent(playerId);
            EntityId targetId;
            if (targetIndex == 8)
                targetId = targetOwner.getHero().getEntityId();
            else
                targetId = targetOwner.getBoard().getMinion(targetIndex).getEntityId();
            Entity eTarget = game.findEntity(targetId);
            if (eTarget instanceof Hero)
                builder.append(eTarget.getOwner().getPlayerId().getName());
            else
                builder.append(eTarget);
        }
        return builder.toString();
    }

    @Override
    public void applyToUnsafe(Board board, boolean logMove) {
        if (logMove)
            LOG.info(toString(board));
        else if (LOG.isTraceEnabled())
            LOG.trace(toString(board));

        Game game = board.getGame();
        Player targetOwner = isTargetFriendly ? game.getPlayer(playerId) : game.getOpponent(playerId);
        EntityId targetId;
        if (targetIndex == -1) {
            targetId = null;
        } else {
            if (targetIndex == 8)
                targetId = targetOwner.getHero().getEntityId();
            else
                targetId = targetOwner.getBoard().getMinion(targetIndex).getEntityId();
        }
        board.playAgent.playHeroPower(new PlayTargetRequest(playerId, -1, targetId));
    }

    public String toString() {
        return String.format("HeroPowerPlaying[playerId: %s, isTargetFriendly: %b, target: %s]", playerId, isTargetFriendly, targetIndex);
    }
}
