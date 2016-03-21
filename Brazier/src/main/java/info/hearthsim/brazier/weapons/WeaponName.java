package info.hearthsim.brazier.weapons;

import info.hearthsim.brazier.EntityName;
import java.util.Objects;

import org.jtrim.utils.ExceptionHelper;

public final class WeaponName implements EntityName {
    private final String name;

    public WeaponName(String name) {
        ExceptionHelper.checkNotNullArgument(name, "name");
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final WeaponName other = (WeaponName)obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
