package com.sagam.demo;

import com.sagam.core.KnowledgeGraph;
import com.sagam.dataset.DatasetExporter;
import com.sagam.dataset.DatasetGenerator;

import java.io.IOException;

/**
 * Демонстрация создания и экспорта датасета
 */
public class DatasetDemo {
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("SAGAM Dataset Generator Demo");
        System.out.println("==========================================\n");
        
        // Создание графа знаний
        System.out.println("Создание графа знаний с кросс-доменными аналогиями...");
        KnowledgeGraph kg = DatasetGenerator.generateCrossDomainKnowledgeGraph();
        
        System.out.println("\nСтатистика графа:");
        System.out.println("  Вершин: " + kg.getVertexCount());
        System.out.println("  Ребер: " + kg.getEdgeCount());
        System.out.println("  Домены: биология, кибербезопасность, финансы, материаловедение\n");
        
        // Вывод информации о доменах
        System.out.println("Домены в графе:");
        System.out.println("1. Биология:");
        kg.getEntities().stream()
                .filter(e -> e.getId().startsWith("bio_"))
                .forEach(e -> System.out.println("   - " + e.getLabel()));
        
        System.out.println("\n2. Кибербезопасность:");
        kg.getEntities().stream()
                .filter(e -> e.getId().startsWith("cs_"))
                .forEach(e -> System.out.println("   - " + e.getLabel()));
        
        System.out.println("\n3. Финансы:");
        kg.getEntities().stream()
                .filter(e -> e.getId().startsWith("fin_"))
                .forEach(e -> System.out.println("   - " + e.getLabel()));
        
        System.out.println("\n4. Материаловедение:");
        kg.getEntities().stream()
                .filter(e -> e.getId().startsWith("mat_"))
                .forEach(e -> System.out.println("   - " + e.getLabel()));
        
        // Экспорт в различные форматы
        System.out.println("\n==========================================");
        System.out.println("Экспорт датасета");
        System.out.println("==========================================\n");
        
        try {
            DatasetExporter.exportToJSON(kg, "dataset.json");
            DatasetExporter.exportToGraphML(kg, "dataset.graphml");
            DatasetExporter.exportToText(kg, "dataset.txt");
            
            System.out.println("\nВсе форматы успешно экспортированы!");
        } catch (IOException e) {
            System.err.println("Ошибка при экспорте: " + e.getMessage());
        }
        
        // Демонстрация аналогий
        System.out.println("\n==========================================");
        System.out.println("Примеры структурных аналогий");
        System.out.println("==========================================\n");
        
        System.out.println("Аналогия 1: Биология ↔ Кибербезопасность");
        System.out.println("  Структура: [A] → нарушает → [B] → ведет к → [C]");
        System.out.println("  Биология:");
        System.out.println("    [BRCA1 Gene] → mutates → [DNA Repair] → disrupts → [Cell Division]");
        System.out.println("  Кибербезопасность:");
        System.out.println("    [Kernel Vulnerability] → exploits → [Defense] → bypasses → [Unauthorized Access]");
        
        System.out.println("\nАналогия 2: Биология ↔ Финансы");
        System.out.println("  Структура: [X] → нарушает → [Y] → вызывает → [Z]");
        System.out.println("  Биология:");
        System.out.println("    [BRCA1] → mutates → [DNA Repair] → causes → [Breast Cancer]");
        System.out.println("  Финансы:");
        System.out.println("    [Toxic Derivatives] → introduces → [Trust Chain] → causes → [Financial Crisis]");
    }
}

