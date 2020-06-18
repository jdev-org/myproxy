package fr.landel.myproxy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    public void testInjectNull() {
        String input = "test";
        String expected = "test";
        assertEquals(expected, StringUtils.inject(input));
        assertEquals(expected, StringUtils.inject(input, (Object[]) null));
        assertEquals(expected, StringUtils.inject(input, new Object[0]));
        assertEquals(expected, StringUtils.inject(input, new Object[] {null}));

        input = "test{}";
        expected = "test";
        assertEquals(expected, StringUtils.inject(input));
        assertEquals(expected, StringUtils.inject(input, (Object[]) null));
        assertEquals(expected, StringUtils.inject(input, new Object[0]));
        assertEquals("testnull", StringUtils.inject(input, new Object[] {null}));

        input = "test{0}";
        expected = input;
        assertEquals(expected, StringUtils.inject(input));
        assertEquals(expected, StringUtils.inject(input, (Object[]) null));
        assertEquals(expected, StringUtils.inject(input, new Object[0]));
        assertEquals("testnull", StringUtils.inject(input, new Object[] {null}));

        input = "test{1}";
        expected = input;
        assertEquals(expected, StringUtils.inject(input));
        assertEquals(expected, StringUtils.inject(input, (Object[]) null));
        assertEquals(expected, StringUtils.inject(input, new Object[0]));
        assertEquals("test", StringUtils.inject(input, new Object[] {null}));

        input = null;
        assertNull(StringUtils.inject(input));
        assertNull(StringUtils.inject(input, (Object[]) null));
        assertNull(StringUtils.inject(input, new Object[0]));
        assertNull(StringUtils.inject(input, new Object[] {null}));
    }

    @Test
    public void testInject() {
        String input = "{}{1}{} test{1}{} {0}";
        String expected = "t1 t2 t2 test t2 t1";
        assertEquals(expected, StringUtils.inject(input, "t1", " t2"));

        input = "<{}{1}{} test{1}{} {0}>";
        expected = "<t1 t2 t2 test t2 t1>";
        assertEquals(expected, StringUtils.inject(input, "t1", " t2"));

        input = "{13}";
        expected = "";
        assertEquals(expected, StringUtils.inject(input, "t1", " t2"));

        input = "{123}";
        expected = "";
        assertEquals(expected, StringUtils.inject(input, "t1", " t2"));
    }

    @Test
    public void testInjectEscape() {
        String input = "\\{}";
        String expected = "{}";
        assertEquals(expected, StringUtils.inject(input, "2", "test"));

        input = "\\{";
        expected = "\\{";
        assertEquals(expected, StringUtils.inject(input, "2", "test"));

        input = "\\{test}";
        expected = "{test}";
        assertEquals(expected, StringUtils.inject(input, "2", "test"));

        input = "test\\{}";
        expected = "test{}";
        assertEquals(expected, StringUtils.inject(input, "2", "test"));

        input = "test\\{{}";
        expected = "test{2";
        assertEquals(expected, StringUtils.inject(input, "2", "test"));

        input = "test\\{{}}";
        expected = "test{2}";
        assertEquals(expected, StringUtils.inject(input, "2", "test"));

        input = "{{}}";
        expected = "{test}";
        assertEquals(expected, StringUtils.inject(input, "test", "2"));

        input = "{{1}}";
        expected = "{2}";
        assertEquals(expected, StringUtils.inject(input, "test", "2"));
    }

    @Test
    public void testInjectMap() {
        String input = "{r2}";
        String expected = "{r2}";
        assertEquals(expected, StringUtils.inject(input, "t1", " t2"));

        Map<String, String> map = new HashMap<>();
        map.put("r1", "value1");
        map.put("r2", "value2");
        map.put("r3", null);

        input = "{r2}";
        expected = "value2";
        assertEquals(expected, StringUtils.inject(input, map));

        input = "test{r2}";
        expected = "testvalue2";
        assertEquals(expected, StringUtils.inject(input, map));

        input = "{r2}{3}";
        expected = "value2";
        assertEquals(expected, StringUtils.inject(input, map, "test", "2"));

        input = "{r2}{0}";
        expected = "value2test";
        assertEquals(expected, StringUtils.inject(input, map, "test", "2"));

        input = "\\{r2}{3}";
        expected = "{r2}";
        assertEquals(expected, StringUtils.inject(input, map, "test", "2"));

        map = Collections.emptyMap();
        input = "{r2}";
        expected = "{r2}";
        assertEquals(expected, StringUtils.inject(input, map));
    }

    @Test
    public void parsePositiveIntTest() {
        assertEquals(1234567890, StringUtils.parsePositiveInt("1234567890"));
        assertEquals(-1, StringUtils.parsePositiveInt("12345678900"));
        assertEquals(Integer.MAX_VALUE, StringUtils.parsePositiveInt("2147483647"));
        assertEquals(-1, StringUtils.parsePositiveInt("2147483648"));
        assertEquals(-1, StringUtils.parsePositiveInt("-2"));
        assertEquals(0, StringUtils.parsePositiveInt("0"));
        assertEquals(-1, StringUtils.parsePositiveInt("a0"));
        assertEquals(-1, StringUtils.parsePositiveInt("0a"));
        assertEquals(-1, StringUtils.parsePositiveInt("0a0"));
    }
}
