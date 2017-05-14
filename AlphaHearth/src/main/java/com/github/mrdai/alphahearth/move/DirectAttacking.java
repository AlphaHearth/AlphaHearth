package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;
import info.hearthsim.brazier.game.Character;
import info.hearthsim.brazier.game.EntityId;
import info.hearthsim.brazier.game.Game;
import info.hearthsim.brazier.game.Hero;
import org.jtrim.utils.ExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectAttacking extends AbstractSingleMove {
    private static final Logger LOG = LoggerFactory.getLogger(DirectAttacking.class);

    private final int attackerIndex;
    private final int targetIndex;

    public DirectAttacking(Character attacker, Character target) {
        ExceptionHelper.checkNotNullArgument(attacker, "attacker");
        ExceptionHelper.checkNotNullArgument(target, "target");

        setConstructPoint();

        if (attacker instanceof Hero) {
            this.attackerIndex = 8;
        } else {
            this.attackerIndex = attacker.getOwner().getBoard().indexOf(attacker.getEntityId());
        }

        if (target instanceof Hero) {
            this.targetIndex = 8;
        } else {
            this.targetIndex = target.getOwner().getBoard().indexOf(target.getEntityId());
        }
    }

    public String toString(Board board) {
        Game game = board.getGame();
        String attackerName;
        if (attackerIndex == 8)
            attackerName = game.getCurrentPlayer().getPlayerId().getName();
        else
            attackerName = game.getCurrentPlayer().getBoard().getMinion(attackerIndex).toString();
        String targetName;
        if (targetIndex == 8)
            targetName = game.getOpponent(game.getCurrentPlayer().getPlayerId()).getPlayerId().getName();
        else
            targetName = game.getOpponent(game.getCurrentPlayer().getPlayerId()).getBoard().getMinion(targetIndex).toString();
        return attackerName + " attacks " + targetName;
    }

    @Override
    public void applyToUnsafe(Board board, boolean logMove) {
        if (logMove)
            LOG.info(toString(board));
        else if (LOG.isTraceEnabled())
            LOG.trace(toString(board));

        Game game = board.getGame();

        EntityId attackerId;
        if (attackerIndex == 8)
            attackerId = game.getCurrentPlayer().getHero().getEntityId();
        else
            attackerId = game.getCurrentPlayer().getBoard().getMinion(attackerIndex).getEntityId();
        EntityId targetId;
        if (targetIndex == 8)
            targetId = game.getCurrentOpponent().getHero().getEntityId();
        else
            targetId = game.getCurrentOpponent().getBoard().getMinion(targetIndex).getEntityId();
        board.playAgent.attack(attackerId, targetId);
    }

    public String toString() {
        return String.format("DirectAttacking[attacker: %s, target: %s]", attackerIndex, targetIndex);
    }
}
