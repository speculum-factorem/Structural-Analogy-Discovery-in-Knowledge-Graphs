package com.sagam.indexing;

import com.sagam.core.Entity;
import com.sagam.core.KnowledgeGraph;
import com.sagam.core.Relation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Semantic-Aware LSH (SA-LSH) индексация
 * Генерирует семантико-структурные сигнатуры для паттернов графа
 */
public class SemanticLSHIndex {
    private static final int PRIME = 2147483647;  // Большое простое число
    private final int numHashFunctions;
    private final int radius;
    private final KnowledgeGraph.Ontology ontology;
    private final Map<KnowledgeGraph, Signature> index;
    
    // Веса для комбинирования хешей
    private final double alpha;  // структурный
    private final double beta;   // семантический
    private final double gamma; // контекстный
    
    public SemanticLSHIndex(int numHashFunctions, int radius, KnowledgeGraph.Ontology ontology) {
        this.numHashFunctions = numHashFunctions;
        this.radius = radius;
        this.ontology = ontology;
        this.index = new HashMap<>();
        
        // По умолчанию равные веса
        this.alpha = 0.4;
        this.beta = 0.4;
        this.gamma = 0.2;
    }
    
    public SemanticLSHIndex(int numHashFunctions, int radius, KnowledgeGraph.Ontology ontology,
                           double alpha, double beta, double gamma) {
        this.numHashFunctions = numHashFunctions;
        this.radius = radius;
        this.ontology = ontology;
        this.index = new HashMap<>();
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }
    
    /**
     * Вычисляет сигнатуру для паттерна графа
     */
    public Signature computeSignature(KnowledgeGraph pattern) {
        Signature sig = new Signature(numHashFunctions);
        
        // 1. Структурные features (k-hop neighborhood)
        Set<StructuralFeature> structFeatures = extractKHopFeatures(pattern, radius);
        
        // 2. Семантические features (онтологические пути)
        Set<SemanticFeature> semFeatures = extractOntologyFeatures(pattern);
        
        // 3. Контекстные features (графлеты)
        Set<ContextFeature> contextFeatures = extractGraphletFeatures(pattern);
        
        // Комбинированная хеш-функция
        for (int i = 0; i < numHashFunctions; i++) {
            int hashStruct = hashStructural(structFeatures, i);
            int hashSem = hashSemantic(semFeatures, i);
            int hashContext = hashContext(contextFeatures, i);
            
            int combinedHash = combineHashes(hashStruct, hashSem, hashContext);
            sig.set(i, combinedHash);
        }
        
        return sig;
    }
    
    /**
     * Извлекает структурные features из k-hop окрестности
     */
    private Set<StructuralFeature> extractKHopFeatures(KnowledgeGraph pattern, int k) {
        Set<StructuralFeature> features = new HashSet<>();
        
        for (Entity entity : pattern.getEntities()) {
            Set<Entity> neighbors = pattern.getKHopNeighbors(entity, k);
            int degree = pattern.getGraph().degreeOf(entity);
            
            StructuralFeature feature = new StructuralFeature(
                    entity.getId(),
                    degree,
                    neighbors.size(),
                    k
            );
            features.add(feature);
        }
        
        return features;
    }
    
    /**
     * Извлекает семантические features из онтологии
     */
    private Set<SemanticFeature> extractOntologyFeatures(KnowledgeGraph pattern) {
        Set<SemanticFeature> features = new HashSet<>();
        
        for (Entity entity : pattern.getEntities()) {
            Set<String> types = entity.getTypes();
            for (String type : types) {
                SemanticFeature feature = new SemanticFeature(
                        entity.getId(),
                        type,
                        ontology.getTypes().contains(type)
                );
                features.add(feature);
            }
        }
        
        return features;
    }
    
    /**
     * Извлекает контекстные features (графлеты)
     */
    private Set<ContextFeature> extractGraphletFeatures(KnowledgeGraph pattern) {
        Set<ContextFeature> features = new HashSet<>();
        
        // Простые графлеты: количество треугольников, путей длины 2, и т.д.
        int vertexCount = pattern.getVertexCount();
        int edgeCount = pattern.getEdgeCount();
        double density = vertexCount > 0 ? (double) edgeCount / (vertexCount * (vertexCount - 1)) : 0.0;
        
        ContextFeature feature = new ContextFeature(vertexCount, edgeCount, density);
        features.add(feature);
        
        return features;
    }
    
    /**
     * Хеширует структурные features
     */
    private int hashStructural(Set<StructuralFeature> features, int hashIndex) {
        int hash = 0;
        for (StructuralFeature f : features) {
            hash = (hash * 31 + f.hashCode() * (hashIndex + 1)) % PRIME;
        }
        return Math.abs(hash);
    }
    
    /**
     * Хеширует семантические features
     */
    private int hashSemantic(Set<SemanticFeature> features, int hashIndex) {
        int hash = 0;
        for (SemanticFeature f : features) {
            hash = (hash * 37 + f.hashCode() * (hashIndex + 1)) % PRIME;
        }
        return Math.abs(hash);
    }
    
    /**
     * Хеширует контекстные features
     */
    private int hashContext(Set<ContextFeature> features, int hashIndex) {
        int hash = 0;
        for (ContextFeature f : features) {
            hash = (hash * 41 + f.hashCode() * (hashIndex + 1)) % PRIME;
        }
        return Math.abs(hash);
    }
    
    /**
     * Комбинирует хеши с учетом весов
     */
    private int combineHashes(int h1, int h2, int h3) {
        long combined = (long)(alpha * h1 + beta * h2 + gamma * h3);
        return (int)(combined % PRIME);
    }
    
    /**
     * Индексирует паттерн
     */
    public void indexPattern(KnowledgeGraph pattern) {
        Signature signature = computeSignature(pattern);
        index.put(pattern, signature);
    }
    
    /**
     * Находит кандидатов по сигнатуре запроса
     */
    public List<KnowledgeGraph> findCandidates(KnowledgeGraph query, double similarityThreshold) {
        Signature querySig = computeSignature(query);
        List<KnowledgeGraph> candidates = new ArrayList<>();
        
        for (Map.Entry<KnowledgeGraph, Signature> entry : index.entrySet()) {
            double similarity = querySig.similarity(entry.getValue());
            if (similarity >= similarityThreshold) {
                candidates.add(entry.getKey());
            }
        }
        
        return candidates;
    }
    
    // Внутренние классы для features
    private static class StructuralFeature {
        final String entityId;
        final int degree;
        final int neighborCount;
        final int hop;
        
        StructuralFeature(String entityId, int degree, int neighborCount, int hop) {
            this.entityId = entityId;
            this.degree = degree;
            this.neighborCount = neighborCount;
            this.hop = hop;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StructuralFeature that = (StructuralFeature) o;
            return degree == that.degree &&
                    neighborCount == that.neighborCount &&
                    hop == that.hop &&
                    Objects.equals(entityId, that.entityId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(entityId, degree, neighborCount, hop);
        }
    }
    
    private static class SemanticFeature {
        final String entityId;
        final String type;
        final boolean inOntology;
        
        SemanticFeature(String entityId, String type, boolean inOntology) {
            this.entityId = entityId;
            this.type = type;
            this.inOntology = inOntology;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SemanticFeature that = (SemanticFeature) o;
            return inOntology == that.inOntology &&
                    Objects.equals(entityId, that.entityId) &&
                    Objects.equals(type, that.type);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(entityId, type, inOntology);
        }
    }
    
    private static class ContextFeature {
        final int vertexCount;
        final int edgeCount;
        final double density;
        
        ContextFeature(int vertexCount, int edgeCount, double density) {
            this.vertexCount = vertexCount;
            this.edgeCount = edgeCount;
            this.density = density;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContextFeature that = (ContextFeature) o;
            return vertexCount == that.vertexCount &&
                    edgeCount == that.edgeCount &&
                    Double.compare(that.density, density) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(vertexCount, edgeCount, density);
        }
    }
}

