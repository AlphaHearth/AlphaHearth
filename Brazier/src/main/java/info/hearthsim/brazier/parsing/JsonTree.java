package info.hearthsim.brazier.parsing;

import com.google.gson.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@code JsonTree} is essentially an encapsulation of Google Gson's {@link JsonElement}, which effectively
 * shields the programmers from the difference between {@link JsonObject} and {@link JsonArray}.
 * <p>
 * An {@code JsonTree} can be used to fetch children from an Json document, methods include {@link #getChild(int)}
 * and {@link #getChild(String)}.
 */
public interface JsonTree {

    /**
     * Returns the encapsulating {@link JsonElement}.
     */
    public JsonElement getElement();

    /**
     * Returns the number of children in this {@code JsonTree}.
     */
    public int getChildCount();

    /**
     * Returns the child of this {@code JsonTree} with the given {@code index}. An integer {@code index}
     * will be useful if the underlying {@link JsonElement} is a {@link JsonArray}. If it's a {@link JsonObject},
     * a deterministic result should be expected from this method, even though the result may have no meaningful
     * relation with the given {@code index}.
     *
     * @param index the given {@code index}.
     * @return the child of this {@code JsonTree} with the given {@code index}.
     * @throws ArrayIndexOutOfBoundsException if the given {@code index} exceeds the number of children in this
     *                                        {@code JsonTree}.
     */
    public JsonTree getChild(int index);

    /**
     * Returns the child of this {@code JsonTree} with the given field name. Returns {@code null}
     * if such child cannot be found or the underlying {@link JsonElement} is not a {@link JsonObject}.
     *
     * @param name the given field name.
     * @return the child of this {@code JsonTree} with the given field name; {@code null} if such child
     *         cannot be found.
     */
    public JsonTree getChild(String name);

    /**
     * Returns an {@link Iterable} of {@link JsonTree}s, which can be used to iterate all children
     * of this {@code JsonTree}.
     */
    public default Iterable<JsonTree> getChildren() {
        return () -> {
            return new Iterator<JsonTree>() {
                private int nextIndex = 0;

                @Override
                public boolean hasNext() {
                    return nextIndex < getChildCount();
                }

                @Override
                public JsonTree next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException("End of Children");
                    }
                    int currentIndex = nextIndex;
                    nextIndex++;
                    return getChild(currentIndex);
                }
            };
        };
    }

    /**
     * Returns if the underlying {@link JsonElement} is a {@link JsonArray}.
     *
     * @see JsonElement#isJsonArray()
     */
    public default boolean isJsonArray() {
        return getElement().isJsonArray();
    }

    /**
     * Returns if the underlying {@link JsonElement} is a {@link JsonObject}.
     *
     * @see JsonElement#isJsonObject()
     */
    public default boolean isJsonObject() {
        return getElement().isJsonObject();
    }

    /**
     * Returns if the underlying {@link JsonElement} is a {@link JsonPrimitive}.
     *
     * @see JsonElement#isJsonPrimitive()
     */
    public default boolean isJsonPrimitive() {
        return getElement().isJsonPrimitive();
    }

    /**
     * Returns if the underlying {@link JsonElement} is a {@link JsonNull}.
     *
     * @see JsonElement#isJsonNull()
     */
    public default boolean isJsonNull() {
        return getElement().isJsonNull();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@link JsonPrimitive}.
     *
     * @see JsonElement#getAsJsonPrimitive()
     */
    public default JsonPrimitive getAsJsonPrimitive() {
        return getElement().getAsJsonPrimitive();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@link JsonNull}.
     *
     * @see JsonElement#getAsJsonNull()
     */
    public default JsonNull getAsJsonNull() {
        return getElement().getAsJsonNull();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code boolean}.
     *
     * @see JsonElement#getAsBoolean()
     */
    public default boolean getAsBoolean() {
        return getElement().getAsBoolean();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code Number}.
     *
     * @see JsonElement#getAsNumber()
     */
    public default Number getAsNumber() {
        return getElement().getAsNumber();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code String}.
     *
     * @see JsonElement#getAsString()
     */
    public default String getAsString() {
        return getElement().getAsString();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code Double}.
     *
     * @see JsonElement#getAsDouble()
     */
    public default double getAsDouble() {
        return getElement().getAsDouble();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code Float}.
     *
     * @see JsonElement#getAsFloat()
     */
    public default float getAsFloat() {
        return getElement().getAsFloat();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code Long}.
     *
     * @see JsonElement#getAsLong()
     */
    public default long getAsLong() {
        return getElement().getAsLong();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code int}.
     *
     * @see JsonElement#getAsInt()
     */
    public default int getAsInt() {
        return getElement().getAsInt();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code byte}.
     *
     * @see JsonElement#getAsByte()
     */
    public default byte getAsByte() {
        return getElement().getAsByte();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code char}.
     *
     * @see JsonElement#getAsCharacter()
     */
    public default char getAsCharacter() {
        return getElement().getAsCharacter();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code BigDecimal}.
     *
     * @see JsonElement#getAsBigDecimal()
     */
    public default BigDecimal getAsBigDecimal() {
        return getElement().getAsBigDecimal();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code BigInteger}.
     *
     * @see JsonElement#getAsBigInteger()
     */
    public default BigInteger getAsBigInteger() {
        return getElement().getAsBigInteger();
    }

    /**
     * Returns the underlying {@link JsonElement} as a {@code short}.
     *
     * @see JsonElement#getAsShort()
     */
    public default short getAsShort() {
        return getElement().getAsShort();
    }
}
