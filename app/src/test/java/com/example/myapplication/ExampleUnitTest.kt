package com.example.myapplication

import com.example.myapplication.ui.isPasswordValid
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for sign-up password validation.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPasswordValidation() {
        assertFalse("Empty password should be invalid", isPasswordValid(""))
        assertFalse("Short password without numbers should be invalid", isPasswordValid("abc"))
        assertFalse("Short password with numbers should be invalid", isPasswordValid("pass12"))
        assertFalse("8+ character password without numbers should be invalid", isPasswordValid("password"))
        assertTrue("8 character password with numbers should be valid", isPasswordValid("pass1234"))
        assertTrue("Long password with numbers should be valid", isPasswordValid("securePassword1"))
    }
}