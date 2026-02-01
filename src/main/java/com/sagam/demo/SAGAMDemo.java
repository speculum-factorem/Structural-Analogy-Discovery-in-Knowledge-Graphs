package com.sagam.demo;

import com.sagam.core.KnowledgeGraph;
import com.sagam.core.SAGAMFramework;
import com.sagam.dataset.DatasetGenerator;

import java.util.List;

/**
 * Демонстрация работы алгоритма SAGAM
 */
public class SAGAMDemo {
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("SAGAM Framework Demo");
        System.out.println("Structural Analogy Discovery in Knowledge Graphs");
        System.out.println("==========================================\n");
        
        // Шаг 1: Создание графа знаний с кросс-доменными аналогиями
        System.out.println("Шаг 1: Создание графа знаний...");
        KnowledgeGraph kg = DatasetGenerator.generateCrossDomainKnowledgeGraph();
        System.out.printf("Граф создан: %d вершин, %d ребер\n\n", 
                kg.getVertexCount(), kg.getEdgeCount());
        
        // Шаг 2: Инициализация фреймворка SAGAM
        System.out.println("Шаг 2: Инициализация SAGAM фреймворка...");
        SAGAMFramework sagam = new SAGAMFramework(kg, 64, 2, 0.6);
        System.out.println("Фреймворк инициализирован\n");
        
        // Шаг 3: Создание запроса
        System.out.println("Шаг 3: Создание запроса для поиска аналогий...");
        KnowledgeGraph query = DatasetGenerator.createQueryPattern();
        System.out.printf("Запрос создан: %d вершин, %d ребер\n", 
                query.getVertexCount(), query.getEdgeCount());
        System.out.println("Паттерн запроса: [Entity A] → disrupts → [Mechanism B] → leads_to → [Outcome C]\n");
        
        // Шаг 4: Поиск аналогий
        System.out.println("Шаг 4: Поиск структурных аналогий...");
        System.out.println("Применяются:");
        System.out.println("  - Semantic-Aware LSH индексация");
        System.out.println("  - Многоуровневая фильтрация");
        System.out.println("  - Гибридная верификация\n");
        
        List<SAGAMFramework.AnalogyResult> results = sagam.findAnalogies(query, 10);
        
        // Шаг 5: Вывод результатов
        System.out.println("==========================================");
        System.out.println("РЕЗУЛЬТАТЫ ПОИСКА АНАЛОГИЙ");
        System.out.println("==========================================\n");
        
        if (results.isEmpty()) {
            System.out.println("Аналогии не найдены. Попробуйте изменить параметры поиска.");
        } else {
            System.out.printf("Найдено аналогий: %d\n\n", results.size());
            
            for (int i = 0; i < results.size(); i++) {
                SAGAMFramework.AnalogyResult result = results.get(i);
                System.out.printf("Аналогия #%d (схожесть: %.3f)\n", 
                        i + 1, result.getSimilarity());
                System.out.printf("  Запрос: %d вершин, %d ребер\n",
                        result.getQuery().getVertexCount(),
                        result.getQuery().getEdgeCount());
                System.out.printf("  Кандидат: %d вершин, %d ребер\n",
                        result.getCandidate().getVertexCount(),
                        result.getCandidate().getEdgeCount());
                
                // Выводим информацию о доменах
                System.out.print("  Домены кандидата: ");
                result.getCandidate().getEntities().forEach(e -> {
                    e.getTypes().forEach(t -> {
                        if (!t.equals("query") && !t.equals("entity") && 
                            !t.equals("mechanism") && !t.equals("outcome")) {
                            System.out.print(t + " ");
                        }
                    });
                });
                System.out.println("\n");
            }
        }
        
        // Дополнительная демонстрация
        System.out.println("==========================================");
        System.out.println("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");
        System.out.println("==========================================\n");
        
        System.out.println("Примеры кросс-доменных аналогий в датасете:");
        System.out.println("1. Биология ↔ Кибербезопасность:");
        System.out.println("   [Мутация гена] → [Уязвимость системы]");
        System.out.println("   [Репарация ДНК] → [Механизмы защиты]");
        System.out.println("   [Рак] → [Системный взлом]\n");
        
        System.out.println("2. Биология ↔ Финансы:");
        System.out.println("   [Мутация] → [Токсичный актив]");
        System.out.println("   [Нарушение механизма] → [Нарушение цепочки доверия]");
        System.out.println("   [Болезнь] → [Кризис]\n");
        
        System.out.println("3. Материаловедение ↔ Биология:");
        System.out.println("   [Структура] → [Биологическая структура]");
        System.out.println("   [Нано-ребра] → [Хитиновые структуры]");
        System.out.println("   [Оптические свойства] → [Структурная окраска]\n");
        
        System.out.println("==========================================");
        System.out.println("Демонстрация завершена!");
        System.out.println("==========================================");
    }
}

