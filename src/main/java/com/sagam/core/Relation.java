package com.sagam.core;

/**
 * Представляет отношение (ребро) в графе знаний
 */
public class Relation {
    private final String id;
    private final String label;
    private final String domain;  // Домен отношения
    private final double weight;  // Вес информативности [0,1]
    
    public Relation(String id, String label) {
        this(id, label, "general", 1.0);
    }
    
    public Relation(String id, String label, String domain, double weight) {
        this.id = id;
        this.label = label;
        this.domain = domain;
        this.weight = Math.max(0.0, Math.min(1.0, weight));
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public double getWeight() {
        return weight;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return id.equals(relation.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Relation{id='%s', label='%s', domain='%s', weight=%.2f}", 
                id, label, domain, weight);
    }
}

