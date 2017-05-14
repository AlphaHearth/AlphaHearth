package info.hearthsim.brazier.events;

import info.hearthsim.brazier.PlayerProperty;
import info.hearthsim.brazier.game.Hero;
import info.hearthsim.brazier.game.Player;
import org.jtrim.utils.ExceptionHelper;

/**
 * The event of a certain hero gaining number(s) of armor.
 */
public final class ArmorGainedEvent implements PlayerProperty {
    private final Hero hero;
    private final int armorGained;

    /**
     * Creates an instance of {@code ArmorGainedEvent}, designating which hero gaining
     * how many armors.
     *
     * @param hero the hero who gains armors
     * @param armorGained the number of armors gained
     */
    public ArmorGainedEvent(Hero hero, int armorGained) {
        ExceptionHelper.checkNotNullArgument(hero, "hero");
        this.hero = hero;
        this.armorGained = armorGained;
    }

    /**
     * Returns the hero who gains the armor.
     */
    public Hero getHero() {
        return hero;
    }

    /**
     * Returns the number of armors gained in this event.
     */
    public int getArmorGained() {
        return armorGained;
    }

    @Override
    public Player getOwner() {
        return hero.getOwner();
    }
}
