package com.sagam.indexing;

import java.util.Arrays;

/**
 * Сигнатура для LSH индексации
 */
public class Signature {
    private final int[] hashes;
    
    public Signature(int size) {
        this.hashes = new int[size];
    }
    
    public void set(int index, int hash) {
        if (index >= 0 && index < hashes.length) {
            hashes[index] = hash;
        }
    }
    
    public int get(int index) {
        return hashes[index];
    }
    
    public int size() {
        return hashes.length;
    }
    
    /**
     * Вычисляет расстояние Хэмминга между сигнатурами
     */
    public int hammingDistance(Signature other) {
        if (this.hashes.length != other.hashes.length) {
            throw new IllegalArgumentException("Signatures must have the same size");
        }
        
        int distance = 0;
        for (int i = 0; i < hashes.length; i++) {
            if (this.hashes[i] != other.hashes[i]) {
                distance++;
            }
        }
        return distance;
    }
    
    /**
     * Вычисляет схожесть сигнатур (1 - normalized hamming distance)
     */
    public double similarity(Signature other) {
        if (this.hashes.length != other.hashes.length) {
            return 0.0;
        }
        int distance = hammingDistance(other);
        return 1.0 - ((double) distance / hashes.length);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return Arrays.equals(hashes, signature.hashes);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(hashes);
    }
    
    @Override
    public String toString() {
        return "Signature{" + Arrays.toString(hashes) + "}";
    }
}

