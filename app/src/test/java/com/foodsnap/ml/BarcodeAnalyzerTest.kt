package com.foodsnap.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for BarcodeAnalyzer result handling.
 *
 * Note: Full ML Kit integration tests require instrumentation tests.
 * These tests verify the result data structures and utility functions.
 */
class BarcodeAnalyzerTest {

    @Test
    fun `BarcodeResult contains correct barcode value`() {
        // Arrange & Act
        val result = AnalyzerResult.BarcodeResult(
            barcode = "0123456789012",
            format = "EAN_13"
        )

        // Assert
        assertEquals("0123456789012", result.barcode)
        assertEquals("EAN_13", result.format)
    }

    @Test
    fun `ImageLabelResult contains detected labels`() {
        // Arrange
        val labels = listOf(
            DetectedLabel("Apple", 0.95f),
            DetectedLabel("Fruit", 0.85f)
        )

        // Act
        val result = AnalyzerResult.ImageLabelResult(labels)

        // Assert
        assertEquals(2, result.labels.size)
        assertEquals("Apple", result.labels[0].text)
        assertEquals(0.95f, result.labels[0].confidence, 0.01f)
    }

    @Test
    fun `DishRecognitionResult has correct dish name and related labels`() {
        // Arrange & Act
        val relatedLabels = listOf(
            DetectedLabel("pasta", 0.88f),
            DetectedLabel("italian", 0.75f),
            DetectedLabel("noodles", 0.70f)
        )
        val result = AnalyzerResult.DishRecognitionResult(
            dishName = "Pasta",
            confidence = 0.92f,
            relatedLabels = relatedLabels
        )

        // Assert
        assertEquals("Pasta", result.dishName)
        assertEquals(3, result.relatedLabels.size)
        assertTrue(result.relatedLabels.any { it.text == "pasta" })
    }

    @Test
    fun `Error result contains message`() {
        // Arrange & Act
        val result = AnalyzerResult.Error("Camera initialization failed")

        // Assert
        assertEquals("Camera initialization failed", result.message)
    }

    @Test
    fun `Empty result is singleton`() {
        // Arrange & Act
        val result1 = AnalyzerResult.Empty
        val result2 = AnalyzerResult.Empty

        // Assert
        assertTrue(result1 === result2)
    }

    @Test
    fun `DetectedLabel filters by confidence threshold`() {
        // Arrange
        val labels = listOf(
            DetectedLabel("High Confidence", 0.9f),
            DetectedLabel("Medium Confidence", 0.6f),
            DetectedLabel("Low Confidence", 0.3f)
        )

        // Act
        val filtered = labels.filter { it.confidence >= 0.5f }

        // Assert
        assertEquals(2, filtered.size)
        assertTrue(filtered.none { it.text == "Low Confidence" })
    }
}
