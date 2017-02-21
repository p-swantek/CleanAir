package com.transitiontose.wildfire;

import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.InstrumentationRegistry;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.ServiceTestRule;
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

// Test CheckNearbyAir service stuff here

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ServiceInstrumentationTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void checkService(){
        // Create the service Intent.
        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), CheckNearbyAirService.class);


    }
}