package info.hearthsim.brazier;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public interface BornEntity {
    /**
     * The default {@code Comparator} for {@link BornEntity}, whose {@link Comparator#compare(Object, Object) compare}
     * method returns value less than {@code 0} if {@code entity1} is born earlier than {@code entity2}.
     */
    public static final Comparator<BornEntity> CMP = (entity1, entity2) -> {
        return Long.compare(entity1.getBirthDate(), entity2.getBirthDate());
    };

    public long getBirthDate();

    /**
     * Sorts the given {@link List} of {@link BornEntity} in the ascending order of their birth date.
     */
    public static void sortEntities(List<? extends BornEntity> entities) {
        entities.sort(CMP);
    }

    public static void sortEntities(BornEntity[] entities) {
        Arrays.sort(entities, CMP);
    }
}
