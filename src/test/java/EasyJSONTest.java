import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;
import xyz.victorolaitan.easyjson.JSONElement;

public class EasyJSONTest {

    @Test
    @DisplayName("create empty JSON")
    public void createEmptyJSON() {
        EasyJSON json = EasyJSON.create();
        Assert.assertEquals("{}", json.toString());
    }

    @Test
    @DisplayName("add string")
    public void addString() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("my", "name");
        Assert.assertEquals("{\"my\":\"name\"}", json.toString());
    }

    @Test
    @DisplayName("add number")
    public void addNumber() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("number", 123.456f);
        Assert.assertEquals("{\"number\":123.456}", json.toString());
    }

    @Test
    @DisplayName("add boolean")
    public void addBoolean() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("bool", true);
        Assert.assertEquals("{\"bool\":true}", json.toString());
    }

    @Test
    @DisplayName("add element (infer key)")
    public void addElementInferKey() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("bool", true);
        json.putElement(EasyJSON.create().putPrimitive("key", "value"));
        Assert.assertEquals("{\"bool\":true,\"key\":\"value\"}", json.toString());
    }

    @Test
    @DisplayName("add element (explicit key)")
    public void addElementExplicitKey() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("bool", true);
        json.putElement("name", EasyJSON.create().putPrimitive("key", "value"));
        Assert.assertEquals("{\"bool\":true,\"name\":\"value\"}", json.toString());
    }

    @Test
    @DisplayName("add primitive (null key)")
    public void addPrimitiveNullKey() {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("__something");
        Assert.assertEquals("{\"null\":\"__something\"}", json.toString());
    }

    @Test
    @DisplayName("add structure")
    public void addStructure() {
        EasyJSON json = EasyJSON.create();
        json.putStructure("struct").putPrimitive("ping", "pong");
        Assert.assertEquals("{\"struct\":{\"ping\":\"pong\"}}", json.toString());
    }

    @Test
    @DisplayName("add structure (from root)")
    public void addStructureFromRoot() {
        EasyJSON json = EasyJSON.create();
        EasyJSON struct = EasyJSON.create();
        struct.putPrimitive("ping", "pong");
        json.putStructure("struct", struct);
        Assert.assertEquals("{\"struct\":{\"ping\":\"pong\"}}", json.toString());
    }

    @Test
    @DisplayName("add structure (from element)")
    public void addStructureFromElement() {
        EasyJSON json = EasyJSON.create();
        EasyJSON struct = EasyJSON.create();
        struct.putPrimitive("ping", "pong");
        json.putStructure("struct", struct.getRootNode());
        Assert.assertEquals("{\"struct\":{\"ping\":\"pong\"}}", json.toString());
    }

    @Test
    @DisplayName("add array")
    public void addArray() {
        EasyJSON json = EasyJSON.create();
        JSONElement arr = json.putArray("arr", 1, 2, 3);
        arr.putPrimitive("ping", "pong");
        Assert.assertEquals("{\"arr\":[1,2,3,\"pong\"]}", json.toString());
    }

    @Test
    @DisplayName("add array (from element)")
    public void addArrayFromElement() {
        EasyJSON json = EasyJSON.create();
        JSONElement arr = EasyJSON.create().putArray("arr", 1, 2, 3);
        arr.putPrimitive("ping", "pong");
        json.putElement(arr);
        Assert.assertEquals("{\"arr\":[1,2,3,\"pong\"]}", json.toString());
    }

    @Test
    @DisplayName("remove element")
    public void removeElement() {
        EasyJSON json = EasyJSON.create();
        json.putStructure("ping").putPrimitive("pong", "ball");
        json.removeElement("ping", "pong");
        Assert.assertEquals("{\"ping\":{}}", json.toString());
    }

    @Test
    @DisplayName("element exists")
    public void elementExists() {
        EasyJSON json = EasyJSON.create();
        json.putArray("ping").putStructure("").putPrimitive("pong", "ball");
        Assert.assertTrue(json.elementExists("ping", "0", "pong"));
    }

    @Test
    @DisplayName("search")
    public void search() {
        EasyJSON json = EasyJSON.create();
        JSONElement struct = json.putArray("ping").putStructure("");
        JSONElement e = struct.putPrimitive("pong", "ball");
        Assert.assertEquals(e, json.search("ping", "0", "pong"));
    }

    @Test
    @DisplayName("valueOf")
    public void valueOf() {
        EasyJSON json = EasyJSON.create();
        json.putArray("ping").putStructure("").putPrimitive("pong", "ball");
        Assert.assertEquals("ball", json.valueOf("ping", "0", "pong"));
    }

    @Test
    @DisplayName("putAll (root)")
    public void putAllRoot() {
        EasyJSON json1 = EasyJSON.create();
        json1.putArray("ping").putPrimitive("hi");
        json1.putStructure("pong").putPrimitive("ball", ":)");
        EasyJSON json2 = EasyJSON.create();
        json2.putAll(json1);
        Assert.assertEquals("{\"ping\":[\"hi\"],\"pong\":{\"ball\":\":)\"}}", json2.toString());
    }

    @Test
    @DisplayName("putAll (element)")
    public void putAllElement() {
        EasyJSON json1 = EasyJSON.create();
        json1.putArray("ping").putPrimitive("hi");
        json1.putStructure("pong").putPrimitive("ball", ":)");
        EasyJSON json2 = EasyJSON.create();
        json2.putAll(json1.getRootNode());
        Assert.assertEquals("{\"ping\":[\"hi\"],\"pong\":{\"ball\":\":)\"}}", json2.toString());
    }

    @Test
    @DisplayName("export")
    public void export() throws EasyJSONException {
        EasyJSON json = EasyJSON.create();
        json.putPrimitive("ping", "pong");
        Assert.assertEquals("pong", json.exportToJSONObject().get("ping"));
    }
}
