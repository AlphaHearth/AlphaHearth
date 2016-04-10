package info.hearthsim.brazier.game;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.game.cards.Card;
import info.hearthsim.brazier.game.minions.Minion;
import info.hearthsim.brazier.game.weapons.Weapon;

/**
 * Entity in a Hearthstone game, which can be {@link Hero}, {@link Card}, {@link Minion},
 * {@link Player}, {@link Weapon}.
 */
public interface Entity<T> extends PlayerProperty {
    public EntityId getEntityId();

    public T copyFor(Game newGame, Player newPlayer);
}
