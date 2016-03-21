package com.github.mrdai.alphahearth.move;

import info.hearthsim.brazier.EntityId;
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
}
