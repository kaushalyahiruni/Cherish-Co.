package lk.javainstitute.app28;

import java.io.Serializable;

public class CartItem implements Serializable {

    private String productName;
    private double productPrice;
    private int quantity;
    private String imageUrl;
    private int imageResource;
    private String documentId;

    public CartItem() {
    }

    public CartItem(String productName, double productPrice, int quantity, String imageUrl) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public CartItem(int imageResource, String productName, double productPrice, int quantity, String imageUrl) {
        this.imageResource = imageResource;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getDocumentId() {
        return documentId;
    }
}