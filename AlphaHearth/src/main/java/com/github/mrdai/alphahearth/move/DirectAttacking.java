package com.github.mrdai.alphahearth.move;

import info.hearthsim.brazier.TargetId;
import org.jtrim.utils.ExceptionHelper;

public class DirectAttacking implements SingleMove {
    private final TargetId attacker;
    private final TargetId target;

    public DirectAttacking(TargetId attacker, TargetId target) {
        ExceptionHelper.checkNotNullArgument(attacker, "attacker");
        ExceptionHelper.checkNotNullArgument(target, "target");

        this.attacker = attacker;
        this.target = target;
    }

    public TargetId getTarget() {
        return target;
    }

    public TargetId getAttacker() {
        return attacker;
    }
}
