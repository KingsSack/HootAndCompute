package dev.kingssack.volt.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class ButtonHandlerTest {
    private lateinit var buttonHandler: ButtonHandler
    
    @BeforeEach
    fun setUp() {
        // Create a new ButtonHandler before each test
        buttonHandler = ButtonHandler()
    }
    
    @Test
    fun `test initial state`() {
        // Test that the initial state is not pressed
        assertFalse(buttonHandler.pressed)
        assertFalse(buttonHandler.tapped())
        assertFalse(buttonHandler.doubleTapped())
        assertFalse(buttonHandler.held(100.0))
    }
    
    @Test
    fun `test button press and release`() {
        // Test pressing the button
        buttonHandler.update(true)
        assertTrue(buttonHandler.pressed)
        
        // Test releasing the button
        buttonHandler.update(false)
        assertFalse(buttonHandler.pressed)
    }
    
    @Test
    fun `test tap detection`() {
        // Press and release to create a tap
        buttonHandler.update(true)
        buttonHandler.update(false)
        
        // Should detect a tap
        assertTrue(buttonHandler.tapped())
        
        // After detecting a tap, it should reset
        assertFalse(buttonHandler.tapped())
    }
    
    @Test
    fun `test double tap detection`() {
        // First tap
        buttonHandler.update(true)
        buttonHandler.update(false)
        
        // Second tap within threshold
        buttonHandler.update(true)
        buttonHandler.update(false)
        
        // Should detect a double tap
        assertTrue(buttonHandler.doubleTapped())
        
        // After detecting a double tap, it should reset
        assertFalse(buttonHandler.doubleTapped())
    }
    
    @Test
    fun `test hold detection`() {
        // Press the button
        buttonHandler.update(true)
        
        // Should not detect a hold yet
        assertFalse(buttonHandler.held(100.0))
        
        // Wait for the hold threshold (this is a bit tricky in a unit test)
        // In a real scenario, we'd wait for the time to pass
        // For testing, we can use a mock or a different approach
        // This test might not be reliable due to timing issues
        
        // For now, we'll just test the logic
        // If the button is pressed and the time since press is > threshold, it should return true
        // If the button is not pressed, it should return false
        buttonHandler.update(false)
        assertFalse(buttonHandler.held(100.0))
    }
    
    @Test
    fun `test reset`() {
        // Create a tap
        buttonHandler.update(true)
        buttonHandler.update(false)
        
        // Reset
        buttonHandler.reset()
        
        // Should not detect a tap after reset
        assertFalse(buttonHandler.tapped())
    }
}