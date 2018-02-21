package br.ufsc.lapesd.sro.tokit;

import java.util.HashMap;
import java.util.Map;

public class User  {
    private final String entityId;
    private final Map<String, String> properties;

    public User(String entityId, Map<String, String> properties) {
        this.entityId = entityId;
        this.properties = properties;
    }

    public User(String entityId) {
        this.entityId = entityId;
        this.properties = new HashMap<String, String>();
    }
    
    public String getEntityId() {
        return entityId;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "User{" +
                "entityId='" + entityId + '\'' +
                ", properties=" + properties +
                '}';
    }
}