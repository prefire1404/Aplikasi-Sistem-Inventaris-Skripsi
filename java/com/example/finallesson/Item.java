package com.example.finallesson;

public class Item {
    private String id;
    private String name;
    private String category;
    private String condition;
    private String quantity;
    private String dateAdded;
    private String imageUrl;
    private String UserId;
    private String documentId;

    public Item() {
    }

    public Item(String id, String name, String category, String condition, String quantity, String dateAdded,String imageUrl, String UserId, String documentId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.condition = condition;
        this.quantity = quantity;
        this.dateAdded = dateAdded;
        this.imageUrl = imageUrl;
        this.UserId = UserId;
        this.documentId = documentId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
