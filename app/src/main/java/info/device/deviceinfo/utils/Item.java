package info.device.deviceinfo.utils;

/**
 * Created by namphan on 11/16/15.
 */
public class Item {
    private String name;
    private String value;

    public Item() {}
    public Item(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
