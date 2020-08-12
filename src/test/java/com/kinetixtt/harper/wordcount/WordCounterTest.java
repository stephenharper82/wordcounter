package com.kinetixtt.harper.wordcount;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WordCounterTest {

    @Mock
    private Translator translator;

    @InjectMocks
    private WordCounter cut;

    @Before
    public void setup() {
        // by default return same word that is to be translated
        when(translator.translate(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                return invocationOnMock.getArgument(0);
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullValue() {
        cut.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullInVargs() {
        cut.add("a", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNothing() {
        cut.add();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNonAlphaChars() {
        cut.add("a", "a;'");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tesAddWordContainingNumber() {
        cut.add("a", "a12345");
    }

    @Test
    public void testHappyPath() {
        when(translator.translate(eq("flor"))).thenReturn("flower");
        when(translator.translate(eq("blume"))).thenReturn("flower");
        cut.add("hello", "flower", "Flor", "blume");
        cut.add("FLOWER");

        assertEquals(1, cut.countOfWord("hello"));
        assertEquals(4, cut.countOfWord("flower"));
    }

    @Test(expected = RuntimeException.class)
    public void testAddTranslatorThrowsException() {
        when(translator.translate(eq("hello"))).thenThrow(RuntimeException.class);
        cut.add("hello");
    }

    @Test
    public void testWordCount() {
        assertEquals(0, cut.countOfWord("nothing"));
        cut.add("NOTHING");
        assertEquals(1, cut.countOfWord("Nothing"));
        assertEquals(1, cut.countOfWord("nothing"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWordCountOfNull() {
        cut.countOfWord(null);
    }

    @Test(expected = RuntimeException.class)
    public void testWordCountThrowsException() {
        when(translator.translate("nothing")).thenThrow(RuntimeException.class);
        cut.countOfWord("nothing");
    }
}
