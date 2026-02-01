package com.sagam.core;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Модель графа знаний с семантическими слоями
 * KG = (V, E, L, O, C, W)
 */
public class KnowledgeGraph {
    private final Graph<Entity, DefaultWeightedEdge> graph;
    private final Map<String, Entity> entities;
    private final Map<String, Relation> relations;
    private final Map<DefaultWeightedEdge, Relation> edgeRelations;
    private final Ontology ontology;
    
    public KnowledgeGraph() {
        this.graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        this.entities = new HashMap<>();
        this.relations = new HashMap<>();
        this.edgeRelations = new HashMap<>();
        this.ontology = new Ontology();
    }
    
    /**
     * Добавляет сущность в граф
     */
    public void addEntity(Entity entity) {
        entities.put(entity.getId(), entity);
        graph.addVertex(entity);
        // Добавляем типы в онтологию
        for (String type : entity.getTypes()) {
            ontology.addType(type);
        }
    }
    
    /**
     * Добавляет отношение между сущностями
     */
    public void addRelation(Entity source, Relation relation, Entity target) {
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            throw new IllegalArgumentException("Both entities must be in the graph");
        }
        
        relations.put(relation.getId(), relation);
        DefaultWeightedEdge edge = graph.addEdge(source, target);
        if (edge != null) {
            graph.setEdgeWeight(edge, relation.getWeight());
            edgeRelations.put(edge, relation);
        }
    }
    
    /**
     * Получает все сущности определенного типа
     */
    public Set<Entity> getEntitiesByType(String type) {
        return entities.values().stream()
                .filter(e -> e.hasType(type))
                .collect(Collectors.toSet());
    }
    
    /**
     * Получает k-hop соседей вершины
     */
    public Set<Entity> getKHopNeighbors(Entity entity, int k) {
        Set<Entity> neighbors = new HashSet<>();
        Set<Entity> current = new HashSet<>();
        current.add(entity);
        
        for (int i = 0; i < k; i++) {
            Set<Entity> next = new HashSet<>();
            for (Entity e : current) {
                Set<DefaultWeightedEdge> edges = graph.edgesOf(e);
                for (DefaultWeightedEdge edge : edges) {
                    Entity source = graph.getEdgeSource(edge);
                    Entity target = graph.getEdgeTarget(edge);
                    Entity neighbor = source.equals(e) ? target : source;
                    if (!neighbors.contains(neighbor) && !neighbor.equals(entity)) {
                        neighbors.add(neighbor);
                        next.add(neighbor);
                    }
                }
            }
            current = next;
        }
        
        return neighbors;
    }
    
    /**
     * Извлекает подграф по заданным вершинам
     */
    public KnowledgeGraph extractSubgraph(Set<Entity> vertices) {
        KnowledgeGraph subgraph = new KnowledgeGraph();
        
        for (Entity v : vertices) {
            if (graph.containsVertex(v)) {
                subgraph.addEntity(v);
            }
        }
        
        for (Entity source : vertices) {
            Set<DefaultWeightedEdge> edges = graph.edgesOf(source);
            for (DefaultWeightedEdge edge : edges) {
                Entity target = graph.getEdgeTarget(edge);
                if (vertices.contains(target)) {
                    Relation rel = edgeRelations.get(edge);
                    subgraph.addRelation(source, rel, target);
                }
            }
        }
        
        return subgraph;
    }
    
    public Graph<Entity, DefaultWeightedEdge> getGraph() {
        return graph;
    }
    
    public Collection<Entity> getEntities() {
        return entities.values();
    }
    
    public Collection<Relation> getRelations() {
        return relations.values();
    }
    
    public Relation getRelation(DefaultWeightedEdge edge) {
        return edgeRelations.get(edge);
    }
    
    public Ontology getOntology() {
        return ontology;
    }
    
    public int getVertexCount() {
        return graph.vertexSet().size();
    }
    
    public int getEdgeCount() {
        return graph.edgeSet().size();
    }
    
    /**
     * Простая онтология для хранения таксономии
     */
    public static class Ontology {
        private final Set<String> types;
        private final Map<String, Set<String>> typeHierarchy;
        
        public Ontology() {
            this.types = new HashSet<>();
            this.typeHierarchy = new HashMap<>();
        }
        
        public void addType(String type) {
            types.add(type);
        }
        
        public void addSubtype(String parent, String child) {
            typeHierarchy.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
            types.add(parent);
            types.add(child);
        }
        
        public Set<String> getTypes() {
            return new HashSet<>(types);
        }
        
        public boolean isSubtype(String child, String parent) {
            Set<String> children = typeHierarchy.get(parent);
            return children != null && children.contains(child);
        }
    }
}

