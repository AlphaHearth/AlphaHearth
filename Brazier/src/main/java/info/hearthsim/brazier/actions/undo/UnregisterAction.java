package info.hearthsim.brazier.actions.undo;

import info.hearthsim.brazier.*;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.weapons.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@FunctionalInterface
public interface UnregisterAction {
    public static final UnregisterAction DO_NOTHING = (game) -> {};

    public void unregister(Game game);

    public static UnregisterAction forHero(Hero hero, UndoObjectAction<Hero> action) {
        return forHero(hero.getEntityId(), action);
    }

    public static UnregisterAction forHero(EntityId heroId, UndoObjectAction<Hero> action) {
        return (game) -> {
            Hero hero = game.getHero(heroId);
            if (hero != null)
                action.undo(hero);
        };
    }

    public static UnregisterAction forPlayer(Player player, UndoObjectAction<Player> action) {
        return forPlayer(player.getPlayerId(), action);
    }

    public static UnregisterAction forPlayer(PlayerId playerId, UndoObjectAction<Player> action) {
        return (game) -> {
            Player player = game.getPlayer(playerId);
            if (player != null)
                action.undo(player);
        };
    }

    public static UnregisterAction forWeapon(Weapon weapon, UndoObjectAction<Weapon> action) {
        return forWeapon(weapon.getEntityId(), action);
    }

    public static UnregisterAction forWeapon(EntityId weaponId, UndoObjectAction<Weapon> action) {
        return (game) -> {
            Weapon weapon = game.getWeapon(weaponId);
            if (weapon != null)
                action.undo(weapon);
        };
    }

    public static UnregisterAction forCard(Card card, UndoObjectAction<Card> action) {
        return forCard(card.getEntityId(), action);
    }

    public static UnregisterAction forCard(EntityId cardId, UndoObjectAction<Card> action) {
        return (game) -> {
            Card card = game.getCard(cardId);
            if (card != null)
                action.undo(card);
        };
    }

    public static UnregisterAction forMinion(Minion minion, UndoObjectAction<Minion> action) {
        return forMinion(minion.getEntityId(), action);
    }

    public static UnregisterAction forMinion(EntityId minionId, UndoObjectAction<Minion> action) {
        return (game) -> {
            Minion minion = game.getMinion(minionId);
            if (minion != null)
                action.undo(minion);
        };
    }

    public final class Builder implements UnregisterAction {
        private final List<UnregisterAction> wrapped;

        public Builder() {
            this(5);
        }

        public Builder(int expectSize) {
            wrapped = new ArrayList<>(expectSize);
        }

        public void add(UnregisterAction action) {
            wrapped.add(action);
        }

        public void unregister(Game game) {
            for (UnregisterAction action : wrapped)
                action.unregister(game);
        }
    }
}
