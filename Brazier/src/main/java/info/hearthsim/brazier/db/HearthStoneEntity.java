package info.hearthsim.brazier.db;

import info.hearthsim.brazier.game.EntityName;
import info.hearthsim.brazier.game.LabeledEntity;

/**
 * Entity in Hearthstone. Known implementations include {@link CardDescr CardDescr},
 * {@link MinionDescr MinionDescr} and
 * {@link WeaponDescr WeaponDescr}.
 */
public interface HearthStoneEntity extends LabeledEntity {
    public EntityName getId();
}
