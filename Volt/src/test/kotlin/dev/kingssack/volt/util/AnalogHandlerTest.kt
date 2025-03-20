package dev.kingssack.volt.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import kotlin.math.pow

class AnalogHandlerTest {
    private lateinit var analogHandler: AnalogHandler
    private val defaultDeadzone = 0.1
    private val defaultInputExp = 2.0
    
    @BeforeEach
    fun setUp() {
        // Create a new AnalogHandler before each test with default values
        analogHandler = AnalogHandler(defaultDeadzone, defaultInputExp)
    }
    
    @Test
    fun `test initial state`() {
        // Test that the initial value is 0.0
        assertEquals(0.0, analogHandler.value, 0.001)
    }
    
    @Test
    fun `test input below deadzone`() {
        // Test that inputs below the deadzone result in 0.0
        analogHandler.update(0.05)
        assertEquals(0.0, analogHandler.value, 0.001)
        
        analogHandler.update(-0.05)
        assertEquals(0.0, analogHandler.value, 0.001)
        
        analogHandler.update(defaultDeadzone - 0.01)
        assertEquals(0.0, analogHandler.value, 0.001)
    }
    
    @Test
    fun `test input at deadzone`() {
        // Test that input exactly at the deadzone results in 0.0
        analogHandler.update(defaultDeadzone)
        assertEquals(0.0, analogHandler.value, 0.001)
    }
    
    @Test
    fun `test input above deadzone`() {
        // Test that inputs above the deadzone are processed correctly
        // For input = 0.5, with deadzone = 0.1 and inputExp = 2.0:
        // normalizedInput = (0.5 - 0.1) / (1 - 0.1) = 0.4 / 0.9 = 0.444...
        // value = 0.444...^2 * 1 = 0.198
        analogHandler.update(0.5)
        
        // Calculate expected value
        val normalizedInput = (0.5 - defaultDeadzone) / (1 - defaultDeadzone)
        val expected = normalizedInput.pow(defaultInputExp)
        
        assertEquals(expected, analogHandler.value, 0.001)
    }
    
    @Test
    fun `test negative input above deadzone`() {
        // Test that negative inputs above the deadzone are processed correctly
        // For input = -0.5, with deadzone = 0.1 and inputExp = 2.0:
        // normalizedInput = (-0.5 - 0.1) / (1 - 0.1) = -0.6 / 0.9 = -0.666...
        // value = 0.666...^2 * -1 = -0.444
        analogHandler.update(-0.5)
        
        // Calculate expected value
        val normalizedInput = (Math.abs(-0.5) - defaultDeadzone) / (1 - defaultDeadzone)
        val expected = -1 * normalizedInput.pow(defaultInputExp)
        
        assertEquals(expected, analogHandler.value, 0.001)
    }
    
    @Test
    fun `test custom deadzone and inputExp`() {
        // Create a new AnalogHandler with custom values
        val customDeadzone = 0.2
        val customInputExp = 3.0
        val customAnalogHandler = AnalogHandler(customDeadzone, customInputExp)
        
        // Test that inputs are processed correctly with custom values
        customAnalogHandler.update(0.5)
        
        // Calculate expected value
        val normalizedInput = (0.5 - customDeadzone) / (1 - customDeadzone)
        val expected = normalizedInput.pow(customInputExp)
        
        assertEquals(expected, customAnalogHandler.value, 0.001)
    }
    
    @Test
    fun `test input at extremes`() {
        // Test input at 1.0
        analogHandler.update(1.0)
        assertEquals(1.0, analogHandler.value, 0.001)
        
        // Test input at -1.0
        analogHandler.update(-1.0)
        assertEquals(-1.0, analogHandler.value, 0.001)
    }
}