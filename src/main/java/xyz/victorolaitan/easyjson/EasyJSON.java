package xyz.victorolaitan.easyjson;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * EasyJSON is a class created to help simplify the JSON process.
 * No need to create new Objects for each item you want to add.
 * Simply use: <i>foo.putPrimitive(value)</i> or <i>foo.putPrimitive(key, value)</i> or <i>foo.putArray(key, values...)</i> or <i>foo.putStructure("key")</i>.
 * You can also add items inline.
 * <p>
 * <b>Example</b>
 * <p>
 * {@code EasyJSON json = EasyJSON.create("/EasyJSON_Example.json");}
 * <br>
 * {@code json.putStructure("pets").putArray("dogs").putPrimitive("pug");}
 * <br>
 * {@code json.search("pets", "dogs").putPrimitive("rottweiler");}
 * <br>
 * {@code json.search("pets").putArray("cats", "i'm not a cat guy");}
 * <p>
 * will result in a structure like this:
 * <p>
 * <b>EasyJSON_Example.json</b>
 * <p>
 * {
 * <p>
 * "pets":{
 * <p>
 * "cats":["i'm not a cat guy"],
 * <p>
 * "dogs":["pug", "rottweiler"]
 * <p>
 * }
 * <p>
 * }
 * <p>
 * Initial commit by Victor Olaitan on 09/03/2017.
 */
@SuppressWarnings("UnusedReturnValue")
public class EasyJSON {
    /**
     * @return an empty EasyJSON structure
     */
    public static EasyJSON create() {
        return new EasyJSON();
    }

    /**
     * Creates an empty EasyJSON structure and assigns a save file.
     * @param saveFile the file to save changes to
     * @return an empty EasyJSON structure
     */
    public static EasyJSON create(File saveFile) {
        EasyJSON easyJSON = new EasyJSON();
        easyJSON.setSaveLocation(saveFile);
        return easyJSON;
    }

    /**
     * Creates an empty EasyJSON structure and assigns a save path.
     * @param absoluteFilePath the filepath to save changes to
     * @return an empty EasyJSON structure
     */
    public static EasyJSON create(String absoluteFilePath) {
        EasyJSON easyJSON = new EasyJSON();
        easyJSON.setSaveLocation(absoluteFilePath);
        return easyJSON;
    }

    /**
     * Reads the specified file and attempts to parse it into an EasyJSON structure.
     * <p><i>Note: the file does not need to have a .json extension</i></p>
     *
     * @param file the file containing the json data
     * @return The parsed EasyJSON structure
     * @throws EasyJSONException if the file's JSON structure is incompatible with EasyJSON.
     */
    public static EasyJSON open(File file) throws EasyJSONException {
        return new EasyJSON(file.getAbsolutePath());
    }

    /**
     * Reads the specified file and attempts to parse it into an EasyJSON structure.
     *
     * @param filePath The path of the file relative to the Java instance (or full path ie. c: .... )
     * @return The parsed EasyJSON structure
     * @throws EasyJSONException if the file's JSON structure is incompatible with EasyJSON.
     */
    public static EasyJSON open(String filePath) throws EasyJSONException {
        return new EasyJSON(filePath);
    }

    private final JSONElement rootNode;
    private String filePath;

    /**
     * @return the JSONElement holding the entire JSON structure
     */
    public JSONElement getRootNode() {
        return rootNode;
    }

    public void setSaveLocation(File saveFile) {
        this.filePath = saveFile.getAbsolutePath();
    }

    public void setSaveLocation(String absoluteFilePath) {
        this.filePath = absoluteFilePath;
    }

    private EasyJSON() {
        rootNode = new JSONElementImpl(this, null, JSONElementType.ROOT, null, null);
    }

    private EasyJSON(String filePath) throws EasyJSONException {
        rootNode = new JSONElementImpl(this, null, JSONElementType.ROOT, null, null);
        try {
            if (!filePath.equals("")) {
                JSONObject obj;
                obj = (JSONObject) (new JSONParser()).parse(new FileReader(filePath));
                for (Object key : obj.keySet()) {
                    if (!(key instanceof String)) {
                        throw new UnexpectedTokenException("EasyJSON can't handle non-string keys.");
                    }
                    Object value = obj.get(key);
                    JSONElementType type = JSONElementType.PRIMITIVE;
                    if (value instanceof JSONArray) {
                        type = JSONElementType.ARRAY;
                    } else if (value instanceof JSONObject) {
                        type = JSONElementType.STRUCTURE;
                    }
                    JSONElement parentElement = new JSONElementImpl(this, null, type, key.toString(), value);
                    iterateElement(parentElement);
                    rootNode.getChildren().add(parentElement);
                }
                this.filePath = filePath;
            } else {
                throw new UnexpectedTokenException("The file path specified is invalid.");
            }
        } catch (org.json.simple.parser.ParseException e) {
            throw new EasyJSONException(EasyJSONException.FILE_NOT_JSON, e);
        } catch (IOException e) {
            throw new EasyJSONException(EasyJSONException.LOAD_ERROR, e);
        }
    }

    private void iterateElement(JSONElement targetItem) throws UnexpectedTokenException {
        if (targetItem.getType() == JSONElementType.ARRAY) {
            JSONArray array = (JSONArray) targetItem.getValue();
            for (Object arrayItem : array) {
                JSONElementType type = JSONElementType.PRIMITIVE;
                if (arrayItem instanceof JSONArray) {
                    type = JSONElementType.ARRAY;
                } else if (arrayItem instanceof JSONObject) {
                    type = JSONElementType.STRUCTURE;
                }
                JSONElement newItem = new JSONElementImpl(this, targetItem, type, "", arrayItem);
                iterateElement(newItem);
                targetItem.getChildren().add(newItem);
            }
        } else if (targetItem.getType() == JSONElementType.STRUCTURE) {
            JSONObject structure = (JSONObject) targetItem.getValue();
            for (Object key : structure.keySet()) {
                if (!(key instanceof String)) {
                    throw new UnexpectedTokenException("EasyJSON can't handle non-string keys.");
                }
                Object value = structure.get(key);
                JSONElementType type = JSONElementType.PRIMITIVE;
                if (value instanceof JSONArray) {
                    type = JSONElementType.ARRAY;
                } else if (value instanceof JSONObject) {
                    type = JSONElementType.STRUCTURE;
                }
                JSONElement newItem = new JSONElementImpl(this, targetItem, type, key.toString(), value);
                iterateElement(newItem);
                targetItem.getChildren().add(newItem);
            }
        }
    }

    /**
     * @see JSONElement#putElement(JSONElement...)
     */
    public void putElement(JSONElement... elements) {
        rootNode.putElement(elements);
    }

    /**
     * @see JSONElement#putElement(String, JSONElement)
     */
    public JSONElement putElement(String key, JSONElement jsonElement) {
        return rootNode.putElement(key, jsonElement);
    }

    /**
     * @see JSONElement#putPrimitive(Object)
     */
    public JSONElement putPrimitive(Object value) {
        return rootNode.putPrimitive(value);
    }

    /**
     * @see JSONElement#putPrimitive(String, Object)
     */
    public JSONElement putPrimitive(String key, Object value) {
        return rootNode.putPrimitive(key, value);
    }

    /**
     * @see JSONElement#putStructure(String)
     */
    public JSONElement putStructure(String key) {
        return rootNode.putStructure(key);
    }

    /**
     * @see JSONElement#putStructure(String, EasyJSON)
     */
    public JSONElement putStructure(String key, EasyJSON easyJSON) {
        return rootNode.putStructure(key, easyJSON);
    }

    /**
     * @see JSONElement#putStructure(String, JSONElement)
     */
    public JSONElement putStructure(String key, JSONElement structure) {
        return rootNode.putStructure(key, structure);
    }

    /**
     * @see JSONElement#putArray(String, Object...)
     */
    public JSONElement putArray(String key, Object... items) {
        return rootNode.putArray(key, items);
    }

    /**
     * @see JSONElement#removeElement(String...)
     */
    public boolean removeElement(String... location) {
        return rootNode.removeElement(location);
    }

    /**
     * @see JSONElement#elementExists(String...)
     */
    public boolean elementExists(String... location) {
        return rootNode.elementExists(location);
    }

    /**
     * @see JSONElement#search(String...)
     */
    public JSONElement search(String... location) {
        return rootNode.search(location);
    }

    /**
     * @see JSONElement#valueOf(String...)
     */
    public Object valueOf(String... location) {
        return rootNode.valueOf(location);
    }

    /**
     * @see JSONElement#putAll(EasyJSON)
     */
    public void putAll(EasyJSON easyJSONStructure) {
        rootNode.putAll(easyJSONStructure.getRootNode());
    }

    /**
     * @see JSONElement#putAll(JSONElement)
     */
    public void putAll(JSONElement jsonElement) {
        rootNode.putAll(jsonElement);
    }

    /**
     * Exports this EasyJSON structure to a SimpleJSON {@link JSONObject JSONObject}
     * @return SimpleJSON object
     * @throws EasyJSONException if there's an error during export
     */
    public JSONObject exportToJSONObject() throws EasyJSONException {
        return deepSave(new JSONObject(), rootNode);
    }

    private void checkExists(String path) throws EasyJSONException {
        File file = new File(path);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new EasyJSONException(EasyJSONException.SAVE_ERROR, "Couldn't create the save file!");
                }
            } catch (IOException e) {
                throw new EasyJSONException(EasyJSONException.SAVE_ERROR, e);
            }
        }
    }

    @Override
    public String toString() {
        return rootNode.toString();
    }

    /**
     * Saves this JSON structure to its pre-defined save location
     * @throws EasyJSONException if there's an error in saving
     */
    public void save() throws EasyJSONException {
        if (filePath == null || filePath.equals("")) {
            throw new EasyJSONException(EasyJSONException.SAVE_ERROR,
                    "Instance save path wasn't initialised!. " +
                            "Use the save(String absoluteSavePath) method to save to a compatible file.");
        }
        save(filePath);
    }

    /**
     * Saves this JSON structure to a file
     * @param saveFile file to save to
     * @throws EasyJSONException if there's an error in saving
     */
    public void save(File saveFile) throws EasyJSONException {
        save(saveFile.getAbsolutePath());
    }

    /**
     * Saves this JSON structure to a file at specified path
     * @param absoluteSavePath path to save to
     * @throws EasyJSONException is there's an error in saving
     */
    public void save(String absoluteSavePath) throws EasyJSONException {
        checkExists(absoluteSavePath);
        try (FileWriter file = new FileWriter(absoluteSavePath)) {
            file.write(this.toString());
            file.flush();
        } catch (IOException e) {
            throw new EasyJSONException(EasyJSONException.SAVE_ERROR, e);
        }
    }

    <T> T deepSave(T currentJSONRef, JSONElement currentElement) throws EasyJSONException {
        for (int i = 0; i < currentElement.getChildren().size(); i++) {
            JSONElement child = currentElement.getChildren().get(i);
            Object objectToAdd;
            switch (child.getType()) {
                case ARRAY:
                    objectToAdd = deepSave(new JSONArray(), child);
                    break;
                case STRUCTURE:
                case ROOT:
                    objectToAdd = deepSave(new JSONObject(), child);
                    break;
                default:
                    objectToAdd = child.getValue();
            }
            if (objectToAdd != null) {
                if (currentJSONRef instanceof JSONObject) {
                    JSONObject object = (JSONObject) currentJSONRef;
                    object.put(child.getKey(), objectToAdd);
                } else if (currentJSONRef instanceof JSONArray) {
                    JSONArray array = (JSONArray) currentJSONRef;
                    array.add(objectToAdd);
                } else {
                    throw new EasyJSONException(EasyJSONException.SAVE_ERROR, currentElement);
                }
            } else {
                throw new EasyJSONException(EasyJSONException.SAVE_ERROR, currentElement);
            }
        }
        return currentJSONRef;
    }
}