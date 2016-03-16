package info.hearthsim.brazier.events;

import info.hearthsim.brazier.Secret;
import info.hearthsim.brazier.actions.AttackRequest;
import info.hearthsim.brazier.cards.Card;
import info.hearthsim.brazier.minions.Minion;
import info.hearthsim.brazier.Player;
import info.hearthsim.brazier.weapons.Weapon;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jtrim.utils.ExceptionHelper;

/**
 * Type of event. An instance of {@code SimpleEventType} contains four fields: {@code greedyEvent}, {@code eventName},
 * {@code argType} and {@code globalFilter}.
 *
 * <p>The field {@code eventName} and {@code argType} will be given when constructing every instance.
 * {@code globalFilter} is the {@link GameEventFilter} that should be applied to every event of this type, which
 * is possible to be {@code null} if no such filter is needed.
 */
// TODO check what greedyEvent means.
public enum SimpleEventType {
    DRAW_CARD("draw-card", Card.class),
    START_PLAY_CARD("start-play-card", CardPlayEvent.class, (game, self, arg) -> {
        return self != ((CardPlayEvent) arg).getCard().getMinion();
    }),
    DONE_PLAY_CARD("done-play-card", CardPlayedEvent.class),
    PREPARE_DAMAGE("prepare-damage", DamageRequest.class),
    HERO_DAMAGED("hero-damaged", DamageEvent.class),
    MINION_DAMAGED("minion-damaged", DamageEvent.class),
    MINION_KILLED("minion-killed", Minion.class),
    WEAPON_DESTROYED("weapon-destroyed", Weapon.class),
    ARMOR_GAINED("armor-gained", ArmorGainedEvent.class),
    HERO_HEALED("hero-healed", DamageEvent.class),
    MINION_HEALED("minion-healed", DamageEvent.class),
    TURN_STARTS("turn-starts", Player.class),
    TURN_ENDS("turn-ends", Player.class),
    ATTACK_INITIATED(true, "attack-initiated", AttackRequest.class),
    SECRET_REVEALED("secret-revealed", Secret.class);

    private static final Map<String, SimpleEventType> NAME_MAP;

    static {
        Map<String, SimpleEventType> nameMap = new HashMap<>();
        for (SimpleEventType eventType: values()) {
            nameMap.put(eventType.eventName, eventType);
        }
        NAME_MAP = Collections.unmodifiableMap(nameMap);
    }

    private final boolean greedyEvent;
    private final String eventName;
    private final Class<?> argType;
    private final GameEventFilter<Object, Object> globalFilter;

    private SimpleEventType(String eventName, Class<?> argType) {
        this(false, eventName, argType);
    }

    private SimpleEventType(String eventName, Class<?> argType, GameEventFilter<Object, Object> globalFilter) {
        this(false, eventName, argType, globalFilter);
    }

    private SimpleEventType(boolean greedyEvent, String eventName, Class<?> argType) {
        this(greedyEvent, eventName, argType, null);
    }

    private SimpleEventType(
            boolean greedyEvent,
            String eventName,
            Class<?> argType,
            GameEventFilter<Object, Object> globalFilter) {
        this.greedyEvent = greedyEvent;
        this.eventName = eventName;
        this.argType = argType;
        this.globalFilter = globalFilter;
    }

    public GameEventFilter<Object, Object> getGlobalFilter() {
        return globalFilter;
    }

    public <Self, Arg> GameEventFilter<Self, Arg> addGlobalFilter(
            GameEventFilter<Self, Arg> localFilter) {
        ExceptionHelper.checkNotNullArgument(localFilter, "localFilter");
        if (globalFilter == null) {
            return localFilter;
        }

        return (game, self, arg) -> {
            return localFilter.applies(game, self, arg) && globalFilter.applies(game, arg, eventName);
        };
    }

    public static SimpleEventType tryParse(String str) {
        return NAME_MAP.get(str.toLowerCase(Locale.ROOT));
    }

    public boolean isGreedyEvent() {
        return greedyEvent;
    }

    public String getEventName() {
        return eventName;
    }

    public Class<?> getArgumentType() {
        return argType;
    }
}
