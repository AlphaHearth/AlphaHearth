package info.hearthsim.brazier.ui;

import info.hearthsim.brazier.TargeterDef;
import info.hearthsim.brazier.game.Character;
import org.jtrim.event.ListenerRef;
import org.jtrim.utils.ExceptionHelper;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public final class AttackTargetNeed {
    private final TargeterDef targeterDef;

    public AttackTargetNeed(TargeterDef targeterDef) {
        ExceptionHelper.checkNotNullArgument(targeterDef, "targeterDef");

        this.targeterDef = targeterDef;
    }

    public TargeterDef getTargeterDef() {
        return targeterDef;
    }

    public static ListenerRef trackForTarget(
            TargetManager targetManager,
            JComponent component,
            Character target,
            Consumer<Boolean> highlightSetter) {
        ExceptionHelper.checkNotNullArgument(targetManager, "targetManager");
        ExceptionHelper.checkNotNullArgument(component, "component");
        ExceptionHelper.checkNotNullArgument(target, "target");
        ExceptionHelper.checkNotNullArgument(highlightSetter, "highlightSetter");

        MouseListener listener = new MouseAdapter() {
            private boolean isAllowedTarget(AttackTargetNeed need) {
                return target.isTargetable(need.targeterDef);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                UiTargetCondition condition = targetManager.getCondition();
                if (condition != null) {
                    Object conditionObj = condition.getCondition();
                    if (conditionObj instanceof AttackTargetNeed) {
                        if (isAllowedTarget((AttackTargetNeed)conditionObj)) {
                            highlightSetter.accept(false);
                            condition.getCallback().accept(target.getEntityId());
                        }
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                highlightSetter.accept(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                Object conditionObj = targetManager.getConditionObj();
                if (conditionObj instanceof AttackTargetNeed) {
                    highlightSetter.accept(isAllowedTarget((AttackTargetNeed)conditionObj));
                }
            }
        };
        component.addMouseListener(listener);
        return new ListenerRef() {
            private volatile boolean registered = true;

            @Override
            public boolean isRegistered() {
                return registered;
            }

            @Override
            public void unregister() {
                component.removeMouseListener(listener);
                registered = false;
            }
        };
    }
}
