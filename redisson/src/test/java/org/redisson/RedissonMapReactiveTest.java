package org.redisson;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.redisson.api.RMapReactive;

public class RedissonMapReactiveTest extends BaseReactiveTest {

    public static class SimpleKey implements Serializable {

        private String key;

        public SimpleKey() {
        }

        public SimpleKey(String field) {
            this.key = field;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "key: " + key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleKey other = (SimpleKey) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }

    }

    public static class SimpleValue implements Serializable {

        private String value;

        public SimpleValue() {
        }

        public SimpleValue(String field) {
            this.value = field;
        }

        public void setValue(String field) {
            this.value = field;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "value: " + value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleValue other = (SimpleValue) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

    }

    @Test
    public void testAddAndGet() throws InterruptedException {
        RMapReactive<Integer, Integer> map = redisson.getMap("getAll");
        sync(map.put(1, 100));

        Integer res = sync(map.addAndGet(1, 12));
        Assert.assertEquals(112, (int)res);
        res = sync(map.get(1));
        Assert.assertEquals(112, (int)res);

        RMapReactive<Integer, Double> map2 = redisson.getMap("getAll2");
        sync(map2.put(1, new Double(100.2)));

        Double res2 = sync(map2.addAndGet(1, new Double(12.1)));
        Assert.assertTrue(new Double(112.3).compareTo(res2) == 0);
        res2 = sync(map2.get(1));
        Assert.assertTrue(new Double(112.3).compareTo(res2) == 0);

        RMapReactive<String, Integer> mapStr = redisson.getMap("mapStr");
        assertThat(sync(mapStr.put("1", 100))).isNull();

        assertThat(sync(mapStr.addAndGet("1", 12))).isEqualTo(112);
        assertThat(sync(mapStr.get("1"))).isEqualTo(112);
    }

    @Test
    public void testGetAll() {
        RMapReactive<Integer, Integer> map = redisson.getMap("getAll");
        sync(map.put(1, 100));
        sync(map.put(2, 200));
        sync(map.put(3, 300));
        sync(map.put(4, 400));

        Map<Integer, Integer> filtered = sync(map.getAll(new HashSet<Integer>(Arrays.asList(2, 3, 5))));

        Map<Integer, Integer> expectedMap = new HashMap<Integer, Integer>();
        expectedMap.put(2, 200);
        expectedMap.put(3, 300);
        Assert.assertEquals(expectedMap, filtered);
    }

    @Test
    public void testGetAllWithStringKeys() {
        RMapReactive<String, Integer> map = redisson.getMap("getAllStrings");
        sync(map.put("A", 100));
        sync(map.put("B", 200));
        sync(map.put("C", 300));
        sync(map.put("D", 400));

        Map<String, Integer> filtered = sync(map.getAll(new HashSet<String>(Arrays.asList("B", "C", "E"))));

        Map<String, Integer> expectedMap = new HashMap<String, Integer>();
        expectedMap.put("B", 200);
        expectedMap.put("C", 300);
        Assert.assertEquals(expectedMap, filtered);
    }

    @Test
    public void testInteger() {
        RMapReactive<Integer, Integer> map = redisson.getMap("test_int");
        sync(map.put(1, 2));
        sync(map.put(3, 4));

        Assert.assertEquals(2, sync(map.size()).intValue());

        Integer val = sync(map.get(1));
        Assert.assertEquals(2, val.intValue());
        Integer val2 = sync(map.get(3));
        Assert.assertEquals(4, val2.intValue());
    }

    @Test
    public void testLong() {
        RMapReactive<Long, Long> map = redisson.getMap("test_long");
        sync(map.put(1L, 2L));
        sync(map.put(3L, 4L));

        Assert.assertEquals(2, sync(map.size()).intValue());

        Long val = sync(map.get(1L));
        Assert.assertEquals(2L, val.longValue());
        Long val2 = sync(map.get(3L));
        Assert.assertEquals(4L, val2.longValue());
    }

    @Test
    public void testNull() {
        RMapReactive<Integer, String> map = redisson.getMap("simple12");
        sync(map.put(1, null));
        sync(map.put(2, null));
        sync(map.put(3, "43"));

        Assert.assertEquals(3, sync(map.size()).intValue());

        String val = sync(map.get(2));
        Assert.assertNull(val);
        String val2 = sync(map.get(1));
        Assert.assertNull(val2);
        String val3 = sync(map.get(3));
        Assert.assertEquals("43", val3);
    }

    @Test
    public void testSimpleTypes() {
        RMapReactive<Integer, String> map = redisson.getMap("simple12");
        sync(map.put(1, "12"));
        sync(map.put(2, "33"));
        sync(map.put(3, "43"));

        String val = sync(map.get(2));
        Assert.assertEquals("33", val);
    }

    @Test
    public void testRemove() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));
        sync(map.put(new SimpleKey("33"), new SimpleValue("44")));
        sync(map.put(new SimpleKey("5"), new SimpleValue("6")));

        sync(map.remove(new SimpleKey("33")));
        sync(map.remove(new SimpleKey("5")));

        Assert.assertEquals(1, sync(map.size()).intValue());
    }

    @Test
    public void testEquals() {
        RMapReactive<String, String> map = redisson.getMap("simple");
        sync(map.put("1", "7"));
        sync(map.put("2", "4"));
        sync(map.put("3", "5"));

        Map<String, String> testMap = new HashMap<String, String>();
        testMap.put("1", "7");
        testMap.put("2", "4");
        testMap.put("3", "5");

        Assert.assertEquals(map, testMap);
        Assert.assertEquals(map.hashCode(), testMap.hashCode());
    }


    @Test
    public void testPutAll() {
        RMapReactive<Integer, String> map = redisson.getMap("simple");
        sync(map.put(1, "1"));
        sync(map.put(2, "2"));
        sync(map.put(3, "3"));

        Map<Integer, String> joinMap = new HashMap<Integer, String>();
        joinMap.put(4, "4");
        joinMap.put(5, "5");
        joinMap.put(6, "6");
        sync(map.putAll(joinMap));

        assertThat(toIterable(map.keyIterator())).contains(1, 2, 3, 4, 5, 6);
    }

    @Test
    public void testContainsValue() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));
        sync(map.put(new SimpleKey("33"), new SimpleValue("44")));
        sync(map.put(new SimpleKey("5"), new SimpleValue("6")));

        Assert.assertTrue(sync(map.containsValue(new SimpleValue("2"))));
        Assert.assertFalse(sync(map.containsValue(new SimpleValue("441"))));
        Assert.assertFalse(sync(map.containsValue(new SimpleKey("5"))));
    }

    @Test
    public void testContainsKey() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));
        sync(map.put(new SimpleKey("33"), new SimpleValue("44")));
        sync(map.put(new SimpleKey("5"), new SimpleValue("6")));

        Assert.assertTrue(sync(map.containsKey(new SimpleKey("33"))));
        Assert.assertFalse(sync(map.containsKey(new SimpleKey("34"))));
    }

    @Test
    public void testRemoveValue() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));

        long size = sync(map.remove(new SimpleKey("1"), new SimpleValue("2")));
        Assert.assertEquals(1, size);

        SimpleValue val1 = sync(map.get(new SimpleKey("1")));
        Assert.assertNull(val1);

        Assert.assertEquals(0, sync(map.size()).intValue());
    }

    @Test
    public void testRemoveValueFail() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));

        long size = sync(map.remove(new SimpleKey("2"), new SimpleValue("1")));
        Assert.assertEquals(0, size);

        long size2 = sync(map.remove(new SimpleKey("1"), new SimpleValue("3")));
        Assert.assertEquals(0, size2);

        SimpleValue val1 = sync(map.get(new SimpleKey("1")));
        Assert.assertEquals("2", val1.getValue());
    }


    @Test
    public void testReplaceOldValueFail() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));

        boolean res = sync(map.replace(new SimpleKey("1"), new SimpleValue("43"), new SimpleValue("31")));
        Assert.assertFalse(res);

        SimpleValue val1 = sync(map.get(new SimpleKey("1")));
        Assert.assertEquals("2", val1.getValue());
    }

    @Test
    public void testReplaceOldValueSuccess() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));

        boolean res = sync(map.replace(new SimpleKey("1"), new SimpleValue("2"), new SimpleValue("3")));
        Assert.assertTrue(res);

        boolean res1 = sync(map.replace(new SimpleKey("1"), new SimpleValue("2"), new SimpleValue("3")));
        Assert.assertFalse(res1);

        SimpleValue val1 = sync(map.get(new SimpleKey("1")));
        Assert.assertEquals("3", val1.getValue());
    }

    @Test
    public void testReplaceValue() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));

        SimpleValue res = sync(map.replace(new SimpleKey("1"), new SimpleValue("3")));
        Assert.assertEquals("2", res.getValue());

        SimpleValue val1 = sync(map.get(new SimpleKey("1")));
        Assert.assertEquals("3", val1.getValue());
    }


    @Test
    public void testReplace() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));
        sync(map.put(new SimpleKey("33"), new SimpleValue("44")));
        sync(map.put(new SimpleKey("5"), new SimpleValue("6")));

        SimpleValue val1 = sync(map.get(new SimpleKey("33")));
        Assert.assertEquals("44", val1.getValue());

        sync(map.put(new SimpleKey("33"), new SimpleValue("abc")));
        SimpleValue val2 = sync(map.get(new SimpleKey("33")));
        Assert.assertEquals("abc", val2.getValue());
    }

    @Test
    public void testPutGet() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));
        sync(map.put(new SimpleKey("33"), new SimpleValue("44")));
        sync(map.put(new SimpleKey("5"), new SimpleValue("6")));

        SimpleValue val1 = sync(map.get(new SimpleKey("33")));
        Assert.assertEquals("44", val1.getValue());

        SimpleValue val2 = sync(map.get(new SimpleKey("5")));
        Assert.assertEquals("6", val2.getValue());
    }

    @Test
    public void testPutIfAbsent() throws Exception {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");
        SimpleKey key = new SimpleKey("1");
        SimpleValue value = new SimpleValue("2");
        sync(map.put(key, value));
        Assert.assertEquals(value, sync(map.putIfAbsent(key, new SimpleValue("3"))));
        Assert.assertEquals(value, sync(map.get(key)));

        SimpleKey key1 = new SimpleKey("2");
        SimpleValue value1 = new SimpleValue("4");
        Assert.assertNull(sync(map.putIfAbsent(key1, value1)));
        Assert.assertEquals(value1, sync(map.get(key1)));

    }

    @Test
    public void testSize() {
        RMapReactive<SimpleKey, SimpleValue> map = redisson.getMap("simple");

        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));
        sync(map.put(new SimpleKey("3"), new SimpleValue("4")));
        sync(map.put(new SimpleKey("5"), new SimpleValue("6")));
        Assert.assertEquals(3, sync(map.size()).intValue());

        sync(map.put(new SimpleKey("1"), new SimpleValue("2")));
        sync(map.put(new SimpleKey("3"), new SimpleValue("4")));
        Assert.assertEquals(3, sync(map.size()).intValue());

        sync(map.put(new SimpleKey("1"), new SimpleValue("21")));
        sync(map.put(new SimpleKey("3"), new SimpleValue("41")));
        Assert.assertEquals(3, sync(map.size()).intValue());

        sync(map.put(new SimpleKey("51"), new SimpleValue("6")));
        Assert.assertEquals(4, sync(map.size()).intValue());

        sync(map.remove(new SimpleKey("3")));
        Assert.assertEquals(3, sync(map.size()).intValue());
    }

    @Test
    public void testEmptyRemove() {
        RMapReactive<Integer, Integer> map = redisson.getMap("simple");
        Assert.assertEquals(0, sync(map.remove(1, 3)).intValue());
        sync(map.put(4, 5));
        Assert.assertEquals(1, sync(map.remove(4, 5)).intValue());
    }

    @Test
    public void testFastRemoveAsync() throws InterruptedException, ExecutionException {
        RMapReactive<Integer, Integer> map = redisson.getMap("simple");
        sync(map.put(1, 3));
        sync(map.put(3, 5));
        sync(map.put(4, 6));
        sync(map.put(7, 8));

        Assert.assertEquals((Long) 3L, sync(map.fastRemove(1, 3, 7)));
        Assert.assertEquals(1, sync(map.size()).intValue());
    }

    @Test
    public void testKeyIterator() {
        RMapReactive<Integer, Integer> map = redisson.getMap("simple");
        sync(map.put(1, 0));
        sync(map.put(3, 5));
        sync(map.put(4, 6));
        sync(map.put(7, 8));

        List<Integer> keys = new ArrayList<Integer>(Arrays.asList(1, 3, 4, 7));
        for (Iterator<Integer> iterator = toIterator(map.keyIterator()); iterator.hasNext();) {
            Integer value = iterator.next();
            if (!keys.remove(value)) {
                Assert.fail();
            }
        }

        Assert.assertEquals(0, keys.size());
    }

    @Test
    public void testValueIterator() {
        RMapReactive<Integer, Integer> map = redisson.getMap("simple");
        sync(map.put(1, 0));
        sync(map.put(3, 5));
        sync(map.put(4, 6));
        sync(map.put(7, 8));

        List<Integer> values = new ArrayList<Integer>(Arrays.asList(0, 5, 6, 8));
        for (Iterator<Integer> iterator = toIterator(map.valueIterator()); iterator.hasNext();) {
            Integer value = iterator.next();
            if (!values.remove(value)) {
                Assert.fail();
            }
        }

        Assert.assertEquals(0, values.size());
    }

    @Test
    public void testFastPut() throws Exception {
        RMapReactive<Integer, Integer> map = redisson.getMap("simple");
        Assert.assertTrue(sync(map.fastPut(1, 2)));
        Assert.assertFalse(sync(map.fastPut(1, 3)));
        Assert.assertEquals(1, sync(map.size()).intValue());
    }

    @Test
    public void testFastRemoveEmpty() throws Exception {
        RMapReactive<Integer, Integer> map = redisson.getMap("simple");
        sync(map.put(1, 3));
        Assert.assertEquals(0, sync(map.fastRemove()).intValue());
        Assert.assertEquals(1, sync(map.size()).intValue());
    }

    public static class SimpleObjectWithoutDefaultConstructor {

        private String testField;

        SimpleObjectWithoutDefaultConstructor(String testField) {
            this.testField = testField;
        }

        public String getTestField() {
            return testField;
        }

        public void setTestField(String testField) {
            this.testField = testField;
        }
    }
}
