package com.transitiontose.wildfire;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

// EXAMPLE UNIT TEST

public class isEvenTest {
    @Test
    public void isEvenIsCorrect() throws Exception {
        assertThat(MainActivity.isEven(2), is(true));
        assertThat(MainActivity.isEven(3), is(false));
        assertThat(MainActivity.isEven(4), is(true));
        assertThat(MainActivity.isEven(5), is(false));
        assertThat(MainActivity.isEven(6), is(true));
        assertThat(MainActivity.isEven(7), is(false));
        assertThat(MainActivity.isEven(8), is(true));
    }


    @Test
    public void isEvenIsCorrect2() throws Exception {
        assertThat(MainActivity.isEven(2), is(true));
        assertThat(MainActivity.isEven(3), is(false));
        assertThat(MainActivity.isEven(4), is(true));
        assertThat(MainActivity.isEven(5), is(false));
        assertThat(MainActivity.isEven(6), is(true));
        assertThat(MainActivity.isEven(7), is(false));
        assertThat(MainActivity.isEven(8), is(true));
    }
}
