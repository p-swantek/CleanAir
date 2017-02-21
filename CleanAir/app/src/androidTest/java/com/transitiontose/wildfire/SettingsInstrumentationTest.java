package com.transitiontose.wildfire;

import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;

import android.app.Notification;
import android.support.v4.app.FragmentActivity;
import android.test.InstrumentationTestCase;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.LargeTest;
import android.support.v4.app.FragmentTransaction;
import android.database.Cursor;
import android.test.ActivityInstrumentationTestCase2;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import com.transitiontose.wildfire.SettingsActivity.NotificationPreferenceFragment;
import android.content.Context;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

// Test device settings and shared preferences here

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsInstrumentationTest extends InstrumentationTestCase {

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(SettingsActivity.class);

    @Test
    public void checkNotifications(){
        NotificationPreferenceFragment myFragment = startMyFragment();
        //onView((withId(R.id.notiffy))).perform(click());
        onView(withId(R.id.notifCheckBox)).perform(click());SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        boolean userWantsNotifications = sp.getBoolean("notifications", true);
        assertTrue(userWantsNotifications == false);

        onView(withText("Check this box")).perform(click());
        sp = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        userWantsNotifications = sp.getBoolean("notifications", true);
        assertTrue(userWantsNotifications == true);
    }

    private NotificationPreferenceFragment startMyFragment() {
        FragmentActivity activity = (FragmentActivity) mActivityRule.getActivity();
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        NotificationPreferenceFragment myFragment = new NotificationPreferenceFragment();
        transaction.commit();
        return myFragment;
    }
}