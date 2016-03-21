package info.hearthsim.brazier;

import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.weapons.Weapon;

/**
 * Entity in a Hearthstone game, which can be {@link Hero}, {@link Card}, {@link Minion},
 * {@link Player}, {@link Weapon}.
 */
public interface Entity<T> extends PlayerProperty {
    public EntityId getEntityId();

    public T copyFor(Game newGame, Player newPlayer);
}
