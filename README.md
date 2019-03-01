# EasyJSON

EasyJSON is a class created to help simplify the JSON process.
No need to create new Objects for each item you want to add.
Simply use: *foo.putPrimitive(value)* or *foo.putPrimitive(key, value)* or *foo.putArray(key, values...)* or *foo.putStructure("key")*.
You can also add items inline.

**Example**
```
EasyJSON json = EasyJSON.create("/easyJSON_example.json");
json.putStructure("pets").putArray("dogs").putPrimitive("pug");
json.search("pets", "dogs").putPrimitive("rottweiler");
json.search("pets").putArray("cats", "i'm not a cat guy");
```
will result in a structure like this:

**easyJSON_example.json**
```
{
 "pets":{
   "cats":["i'm not a cat guy"],
   "dogs":["pug", "rottweiler"]
 }
}
```
