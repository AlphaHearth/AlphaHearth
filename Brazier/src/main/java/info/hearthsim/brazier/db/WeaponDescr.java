package info.hearthsim.brazier.db;

import info.hearthsim.brazier.abilities.Ability;
import info.hearthsim.brazier.abilities.LivingEntitiesAbilities;
import info.hearthsim.brazier.events.EventAction;
import info.hearthsim.brazier.events.TriggeringAbility;
import info.hearthsim.brazier.game.Keyword;
import info.hearthsim.brazier.game.weapons.Weapon;
import info.hearthsim.brazier.game.weapons.WeaponName;
import org.jtrim.utils.ExceptionHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class WeaponDescr implements HearthStoneEntity {
    private final WeaponName id;
    private final int attack;
    private final int durability;

    private final int maxAttackCount;
    private final boolean canRetaliateWith;
    private final boolean canTargetRetaliate;

    private final Set<Keyword> keywords;

    private final LivingEntitiesAbilities<Weapon> abilities;

    private WeaponDescr(Builder builder) {
        this.id = builder.id;
        this.attack = builder.attack;
        this.durability = builder.durability;
        this.maxAttackCount = builder.maxAttackCount;
        this.canRetaliateWith = builder.canRetaliateWith;
        this.canTargetRetaliate = builder.canTargetRetaliate;
        this.keywords = readOnlyCopySet(builder.keywords);
        this.abilities = builder.abilities;
    }

    private <T> Set<T> readOnlyCopySet(Collection<? extends T> src) {
        if (src.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(src));
    }

    public String getDisplayName() {
        // TODO: Allow customizing the display name.
        return id.getName();
    }

    @Override
    public WeaponName getId() {
        return id;
    }

    @Override
    public Set<Keyword> getKeywords() {
        return keywords;
    }

    public int getAttack() {
        return attack;
    }

    public int getMaxAttackCount() {
        return maxAttackCount;
    }

    public int getDurability() {
        return durability;
    }

    public boolean canRetaliateWith() {
        return canRetaliateWith;
    }

    public boolean canTargetRetaliate() {
        return canTargetRetaliate;
    }

    public TriggeringAbility<Weapon> getEventActionDefs() {
        return abilities.getTriggers();
    }

    public EventAction<? super Weapon, ? super Weapon> tryGetDeathRattle() {
        return abilities.tryGetDeathRattle();
    }

    public Ability<? super Weapon> tryGetAbility() {
        return abilities.tryGetAbility();
    }

    public static final class Builder {
        private final WeaponName id;
        private final int attack;
        private final int durability;

        private int maxAttackCount;
        private boolean canRetaliateWith;
        private boolean canTargetRetaliate;

        private final Set<Keyword> keywords;

        private LivingEntitiesAbilities<Weapon> abilities;

        public Builder(WeaponName id, int attack, int durability) {
            ExceptionHelper.checkNotNullArgument(id, "id");

            this.id = id;
            this.attack = attack;
            this.durability = durability;
            this.maxAttackCount = 1;
            this.canRetaliateWith = false;
            this.canTargetRetaliate = true;
            this.keywords = new HashSet<>();
            this.abilities = LivingEntitiesAbilities.noAbilities();
        }

        public void setAbilities(LivingEntitiesAbilities<Weapon> abilities) {
            ExceptionHelper.checkNotNullArgument(abilities, "abilities");
            this.abilities = abilities;
        }

        public void setMaxAttackCount(int maxAttackCount) {
            this.maxAttackCount = maxAttackCount;
        }

        public void addKeyword(Keyword keyword) {
            ExceptionHelper.checkNotNullArgument(keyword, "keyword");
            keywords.add(keyword);
        }

        public void setCanRetaliateWith(boolean canRetaliateWith) {
            this.canRetaliateWith = canRetaliateWith;
        }

        public void setCanTargetRetaliate(boolean canTargetRetaliate) {
            this.canTargetRetaliate = canTargetRetaliate;
        }

        public WeaponDescr create() {
            return new WeaponDescr(this);
        }
    }
}
