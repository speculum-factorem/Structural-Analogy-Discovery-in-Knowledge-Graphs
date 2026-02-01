package com.sagam.verification;

import com.sagam.core.Entity;
import com.sagam.core.KnowledgeGraph;
import com.sagam.core.Relation;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

/**
 * Гибридный верификатор: комбинация точных и приближенных алгоритмов
 */
public class HybridVerifier {
    private final KnowledgeGraph.Ontology ontology;
    private final double similarityThreshold;
    
    public HybridVerifier(KnowledgeGraph.Ontology ontology, double similarityThreshold) {
        this.ontology = ontology;
        this.similarityThreshold = similarityThreshold;
    }
    
    /**
     * Вероятностная проверка с использованием случайных блужданий
     */
    public double probabilisticMatch(KnowledgeGraph query, KnowledgeGraph candidate,
                                     int numWalks, int walkLength) {
        if (query.getVertexCount() == 0 || candidate.getVertexCount() == 0) {
            return 0.0;
        }
        
        double totalSimilarity = 0.0;
        int comparisons = 0;
        
        for (Entity queryNode : query.getEntities()) {
            List<List<Entity>> queryWalks = performRandomWalks(query, queryNode, numWalks, walkLength);
            
            for (Entity candidateNode : candidate.getEntities()) {
                List<List<Entity>> candidateWalks = performRandomWalks(candidate, candidateNode, 
                                                                       numWalks, walkLength);
                double walkSimilarity = computeWalkSimilarity(queryWalks, candidateWalks);
                totalSimilarity += walkSimilarity;
                comparisons++;
            }
        }
        
        return comparisons > 0 ? totalSimilarity / comparisons : 0.0;
    }
    
    /**
     * Выполняет случайные блуждания из начальной вершины
     */
    private List<List<Entity>> performRandomWalks(KnowledgeGraph graph, Entity startNode,
                                                   int numWalks, int walkLength) {
        List<List<Entity>> walks = new ArrayList<>();
        Random random = new Random();
        Graph<Entity, DefaultWeightedEdge> g = graph.getGraph();
        
        for (int i = 0; i < numWalks; i++) {
            List<Entity> walk = new ArrayList<>();
            Entity current = startNode;
            walk.add(current);
            
            for (int step = 0; step < walkLength; step++) {
                Set<DefaultWeightedEdge> edges = g.edgesOf(current);
                if (edges.isEmpty()) {
                    break;
                }
                
                // Выбираем случайное ребро с учетом весов
                DefaultWeightedEdge[] edgeArray = edges.toArray(new DefaultWeightedEdge[0]);
                DefaultWeightedEdge selectedEdge = edgeArray[random.nextInt(edgeArray.length)];
                
                Entity source = g.getEdgeSource(selectedEdge);
                Entity target = g.getEdgeTarget(selectedEdge);
                current = source.equals(current) ? target : source;
                walk.add(current);
            }
            
            walks.add(walk);
        }
        
        return walks;
    }
    
    /**
     * Вычисляет схожесть путей блужданий
     */
    private double computeWalkSimilarity(List<List<Entity>> walks1, List<List<Entity>> walks2) {
        if (walks1.isEmpty() || walks2.isEmpty()) {
            return 0.0;
        }
        
        double totalSimilarity = 0.0;
        int comparisons = 0;
        
        for (List<Entity> walk1 : walks1) {
            for (List<Entity> walk2 : walks2) {
                double walkSim = computeSingleWalkSimilarity(walk1, walk2);
                totalSimilarity += walkSim;
                comparisons++;
            }
        }
        
        return comparisons > 0 ? totalSimilarity / comparisons : 0.0;
    }
    
    /**
     * Вычисляет схожесть двух путей
     */
    private double computeSingleWalkSimilarity(List<Entity> walk1, List<Entity> walk2) {
        if (walk1.isEmpty() || walk2.isEmpty()) {
            return 0.0;
        }
        
        // Сравниваем типы сущностей в путях
        int minLength = Math.min(walk1.size(), walk2.size());
        int matches = 0;
        
        for (int i = 0; i < minLength; i++) {
            Entity e1 = walk1.get(i);
            Entity e2 = walk2.get(i);
            
            // Проверяем таксономическую схожесть
            Set<String> types1 = e1.getTypes();
            Set<String> types2 = e2.getTypes();
            
            if (!types1.isEmpty() && !types2.isEmpty()) {
                Set<String> intersection = new HashSet<>(types1);
                intersection.retainAll(types2);
                Set<String> union = new HashSet<>(types1);
                union.addAll(types2);
                
                if (!union.isEmpty()) {
                    double typeSim = (double) intersection.size() / union.size();
                    if (typeSim > 0.5) {
                        matches++;
                    }
                }
            }
        }
        
        return minLength > 0 ? (double) matches / minLength : 0.0;
    }
    
    /**
     * Точная проверка с отсечениями (упрощенная версия Ullmann)
     */
    public boolean exactMatchWithPruning(KnowledgeGraph query, KnowledgeGraph candidate) {
        if (query.getVertexCount() != candidate.getVertexCount()) {
            return false;
        }
        
        if (query.getEdgeCount() != candidate.getEdgeCount()) {
            return false;
        }
        
        // Поиск изоморфизма с отсечениями
        return findIsomorphism(query, candidate, new HashMap<>(), new HashSet<>());
    }
    
    /**
     * Рекурсивный поиск изоморфизма с отсечениями
     */
    private boolean findIsomorphism(KnowledgeGraph query, KnowledgeGraph candidate,
                                    Map<Entity, Entity> mapping, Set<Entity> used) {
        if (mapping.size() == query.getVertexCount()) {
            return verifyMapping(query, candidate, mapping);
        }
        
        // Выбираем следующую неотображенную вершину из query
        Entity queryVertex = null;
        for (Entity v : query.getEntities()) {
            if (!mapping.containsKey(v)) {
                queryVertex = v;
                break;
            }
        }
        
        if (queryVertex == null) {
            return false;
        }
        
        // Пробуем отобразить на кандидаты с похожими типами
        for (Entity candidateVertex : candidate.getEntities()) {
            if (used.contains(candidateVertex)) {
                continue;
            }
            
            // Проверка совместимости типов
            if (!areCompatible(queryVertex, candidateVertex)) {
                continue;
            }
            
            // Проверка локальной структуры
            if (!checkLocalStructure(query, candidate, queryVertex, candidateVertex, mapping)) {
                continue;
            }
            
            mapping.put(queryVertex, candidateVertex);
            used.add(candidateVertex);
            
            if (findIsomorphism(query, candidate, mapping, used)) {
                return true;
            }
            
            mapping.remove(queryVertex);
            used.remove(candidateVertex);
        }
        
        return false;
    }
    
    /**
     * Проверяет совместимость вершин по типам
     */
    private boolean areCompatible(Entity e1, Entity e2) {
        Set<String> types1 = e1.getTypes();
        Set<String> types2 = e2.getTypes();
        
        if (types1.isEmpty() || types2.isEmpty()) {
            return true;  // Если нет типов, считаем совместимыми
        }
        
        // Есть пересечение типов
        Set<String> intersection = new HashSet<>(types1);
        intersection.retainAll(types2);
        return !intersection.isEmpty();
    }
    
    /**
     * Проверяет локальную структуру вокруг вершин
     */
    private boolean checkLocalStructure(KnowledgeGraph query, KnowledgeGraph candidate,
                                       Entity qVertex, Entity cVertex,
                                       Map<Entity, Entity> mapping) {
        Graph<Entity, DefaultWeightedEdge> qGraph = query.getGraph();
        Graph<Entity, DefaultWeightedEdge> cGraph = candidate.getGraph();
        
        // Проверяем степени
        if (qGraph.degreeOf(qVertex) != cGraph.degreeOf(cVertex)) {
            return false;
        }
        
        // Проверяем уже отображенных соседей
        Set<DefaultWeightedEdge> qEdges = qGraph.edgesOf(qVertex);
        Set<DefaultWeightedEdge> cEdges = cGraph.edgesOf(cVertex);
        
        for (DefaultWeightedEdge qEdge : qEdges) {
            Entity qNeighbor = qGraph.getEdgeTarget(qEdge).equals(qVertex) ?
                    qGraph.getEdgeSource(qEdge) : qGraph.getEdgeTarget(qEdge);
            
            if (mapping.containsKey(qNeighbor)) {
                Entity expectedNeighbor = mapping.get(qNeighbor);
                boolean found = false;
                for (DefaultWeightedEdge cEdge : cEdges) {
                    Entity cNeighbor = cGraph.getEdgeTarget(cEdge).equals(cVertex) ?
                            cGraph.getEdgeSource(cEdge) : cGraph.getEdgeTarget(cEdge);
                    if (cNeighbor.equals(expectedNeighbor)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Проверяет корректность полного отображения
     */
    private boolean verifyMapping(KnowledgeGraph query, KnowledgeGraph candidate,
                                 Map<Entity, Entity> mapping) {
        Graph<Entity, DefaultWeightedEdge> qGraph = query.getGraph();
        Graph<Entity, DefaultWeightedEdge> cGraph = candidate.getGraph();
        
        for (DefaultWeightedEdge qEdge : qGraph.edgeSet()) {
            Entity qSource = qGraph.getEdgeSource(qEdge);
            Entity qTarget = qGraph.getEdgeTarget(qEdge);
            
            Entity cSource = mapping.get(qSource);
            Entity cTarget = mapping.get(qTarget);
            
            DefaultWeightedEdge cEdge = cGraph.getEdge(cSource, cTarget);
            if (cEdge == null) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Комбинированная верификация
     */
    public double verify(KnowledgeGraph query, KnowledgeGraph candidate) {
        // Сначала быстрая вероятностная проверка
        double probScore = probabilisticMatch(query, candidate, 10, 3);
        
        if (probScore < similarityThreshold) {
            return probScore;
        }
        
        // Если вероятностная проверка прошла, делаем точную
        boolean exactMatch = exactMatchWithPruning(query, candidate);
        
        if (exactMatch) {
            return 1.0;
        }
        
        // Возвращаем взвешенную комбинацию
        return 0.7 * probScore + 0.3 * (exactMatch ? 1.0 : 0.0);
    }
}

