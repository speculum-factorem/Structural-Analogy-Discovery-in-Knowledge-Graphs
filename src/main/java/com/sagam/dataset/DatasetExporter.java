package com.sagam.dataset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sagam.core.Entity;
import com.sagam.core.KnowledgeGraph;
import com.sagam.core.Relation;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Экспортер датасета в различные форматы
 */
public class DatasetExporter {
    
    /**
     * Экспортирует граф в JSON формат
     */
    public static void exportToJSON(KnowledgeGraph kg, String filename) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        Map<String, Object> graphData = new HashMap<>();
        
        // Вершины
        List<Map<String, Object>> vertices = new ArrayList<>();
        for (Entity entity : kg.getEntities()) {
            Map<String, Object> vertex = new HashMap<>();
            vertex.put("id", entity.getId());
            vertex.put("label", entity.getLabel());
            vertex.put("types", entity.getTypes());
            vertices.add(vertex);
        }
        graphData.put("vertices", vertices);
        
        // Ребра
        List<Map<String, Object>> edges = new ArrayList<>();
        Graph<Entity, DefaultWeightedEdge> graph = kg.getGraph();
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            Map<String, Object> edgeData = new HashMap<>();
            Entity source = graph.getEdgeSource(edge);
            Entity target = graph.getEdgeTarget(edge);
            Relation relation = kg.getRelation(edge);
            
            edgeData.put("source", source.getId());
            edgeData.put("target", target.getId());
            edgeData.put("relation", relation != null ? relation.getId() : "unknown");
            edgeData.put("label", relation != null ? relation.getLabel() : "unknown");
            edgeData.put("weight", graph.getEdgeWeight(edge));
            edges.add(edgeData);
        }
        graphData.put("edges", edges);
        
        // Статистика
        Map<String, Object> stats = new HashMap<>();
        stats.put("vertexCount", kg.getVertexCount());
        stats.put("edgeCount", kg.getEdgeCount());
        graphData.put("statistics", stats);
        
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(graphData, writer);
        }
        
        System.out.println("Граф экспортирован в: " + filename);
    }
    
    /**
     * Экспортирует граф в формат GraphML
     */
    public static void exportToGraphML(KnowledgeGraph kg, String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
        sb.append("  <key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>\n");
        sb.append("  <key id=\"types\" for=\"node\" attr.name=\"types\" attr.type=\"string\"/>\n");
        sb.append("  <key id=\"relation\" for=\"edge\" attr.name=\"relation\" attr.type=\"string\"/>\n");
        sb.append("  <key id=\"weight\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>\n");
        sb.append("  <graph id=\"G\" edgedefault=\"directed\">\n");
        
        // Вершины
        for (Entity entity : kg.getEntities()) {
            sb.append("    <node id=\"").append(entity.getId()).append("\">\n");
            sb.append("      <data key=\"label\">").append(escapeXml(entity.getLabel())).append("</data>\n");
            sb.append("      <data key=\"types\">").append(String.join(",", entity.getTypes()))
                    .append("</data>\n");
            sb.append("    </node>\n");
        }
        
        // Ребра
        Graph<Entity, DefaultWeightedEdge> graph = kg.getGraph();
        int edgeId = 0;
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            Entity source = graph.getEdgeSource(edge);
            Entity target = graph.getEdgeTarget(edge);
            Relation relation = kg.getRelation(edge);
            
            sb.append("    <edge id=\"e").append(edgeId++).append("\" source=\"")
                    .append(source.getId()).append("\" target=\"").append(target.getId()).append("\">\n");
            if (relation != null) {
                sb.append("      <data key=\"relation\">").append(relation.getLabel()).append("</data>\n");
            }
            sb.append("      <data key=\"weight\">").append(graph.getEdgeWeight(edge)).append("</data>\n");
            sb.append("    </edge>\n");
        }
        
        sb.append("  </graph>\n");
        sb.append("</graphml>\n");
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(sb.toString());
        }
        
        System.out.println("Граф экспортирован в GraphML: " + filename);
    }
    
    /**
     * Экспортирует граф в простой текстовый формат (для визуализации)
     */
    public static void exportToText(KnowledgeGraph kg, String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Knowledge Graph Export\n");
        sb.append("=====================\n\n");
        sb.append("Statistics:\n");
        sb.append("  Vertices: ").append(kg.getVertexCount()).append("\n");
        sb.append("  Edges: ").append(kg.getEdgeCount()).append("\n\n");
        
        sb.append("Vertices:\n");
        for (Entity entity : kg.getEntities()) {
            sb.append("  ").append(entity.getId())
                    .append(" (").append(entity.getLabel()).append(")\n");
            sb.append("    Types: ").append(String.join(", ", entity.getTypes())).append("\n");
        }
        
        sb.append("\nEdges:\n");
        Graph<Entity, DefaultWeightedEdge> graph = kg.getGraph();
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            Entity source = graph.getEdgeSource(edge);
            Entity target = graph.getEdgeTarget(edge);
            Relation relation = kg.getRelation(edge);
            
            sb.append("  ").append(source.getId())
                    .append(" --[").append(relation != null ? relation.getLabel() : "unknown")
                    .append("]--> ").append(target.getId()).append("\n");
        }
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(sb.toString());
        }
        
        System.out.println("Граф экспортирован в текстовый формат: " + filename);
    }
    
    private static String escapeXml(String str) {
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

