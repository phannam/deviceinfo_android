package info.device.deviceinfo.views;

/**
 * Created by namphan on 5/12/15.
 */
public class SlidingTab {
    private int tab = 0;
    private int ic_drawable;
    private int ic_drawable_selected;
    private int ic_text_color;
    private int ic_text_color_selected;
    private String name = "";
    private boolean selected = false;

    public SlidingTab(int tab, int ic_drawable, String name, int ic_text_color) {
        this.tab = tab;
        this.ic_drawable = ic_drawable;
        this.name = name;
        this.ic_text_color = ic_text_color;
    }

    public void setSelectedResource(int ic_drawable_selected, int ic_text_color_selected) {
        this.ic_drawable_selected = ic_drawable_selected;
        this.ic_text_color_selected = ic_text_color_selected;
    }

    public int getType() {return tab;}
    public int getIconDrawable() {
        return (selected ? ic_drawable_selected : ic_drawable);
    }
    public String getName() {return name;}
    public int getTextColor() {
        return (selected ? ic_text_color_selected : ic_text_color);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
