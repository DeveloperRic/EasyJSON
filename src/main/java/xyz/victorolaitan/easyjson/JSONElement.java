package xyz.victorolaitan.easyjson;

import java.util.Iterator;
import java.util.List;

/**
 * JSONElement is a representation of a node in the JSON structure<br/>
 * It can have multiple children of different types, but it only has one parent.
 */
public interface JSONElement extends Iterable<JSONElement> {
    EasyJSON getEasyJSONStructure();

    JSONElement getParent();

    /**
     * Sets `easyJSONStructure` & `parent` fields to the values provided.
     * BE VERY CAREFUL USING THIS METHOD
     */
    void mutateAncestry(EasyJSON easyJSONStructure, JSONElement parent);

    JSONElementType getType();

    void setType(SafeJSONElementType type);

    List<JSONElement> getChildren();

    String getKey();

    void setKey(String key);

    Object getValue();

    void setValue(Object value);

    /**
     * Adds pre-existing nodes to this node
     *
     * @param elements nodes to add
     */
    void putElement(JSONElement... elements);

    /**
     * Add pre-existing nodes to this node
     *
     * @param key         string key to use if the node to be added supports it
     * @param jsonElement node to be added
     * @return the new node added (or null if the node has a bad structure)
     */
    JSONElement putElement(String key, JSONElement jsonElement);

    /**
     * Adds an element with a primitive value to this node
     *
     * @param value object to add
     * @return new element added
     */
    JSONElement putPrimitive(Object value);

    /**
     * Adds an element with a string key and primitive value
     *
     * @param key   string identifier
     * @param value object to add
     * @return new element added
     */
    JSONElement putPrimitive(String key, Object value);

    /**
     * Adds a structure element
     *
     * @param key string identifier
     * @return new structure added
     */
    JSONElement putStructure(String key);

    /**
     * Adds an EasyJSON root structure to this node
     *
     * @param key      string identifier
     * @param easyJSON structure to add
     * @return new structure added
     */
    JSONElement putStructure(String key, EasyJSON easyJSON);

    /**
     * Adds an existing structure node to this node
     *
     * @param key       string identifier
     * @param structure structure to add
     * @return new structure added
     */
    JSONElement putStructure(String key, JSONElement structure);

    /**
     * Adds an array node with preset children to this node
     *
     * @param key   string identifier
     * @param items objects to convert to elements and add to the new node
     * @return new array node added
     */
    JSONElement putArray(String key, Object... items);

    /**
     * Claims all elements in the EasyJSON structure<br/>
     * Note: the supplied structure is unchanged
     *
     * @param easyJSONStructure structure to add from
     */
    void putAll(EasyJSON easyJSONStructure);

    /**
     * Claims all child elements of the specified element<br/>
     * Note: the supplied element is unchanged
     *
     * @param jsonElement element to add from
     */
    void putAll(JSONElement jsonElement);

    /**
     * may cause duplicate keys, use carefully!
     *
     * @param jsonElement node to claim
     */
    void claimElement(JSONElement jsonElement);

    JSONElement merge(JSONElement newElement);

    /**
     * Removes an element from a location relative to this node
     *
     * @param location search path for the node to remove
     * @return true if the node was removed
     */
    boolean removeElement(String... location);

    /**
     * @param location search path for a node
     * @return true if a node exists in the specified location
     */
    boolean elementExists(String... location);

    /**
     * Finds a node in a specified location
     *
     * @param location search path for the node
     * @return node found or null
     */
    JSONElement search(String... location);

    JSONElement deepSearch(JSONElement element, String[] location, int locPosition);

    /**
     * Gets and returns the value of a node in a specified location
     *
     * @param location search path for the node
     * @see #search(String...)
     */
    Object valueOf(String... location);

    @Override
    Iterator<JSONElement> iterator();

    @Override
    String toString();
}
