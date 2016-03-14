package info.hearthsim.brazier;

import info.hearthsim.brazier.cards.CardDescr;
import info.hearthsim.brazier.minions.MinionDescr;
import info.hearthsim.brazier.weapons.WeaponDescr;

/**
 * Entity in Hearthstone. Known implementations include {@link CardDescr CardDescr},
 * {@link MinionDescr MinionDescr} and
 * {@link WeaponDescr WeaponDescr}.
 */
public interface HearthStoneEntity extends LabeledEntity {
    public EntityId getId();
}
