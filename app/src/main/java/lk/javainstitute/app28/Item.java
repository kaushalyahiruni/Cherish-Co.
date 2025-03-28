package lk.javainstitute.app28;

public class Item {
    private int imageResourceId;
    private String title;
    private String price;
    private String description;

    public Item(int imageResourceId, String title, String price, String description) {
        this.imageResourceId = imageResourceId;
        this.title = title;
        this.price = price;
        this.description = description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }
}
