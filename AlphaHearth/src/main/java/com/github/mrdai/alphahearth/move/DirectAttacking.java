package com.github.mrdai.alphahearth.move;

import com.github.mrdai.alphahearth.Board;
import info.hearthsim.brazier.game.EntityId;
import info.hearthsim.brazier.game.Game;
import org.jtrim.utils.ExceptionHelper;

public class DirectAttacking implements SingleMove {
    private final EntityId attacker;
    private final EntityId target;

    public DirectAttacking(EntityId attacker, EntityId target) {
        ExceptionHelper.checkNotNullArgument(attacker, "attacker");
        ExceptionHelper.checkNotNullArgument(target, "target");

        this.attacker = attacker;
        this.target = target;
    }

    public EntityId getTarget() {
        return target;
    }

    public EntityId getAttacker() {
        return attacker;
    }

    public String toString(Board board) {
        Game game = board.getGame();
        return game.findEntity(attacker) + " attacks " + game.findEntity(target);
    }

    public String toString() {
        return String.format("DirectAttacking[attacker: %s, target: %s]", attacker, target);
    }
}
