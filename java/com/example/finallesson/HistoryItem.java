package com.example.finallesson;

public class HistoryItem {
    private String action;
    private String date;
    private String documentId;
    private String collectionName;
    private String itemId;
    private String id;
    private String items;
    private String name;
    private String category;
    private String condition;
    private String quantity;
    private String dateAdded;

    public HistoryItem(String action, String date, String documentId, String collectionName) {
        this.action = action;
        this.date = date;
        this.documentId = documentId;
        this.collectionName = collectionName;
    }

    public HistoryItem(String action, String date, String itemId, String id, String items, String name, String category, String condition, String quantity, String dateAdded) {
        this.action = action;
        this.date = date;
        this.itemId = itemId;
        this.id = id;
        this.items = items;
        this.name = name;
        this.category = category;
        this.condition = condition;
        this.quantity = quantity;
        this.dateAdded = dateAdded;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
