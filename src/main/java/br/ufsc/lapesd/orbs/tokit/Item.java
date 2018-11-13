package br.ufsc.lapesd.orbs.tokit;

import java.util.Set;

public class Item {
    private final Set<String> categories;
    private final String entityId;

    public Item(String entityId, Set<String> categories) {
        this.categories = categories;
        this.entityId = entityId;
    }

    public Item(String entityId) {
        this.categories = null;
        this.entityId = entityId;
    }
    
    public String getEntityId() {
        return entityId;
    }

    public Set<String> getCategories() {
        return categories;
    }

    @Override
    public String toString() {
        return "Item{" +
                "categories=" + categories +
                ", entityId='" + entityId + '\'' +
                '}';
    }

}