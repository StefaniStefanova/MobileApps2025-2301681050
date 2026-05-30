package com.example.pinmemory

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUITest {

    init {
        FirebaseAuth.getInstance().signOut()
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun loginScreen_isDisplayed() {
        Thread.sleep(1000)
        onView(withId(R.id.etEmail))
            .check(matches(isDisplayed()))
    }

    @Test
    fun loginScreen_emptyFields_showsError() {
        Thread.sleep(1000)
        onView(withId(R.id.btnLogin))
            .perform(click())
        onView(withId(R.id.etEmail))
            .check(matches(isDisplayed()))
    }
}