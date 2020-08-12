package com.kinetixtt.harper.wordcount;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TranslatorTest {

    private Translator cut = new Translator();

    @Test
    public void test() {
        assertEquals("stub", cut.translate("stub"));
    }
}
