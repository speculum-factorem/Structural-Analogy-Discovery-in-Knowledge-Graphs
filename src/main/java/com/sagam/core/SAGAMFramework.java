package com.sagam.core;

import com.sagam.filtering.MultiLevelFilter;
import com.sagam.indexing.SemanticLSHIndex;
import com.sagam.verification.HybridVerifier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Основной фреймворк SAGAM для обнаружения структурных аналогий
 */
public class SAGAMFramework {
    private final KnowledgeGraph knowledgeGraph;
    private final SemanticLSHIndex lshIndex;
    private final MultiLevelFilter filter;
    private final HybridVerifier verifier;
    
    // Параметры по умолчанию
    private static final int DEFAULT_NUM_HASH_FUNCTIONS = 128;
    private static final int DEFAULT_RADIUS = 2;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    
    public SAGAMFramework(KnowledgeGraph knowledgeGraph) {
        this.knowledgeGraph = knowledgeGraph;
        this.lshIndex = new SemanticLSHIndex(
                DEFAULT_NUM_HASH_FUNCTIONS,
                DEFAULT_RADIUS,
                knowledgeGraph.getOntology()
        );
        this.filter = new MultiLevelFilter(lshIndex, knowledgeGraph.getOntology());
        this.verifier = new HybridVerifier(
                knowledgeGraph.getOntology(),
                DEFAULT_SIMILARITY_THRESHOLD
        );
        
        // Индексируем все подграфы графа знаний
        indexKnowledgeGraph();
    }
    
    public SAGAMFramework(KnowledgeGraph knowledgeGraph,
                         int numHashFunctions,
                         int radius,
                         double similarityThreshold) {
        this.knowledgeGraph = knowledgeGraph;
        this.lshIndex = new SemanticLSHIndex(
                numHashFunctions,
                radius,
                knowledgeGraph.getOntology()
        );
        this.filter = new MultiLevelFilter(lshIndex, knowledgeGraph.getOntology());
        this.verifier = new HybridVerifier(
                knowledgeGraph.getOntology(),
                similarityThreshold
        );
        
        indexKnowledgeGraph();
    }
    
    /**
     * Индексирует паттерны из графа знаний
     */
    private void indexKnowledgeGraph() {
        // Извлекаем все возможные подграфы (упрощенная версия)
        // В реальной реализации здесь был бы более сложный алгоритм извлечения паттернов
        List<KnowledgeGraph> patterns = extractPatterns(knowledgeGraph);
        
        for (KnowledgeGraph pattern : patterns) {
            lshIndex.indexPattern(pattern);
        }
    }
    
    /**
     * Извлекает паттерны из графа (упрощенная версия)
     */
    private List<KnowledgeGraph> extractPatterns(KnowledgeGraph graph) {
        List<KnowledgeGraph> patterns = new ArrayList<>();
        Set<Entity> processed = new HashSet<>();
        
        // Извлекаем паттерны вокруг каждой вершины
        for (Entity entity : graph.getEntities()) {
            if (processed.contains(entity)) {
                continue;
            }
            
            // Создаем паттерн из k-hop окрестности
            Set<Entity> neighbors = graph.getKHopNeighbors(entity, 2);
            neighbors.add(entity);
            
            KnowledgeGraph pattern = graph.extractSubgraph(neighbors);
            if (pattern.getVertexCount() >= 2) {
                patterns.add(pattern);
                processed.addAll(neighbors);
            }
        }
        
        return patterns;
    }
    
    /**
     * Находит аналогии для заданного запроса
     */
    public List<AnalogyResult> findAnalogies(KnowledgeGraph query, int topK) {
        // Фаза 1: Поиск кандидатов через LSH
        List<KnowledgeGraph> candidates = lshIndex.findCandidates(query, 0.5);
        
        // Фаза 2: Многоуровневая фильтрация
        List<KnowledgeGraph> filtered = filter.filter(query, candidates);
        
        // Фаза 3: Верификация и ранжирование
        List<AnalogyResult> results = new ArrayList<>();
        
        for (KnowledgeGraph candidate : filtered) {
            double similarity = verifier.verify(query, candidate);
            
            // Проверяем, что это кросс-доменная аналогия
            if (isCrossDomainAnalogy(query, candidate)) {
                AnalogyResult result = new AnalogyResult(query, candidate, similarity);
                results.add(result);
            }
        }
        
        // Сортируем по схожести и возвращаем топ-K
        return results.stream()
                .sorted(Comparator.comparing(AnalogyResult::getSimilarity).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }
    
    /**
     * Проверяет, является ли аналогия кросс-доменной
     */
    private boolean isCrossDomainAnalogy(KnowledgeGraph g1, KnowledgeGraph g2) {
        Set<String> domains1 = extractDomains(g1);
        Set<String> domains2 = extractDomains(g2);
        
        Set<String> intersection = new HashSet<>(domains1);
        intersection.retainAll(domains2);
        
        // Кросс-доменная аналогия: разные домены или минимальное пересечение
        return intersection.isEmpty() || 
               (intersection.size() < Math.min(domains1.size(), domains2.size()) * 0.3);
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
     * Результат аналогии
     */
    public static class AnalogyResult {
        private final KnowledgeGraph query;
        private final KnowledgeGraph candidate;
        private final double similarity;
        
        public AnalogyResult(KnowledgeGraph query, KnowledgeGraph candidate, double similarity) {
            this.query = query;
            this.candidate = candidate;
            this.similarity = similarity;
        }
        
        public KnowledgeGraph getQuery() {
            return query;
        }
        
        public KnowledgeGraph getCandidate() {
            return candidate;
        }
        
        public double getSimilarity() {
            return similarity;
        }
        
        @Override
        public String toString() {
            return String.format("AnalogyResult{similarity=%.3f, query=%d vertices, candidate=%d vertices}",
                    similarity, query.getVertexCount(), candidate.getVertexCount());
        }
    }
}

