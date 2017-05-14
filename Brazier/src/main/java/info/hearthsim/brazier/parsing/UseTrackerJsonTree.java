package info.hearthsim.brazier.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jtrim.utils.ExceptionHelper;

import java.util.*;

/**
 * {@link JsonTree} that keeps track to its usage. Every time its method is called, a flag called {@code used}
 * will be set and recognized as "requested". Customized methods include {@link #isRequestedAllElements()} and
 * {@link #checkRequestedAllElements()} can be used to test the {@code UseTrackerJsonTree}'s state of "requested".
 */
public final class UseTrackerJsonTree implements JsonTree {
    private static final UseTrackerJsonTree[] EMPTY_TREE_ARRAY = new UseTrackerJsonTree[0];

    private final JsonElement element;
    private final Map<String, UseTrackerJsonTree> children;
    private final UseTrackerJsonTree[] orderedChildren;
    private boolean used;

    public UseTrackerJsonTree(JsonElement element) {
        ExceptionHelper.checkNotNullArgument(element, "element");

        this.element = element;
        this.children = getChildren(element);
        this.orderedChildren = getIndexChildren(element, children);
        this.used = false;
    }

    /**
     * If the given {@link JsonElement} is a {@link JsonObject}, converts its children to equivalent
     * {@code UseTrackerJsonTree}s and returns.
     *
     * @param element the given {@link JsonElement}
     * @return the mapping from field names to the equivalent children {@code UseTrackerJsonTree}s.
     */
    private static Map<String, UseTrackerJsonTree> getChildren(JsonElement element) {
        if (!element.isJsonObject()) {
            return Collections.emptyMap();
        }

        JsonObject root = element.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> children = root.entrySet();
        Map<String, UseTrackerJsonTree> result = new LinkedHashMap<>(children.size() * 4 / 3 + 1);
        for (Map.Entry<String, JsonElement> entry : children) {
            result.put(entry.getKey(), new UseTrackerJsonTree(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * If the given {@link JsonElement} is a {@link JsonArray}, returns its content as an array of
     * {@code UseTrackerJsonTree}s; otherwise, returns the values in the given mapping {@code children}
     * as an array.
     *
     * @param element  the given {@link JsonElement}.
     * @param children the given mapping.
     * @return an array of {@code UseTrackerJsonTree}.
     */
    private static UseTrackerJsonTree[] getIndexChildren(
        JsonElement element,
        Map<String, UseTrackerJsonTree> children) {

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            int childCount = array.size();
            List<UseTrackerJsonTree> result = new ArrayList<>(childCount);
            for (int i = 0; i < childCount; i++) {
                result.add(new UseTrackerJsonTree(array.get(i)));
            }
            return result.toArray(new UseTrackerJsonTree[result.size()]);
        }

        if (children.isEmpty()) {
            return EMPTY_TREE_ARRAY;
        }

        List<UseTrackerJsonTree> result = new ArrayList<>(children.size());
        for (UseTrackerJsonTree child : children.values()) {
            result.add(child);
        }
        return result.toArray(new UseTrackerJsonTree[result.size()]);
    }

    @Override
    public JsonElement getElement() {
        used = true;
        return element;
    }

    @Override
    public JsonTree getChild(String childName) {
        used = true;
        return children.get(childName);
    }

    @Override
    public int getChildCount() {
        used = true;
        return orderedChildren.length;
    }

    @Override
    public JsonTree getChild(int index) {
        used = true;
        if (index > orderedChildren.length)
            throw new ArrayIndexOutOfBoundsException(String.format(
                "The given index `%d` exceeds the JsonTree's children count `%d`",
                index, orderedChildren.length
            ));
        return orderedChildren[index];
    }

    /**
     * Returns if every children of this {@code UseTrackerJsonTree} has been requested.
     */
    public boolean isRequestedAllElements() {
        if (!used) {
            return false;
        }

        for (UseTrackerJsonTree child : orderedChildren) {
            if (!child.isRequestedAllElements()) {
                return false;
            }
        }
        return true;
    }

    private void checkRequestedAllElements(List<String> parents) throws ObjectParsingException {
        if (!used) {
            throw new ObjectParsingException("The entry is unused at " + concat(parents, "/"));
        }

        if (children.isEmpty()) {
            int index = 0;
            for (UseTrackerJsonTree child : orderedChildren) {
                parents.add(Integer.toString(index));
                child.checkRequestedAllElements(parents);
                parents.remove(parents.size() - 1);
                index++;
            }
        } else {
            for (Map.Entry<String, UseTrackerJsonTree> entry : children.entrySet()) {
                parents.add(entry.getKey());
                entry.getValue().checkRequestedAllElements(parents);
                parents.remove(parents.size() - 1);
            }
        }
    }

    /**
     * Checks the "requested" state of every children of the {@code UseTrackerJsonTree} and
     * throws an {@link ObjectParsingException} if any of the children is not requested before.
     *
     * @throws ObjectParsingException if any children of the {@code UseTrackerJsonTree} is not
     *                                requested before.
     */
    public void checkRequestedAllElements() throws ObjectParsingException {
        checkRequestedAllElements(new ArrayList<>());
    }

    @Override
    public String toString() {
        return getElement().toString();
    }

    private static String concat(List<String> values, String separator) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(value);
        }
        return result.toString();
    }
}
