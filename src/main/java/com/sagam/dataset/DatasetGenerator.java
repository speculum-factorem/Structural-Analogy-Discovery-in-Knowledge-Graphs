package com.sagam.dataset;

import com.sagam.core.Entity;
import com.sagam.core.KnowledgeGraph;
import com.sagam.core.Relation;

import java.util.*;

/**
 * Генератор синтетического датасета с кросс-доменными аналогиями
 * Создает граф знаний с аналогичными паттернами в разных доменах
 */
public class DatasetGenerator {
    
    /**
     * Создает граф знаний с аналогиями из разных доменов
     */
    public static KnowledgeGraph generateCrossDomainKnowledgeGraph() {
        KnowledgeGraph kg = new KnowledgeGraph();
        
        // Домен 1: Биология (рак и репарация ДНК)
        createBiologicalDomain(kg);
        
        // Домен 2: Кибербезопасность (уязвимости и защита)
        createCybersecurityDomain(kg);
        
        // Домен 3: Финансы (кризисы и стабильность)
        createFinancialDomain(kg);
        
        // Домен 4: Материаловедение (структуры и свойства)
        createMaterialsDomain(kg);
        
        return kg;
    }
    
    /**
     * Создает биологический домен: паттерн "мутация → нарушение → проблема"
     */
    private static void createBiologicalDomain(KnowledgeGraph kg) {
        // Сущности
        Entity brca1 = new Entity("bio_brca1", "BRCA1 Gene", 
                Set.of("gene", "biology", "genetics"));
        Entity dnaRepair = new Entity("bio_dna_repair", "DNA Repair Mechanism",
                Set.of("mechanism", "biology", "cellular_process"));
        Entity cellDivision = new Entity("bio_cell_division", "Uncontrolled Cell Division",
                Set.of("process", "biology", "disease"));
        Entity breastCancer = new Entity("bio_breast_cancer", "Breast Cancer",
                Set.of("disease", "biology", "oncology"));
        
        kg.addEntity(brca1);
        kg.addEntity(dnaRepair);
        kg.addEntity(cellDivision);
        kg.addEntity(breastCancer);
        
        // Отношения
        Relation mutates = new Relation("bio_mutates", "mutates", "biology", 0.9);
        Relation disrupts = new Relation("bio_disrupts", "disrupts", "biology", 0.85);
        Relation leadsTo = new Relation("bio_leads_to", "leads_to", "biology", 0.8);
        Relation causes = new Relation("bio_causes", "causes", "biology", 0.95);
        
        kg.addRelation(brca1, mutates, dnaRepair);
        kg.addRelation(dnaRepair, disrupts, cellDivision);
        kg.addRelation(cellDivision, leadsTo, breastCancer);
        kg.addRelation(brca1, causes, breastCancer);
    }
    
    /**
     * Создает домен кибербезопасности: аналогичный паттерн
     */
    private static void createCybersecurityDomain(KnowledgeGraph kg) {
        // Сущности
        Entity kernelVuln = new Entity("cs_kernel_vuln", "Kernel Vulnerability",
                Set.of("vulnerability", "cybersecurity", "software"));
        Entity defenseMech = new Entity("cs_defense", "Defense Mechanisms",
                Set.of("mechanism", "cybersecurity", "protection"));
        Entity unauthorizedAccess = new Entity("cs_unauthorized", "Unauthorized Access",
                Set.of("process", "cybersecurity", "threat"));
        Entity systemBreach = new Entity("cs_breach", "System Breach",
                Set.of("incident", "cybersecurity", "security"));
        
        kg.addEntity(kernelVuln);
        kg.addEntity(defenseMech);
        kg.addEntity(unauthorizedAccess);
        kg.addEntity(systemBreach);
        
        // Отношения (аналогичные биологическим)
        Relation exploits = new Relation("cs_exploits", "exploits", "cybersecurity", 0.9);
        Relation bypasses = new Relation("cs_bypasses", "bypasses", "cybersecurity", 0.85);
        Relation enables = new Relation("cs_enables", "enables", "cybersecurity", 0.8);
        Relation resultsIn = new Relation("cs_results_in", "results_in", "cybersecurity", 0.95);
        
        kg.addRelation(kernelVuln, exploits, defenseMech);
        kg.addRelation(defenseMech, bypasses, unauthorizedAccess);
        kg.addRelation(unauthorizedAccess, enables, systemBreach);
        kg.addRelation(kernelVuln, resultsIn, systemBreach);
    }
    
    /**
     * Создает финансовый домен: паттерн "токсичный актив → нарушение → кризис"
     */
    private static void createFinancialDomain(KnowledgeGraph kg) {
        // Сущности
        Entity toxicDerivatives = new Entity("fin_toxic", "Toxic Derivatives",
                Set.of("instrument", "finance", "derivatives"));
        Entity trustChain = new Entity("fin_trust", "Trust Chain",
                Set.of("mechanism", "finance", "system"));
        Entity marketPanic = new Entity("fin_panic", "Market Panic",
                Set.of("process", "finance", "behavior"));
        Entity financialCrisis = new Entity("fin_crisis", "Financial Crisis",
                Set.of("event", "finance", "crisis"));
        
        kg.addEntity(toxicDerivatives);
        kg.addEntity(trustChain);
        kg.addEntity(marketPanic);
        kg.addEntity(financialCrisis);
        
        // Отношения
        Relation introduces = new Relation("fin_introduces", "introduces", "finance", 0.9);
        Relation breaks = new Relation("fin_breaks", "breaks", "finance", 0.85);
        Relation triggers = new Relation("fin_triggers", "triggers", "finance", 0.8);
        Relation causes = new Relation("fin_causes", "causes", "finance", 0.95);
        
        kg.addRelation(toxicDerivatives, introduces, trustChain);
        kg.addRelation(trustChain, breaks, marketPanic);
        kg.addRelation(marketPanic, triggers, financialCrisis);
        kg.addRelation(toxicDerivatives, causes, financialCrisis);
    }
    
    /**
     * Создает домен материаловедения: паттерн "структура → свойство → функция"
     */
    private static void createMaterialsDomain(KnowledgeGraph kg) {
        // Сущности
        Entity chitinStructure = new Entity("mat_chitin", "Chitin Structure",
                Set.of("structure", "materials", "biology"));
        Entity nanoRibs = new Entity("mat_nano_ribs", "Nano-scale Ribs",
                Set.of("feature", "materials", "nanotechnology"));
        Entity structuralColor = new Entity("mat_color", "Structural Color",
                Set.of("property", "materials", "optics"));
        Entity butterflyWing = new Entity("mat_wing", "Butterfly Wing",
                Set.of("material", "materials", "biomimetics"));
        
        kg.addEntity(chitinStructure);
        kg.addEntity(nanoRibs);
        kg.addEntity(structuralColor);
        kg.addEntity(butterflyWing);
        
        // Отношения
        Relation contains = new Relation("mat_contains", "contains", "materials", 0.9);
        Relation creates = new Relation("mat_creates", "creates", "materials", 0.85);
        Relation produces = new Relation("mat_produces", "produces", "materials", 0.8);
        Relation forms = new Relation("mat_forms", "forms", "materials", 0.95);
        
        kg.addRelation(chitinStructure, contains, nanoRibs);
        kg.addRelation(nanoRibs, creates, structuralColor);
        kg.addRelation(structuralColor, produces, butterflyWing);
        kg.addRelation(chitinStructure, forms, butterflyWing);
    }
    
    /**
     * Создает расширенный граф с дополнительными паттернами
     */
    public static KnowledgeGraph generateExtendedDataset() {
        KnowledgeGraph kg = generateCrossDomainKnowledgeGraph();
        
        // Добавляем дополнительные аналогии
        
        // Паттерн "регуляция": биология и финансы
        Entity bioRegulator = new Entity("bio_regulator", "Regulatory Protein",
                Set.of("protein", "biology", "regulation"));
        Entity finRegulator = new Entity("fin_regulator", "Regulatory Agency",
                Set.of("agency", "finance", "regulation"));
        
        kg.addEntity(bioRegulator);
        kg.addEntity(finRegulator);
        
        Relation controls = new Relation("controls", "controls", "general", 0.9);
        kg.addRelation(bioRegulator, controls, 
                kg.getEntities().stream()
                        .filter(e -> e.getId().startsWith("bio_"))
                        .findFirst().orElse(null));
        
        kg.addRelation(finRegulator, controls,
                kg.getEntities().stream()
                        .filter(e -> e.getId().startsWith("fin_"))
                        .findFirst().orElse(null));
        
        return kg;
    }
    
    /**
     * Создает тестовый запрос для поиска аналогий
     */
    public static KnowledgeGraph createQueryPattern() {
        KnowledgeGraph query = new KnowledgeGraph();
        
        // Создаем паттерн "A → нарушает → B → ведет к → C"
        Entity a = new Entity("q_a", "Entity A", Set.of("entity", "query"));
        Entity b = new Entity("q_b", "Entity B", Set.of("mechanism", "query"));
        Entity c = new Entity("q_c", "Entity C", Set.of("outcome", "query"));
        
        query.addEntity(a);
        query.addEntity(b);
        query.addEntity(c);
        
        Relation disrupts = new Relation("q_disrupts", "disrupts", "query", 0.9);
        Relation leadsTo = new Relation("q_leads_to", "leads_to", "query", 0.8);
        
        query.addRelation(a, disrupts, b);
        query.addRelation(b, leadsTo, c);
        
        return query;
    }
}

