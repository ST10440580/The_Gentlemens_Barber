package com.example.thegentlemensbar

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(WelcomeActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testWelcomeScreenComponentsVisible() {
        onView(withText("Let's Get Started")).check(matches(isDisplayed()))
        onView(withId(R.id.registerBtn)).check(matches(isDisplayed()))
        onView(withId(R.id.loginBtn)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigateToLogin() {
        onView(withId(R.id.loginBtn)).perform(click())
        intended(hasComponent(Login::class.java.name))
    }

    @Test
    fun testNavigateToRegister() {
        onView(withId(R.id.registerBtn)).perform(click())
        intended(hasComponent(Register::class.java.name))
    }
}
