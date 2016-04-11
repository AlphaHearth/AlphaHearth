package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;
import info.hearthsim.brazier.game.EntityId;
import info.hearthsim.brazier.game.Game;
import org.jtrim.utils.ExceptionHelper;

public class DirectAttacking implements SingleMove {
    private final int attackerIndex;
    private final int targetIndex;

    public DirectAttacking(int attacker, int target) {
        ExceptionHelper.checkArgumentInRange(attacker, 0, 8, "attacker");
        ExceptionHelper.checkArgumentInRange(target, 0, 8, "target");

        this.attackerIndex = attacker;
        this.targetIndex = target;
    }

    public int getTarget() {
        return targetIndex;
    }

    public int getAttacker() {
        return attackerIndex;
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

    public String toString() {
        return String.format("DirectAttacking[attacker: %s, target: %s]", attackerIndex, targetIndex);
    }
}
