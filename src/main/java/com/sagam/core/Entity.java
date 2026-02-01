package com.sagam.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Представляет сущность в графе знаний
 */
public class Entity {
    private final String id;
    private final String label;
    private final Set<String> types;  // Таксономические типы
    private final Set<String> attributes;  // Атрибуты сущности
    
    public Entity(String id, String label) {
        this.id = id;
        this.label = label;
        this.types = new HashSet<>();
        this.attributes = new HashSet<>();
    }
    
    public Entity(String id, String label, Set<String> types) {
        this.id = id;
        this.label = label;
        this.types = types != null ? new HashSet<>(types) : new HashSet<>();
        this.attributes = new HashSet<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public Set<String> getTypes() {
        return new HashSet<>(types);
    }
    
    public void addType(String type) {
        this.types.add(type);
    }
    
    public void addAttribute(String attribute) {
        this.attributes.add(attribute);
    }
    
    public Set<String> getAttributes() {
        return new HashSet<>(attributes);
    }
    
    public boolean hasType(String type) {
        return types.contains(type);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id.equals(entity.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Entity{id='%s', label='%s', types=%s}", 
                id, label, types);
    }
}

