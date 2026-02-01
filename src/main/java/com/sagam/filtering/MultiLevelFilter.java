package com.sagam.filtering;

import com.sagam.core.Entity;
import com.sagam.core.KnowledgeGraph;
import com.sagam.indexing.SemanticLSHIndex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Многоуровневая стратегия фильтрации кандидатов
 */
public class MultiLevelFilter {
    private final SemanticLSHIndex lshIndex;
    private final KnowledgeGraph.Ontology ontology;
    
    // Пороги для каждого уровня
    private final double l0Threshold = 0.85;  // Онтологический префильтр
    private final double l1Threshold = 0.70;  // LSH-индексация
    private final double l2Threshold = 0.60;  // Графлет-фильтр
    private final double l3Threshold = 0.50;  // Быстрая верификация
    
    public MultiLevelFilter(SemanticLSHIndex lshIndex, KnowledgeGraph.Ontology ontology) {
        this.lshIndex = lshIndex;
        this.ontology = ontology;
    }
    
    /**
     * Применяет многоуровневую фильтрацию
     */
    public List<KnowledgeGraph> filter(KnowledgeGraph query, List<KnowledgeGraph> allCandidates) {
        List<KnowledgeGraph> candidates = new ArrayList<>(allCandidates);
        
        // L0: Онтологический префильтр
        candidates = level0OntologicalFilter(query, candidates);
        
        // L1: LSH-индексация
        candidates = level1LSHFilter(query, candidates);
        
        // L2: Графлет-фильтр
        candidates = level2GraphletFilter(query, candidates);
        
        // L3: Быстрая верификация
        candidates = level3QuickVerification(query, candidates);
        
        return candidates;
    }
    
    /**
     * L0: Онтологический префильтр
     * Фильтрует по таксономической схожести доменов
     */
    private List<KnowledgeGraph> level0OntologicalFilter(KnowledgeGraph query, 
                                                         List<KnowledgeGraph> candidates) {
        Set<String> queryDomains = extractDomains(query);
        
        return candidates.stream()
                .filter(candidate -> {
                    Set<String> candidateDomains = extractDomains(candidate);
                    double domainSimilarity = computeDomainSimilarity(queryDomains, candidateDomains);
                    return domainSimilarity >= l0Threshold || 
                           areCrossDomain(queryDomains, candidateDomains);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * L1: LSH-индексация
     */
    private List<KnowledgeGraph> level1LSHFilter(KnowledgeGraph query, 
                                                  List<KnowledgeGraph> candidates) {
        return lshIndex.findCandidates(query, l1Threshold);
    }
    
    /**
     * L2: Графлет-фильтр
     * Сравнивает структурные характеристики
     */
    private List<KnowledgeGraph> level2GraphletFilter(KnowledgeGraph query, 
                                                       List<KnowledgeGraph> candidates) {
        GraphletProfile queryProfile = computeGraphletProfile(query);
        
        return candidates.stream()
                .filter(candidate -> {
                    GraphletProfile candidateProfile = computeGraphletProfile(candidate);
                    double similarity = queryProfile.similarity(candidateProfile);
                    return similarity >= l2Threshold;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * L3: Быстрая верификация
     * Простая проверка структурного сходства
     */
    private List<KnowledgeGraph> level3QuickVerification(KnowledgeGraph query, 
                                                          List<KnowledgeGraph> candidates) {
        return candidates.stream()
                .filter(candidate -> {
                    double structuralSimilarity = computeStructuralSimilarity(query, candidate);
                    return structuralSimilarity >= l3Threshold;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Извлекает домены из графа
     */
    private Set<String> extractDomains(KnowledgeGraph graph) {
        Set<String> domains = new HashSet<>();
        for (Entity entity : graph.getEntities()) {
            domains.addAll(entity.getTypes());
        }
        return domains;
    }
    
    /**
     * Вычисляет схожесть доменов
     */
    private double computeDomainSimilarity(Set<String> domains1, Set<String> domains2) {
        if (domains1.isEmpty() || domains2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(domains1);
        intersection.retainAll(domains2);
        Set<String> union = new HashSet<>(domains1);
        union.addAll(domains2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Проверяет, являются ли домены кросс-доменными
     */
    private boolean areCrossDomain(Set<String> domains1, Set<String> domains2) {
        Set<String> intersection = new HashSet<>(domains1);
        intersection.retainAll(domains2);
        return intersection.isEmpty() && !domains1.isEmpty() && !domains2.isEmpty();
    }
    
    /**
     * Вычисляет профиль графлетов
     */
    private GraphletProfile computeGraphletProfile(KnowledgeGraph graph) {
        int vertices = graph.getVertexCount();
        int edges = graph.getEdgeCount();
        double density = vertices > 1 ? (double) edges / (vertices * (vertices - 1)) : 0.0;
        
        // Средняя степень
        double avgDegree = 0.0;
        if (vertices > 0) {
            int totalDegree = 0;
            for (Entity e : graph.getEntities()) {
                totalDegree += graph.getGraph().degreeOf(e);
            }
            avgDegree = (double) totalDegree / vertices;
        }
        
        return new GraphletProfile(vertices, edges, density, avgDegree);
    }
    
    /**
     * Вычисляет структурное сходство
     */
    private double computeStructuralSimilarity(KnowledgeGraph g1, KnowledgeGraph g2) {
        GraphletProfile p1 = computeGraphletProfile(g1);
        GraphletProfile p2 = computeGraphletProfile(g2);
        return p1.similarity(p2);
    }
    
    /**
     * Профиль графлетов для сравнения
     */
    private static class GraphletProfile {
        final int vertexCount;
        final int edgeCount;
        final double density;
        final double avgDegree;
        
        GraphletProfile(int vertexCount, int edgeCount, double density, double avgDegree) {
            this.vertexCount = vertexCount;
            this.edgeCount = edgeCount;
            this.density = density;
            this.avgDegree = avgDegree;
        }
        
        double similarity(GraphletProfile other) {
            // Нормализованное сравнение характеристик
            double vSim = 1.0 - Math.abs(this.vertexCount - other.vertexCount) / 
                    (double) Math.max(Math.max(this.vertexCount, other.vertexCount), 1);
            double eSim = 1.0 - Math.abs(this.edgeCount - other.edgeCount) / 
                    (double) Math.max(Math.max(this.edgeCount, other.edgeCount), 1);
            double dSim = 1.0 - Math.abs(this.density - other.density);
            double degSim = 1.0 - Math.abs(this.avgDegree - other.avgDegree) / 
                    Math.max(Math.max(this.avgDegree, other.avgDegree), 1.0);
            
            return (vSim + eSim + dSim + degSim) / 4.0;
        }
    }
}

