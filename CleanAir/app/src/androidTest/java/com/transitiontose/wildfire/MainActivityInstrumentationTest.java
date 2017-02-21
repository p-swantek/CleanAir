package com.transitiontose.wildfire;

import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

// EXAMPLE INTEGRATION TEST
// Test device location stuff here and anything else in main activity

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityInstrumentationTest extends InstrumentationTestCase {

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void checkStuff(){
        onView(withText("LOG IN")).perform(click());
        onView(withId(R.id.passwordTextView)).check(matches(withText("Password:")));

        Activity activity = mActivityRule.getActivity();
        SharedPreferences prefs = activity.getSharedPreferences("current.user", Context.MODE_PRIVATE);
        boolean userWantsNotifications = prefs.getBoolean("notifications", true);
        assertTrue(userWantsNotifications == true);
        boolean userIsCollector = prefs.getBoolean("sensorUploadBox", false);
        assertTrue(userIsCollector == false);
    }
}