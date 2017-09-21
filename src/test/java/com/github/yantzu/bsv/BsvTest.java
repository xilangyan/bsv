package com.github.yantzu.bsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.yantzu.bsv.BsvContext;
import com.github.yantzu.bsv.BsvContextBuilder;
import com.github.yantzu.bsv.BsvDeserializer;
import com.github.yantzu.bsv.BsvException;


public class BsvTest {
    
    private static BsvContext context;

    @BeforeClass
    public static void beforeClass() throws IOException {
        context = new BsvContextBuilder()
            .yamlSchema("classpath:com/github/yantzu/bsv/schema03.0.0.yaml")
            .yamlSchema("classpath:com/github/yantzu/bsv/schema03.0.1.yaml")
            .yamlSchema("classpath:com/github/yantzu/bsv/schema03.1.0.yaml")
            .transcoding('\n', (char) 0x00)
            .build();
    }
    
    @Test
    public void testDeserializeSingleLine() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/sample_single.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        Schema030x data = (Schema030x) deserializer.next();
        assertEquals("ABC", data.getS());
        assertEquals("", data.getN());
        assertTrue(data.isB());
        assertEquals(12345678, data.getI());
        assertEquals(Arrays.asList("1.2", "2.4", "3.4"), data.getArray());
        Map<String, String> matching = new HashMap<String, String>();
        matching.put("a", "4");
        matching.put("b", "d");
        assertEquals(matching, data.getMap());
        assertNull(deserializer.next());
    }
    
    @Test
    public void testDeserializeBatchLine() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/sample_batch.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        deserializer.next(); //second line
        Schema030x data = (Schema030x) deserializer.next(); //third line
        assertEquals("ABC", data.getS());
        assertEquals("", data.getN());
        assertFalse(Boolean.FALSE);
        assertEquals(87654321, data.getI());
        assertEquals(Arrays.asList("1.2", "2.4", "3.4"), data.getArray());
        Map<String, String> matching = new HashMap<String, String>();
        matching.put("a", "4");
        matching.put("b", "d");
        assertEquals(matching, data.getMap());
        assertNull(deserializer.next());
    }
    
    @Test
    public void testDeserializeChinese() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/sample_chinese.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        deserializer.next(); //second line
        Schema030x data = (Schema030x) deserializer.next(); //third line
        assertEquals("A币C", data.getS());
        Map<String, String> matching = new HashMap<String, String>();
        matching.put("a", "四");
        matching.put("b", "地");
        assertEquals(matching, data.getMap());
        assertNull(deserializer.next());
    }
    
    @Test
    public void testDeserializeEmoji() throws IOException, BsvException {
        Scanner emojiData = new Scanner(this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/testEmoji.data"), "UTF-8");
        String s = emojiData.nextLine();
        String a = emojiData.nextLine();
        String b = emojiData.nextLine();
        
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/sample_emoji.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        deserializer.next(); //second line
        Schema030x data = (Schema030x) deserializer.next(); //third line
        assertEquals(s, data.getS());
        Map<String, String> matching = new HashMap<String, String>();
        matching.put("a", a);
        matching.put("b", b);
        assertEquals(matching, data.getMap());
        assertNull(deserializer.next());
        
        emojiData.close();
    }
    
    @Test
    public void testDeserializeVariants() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/sample_variants.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        Schema030x data2 = (Schema030x) deserializer.next(); //second line
        Schema030x data3 = (Schema030x) deserializer.next(); //third line
        assertEquals("", data2.getN());
        assertNull(data3.getN());
    }
    
    @Test(expected = BsvException.class)
    public void testDeserializeInvalid() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/sample_invalid.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        deserializer.next(); //second line, should throw exception
    }
    
    @Test
    public void testDeserializeEmptymap() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/sample_emptymap.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        Schema030x data = (Schema030x) deserializer.next(); //first line
        assertEquals("ABC", data.getS());
        assertTrue(data.getMap().isEmpty());
    }
    
    @Test
    public void testDeserializeTranscoding() throws IOException, BsvException {
    	InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/yantzu/bsv/sample_transcode.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        Schema030x data = (Schema030x) deserializer.next(); //first line
        assertEquals("AB\nCD", data.getS());
        assertTrue(data.getMap().isEmpty());
    }
    
	@Test
	public void testSerdeString() throws IOException, BsvException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsvSerializer serializer = context.createSerializer(baos, "03", '0');
		Schema030x data = new Schema030x();
		data.setVersion("03.0.0");
		data.setS("test_string");
		
		serializer.next(data);
		serializer.close();
		
		BsvDeserializer deserializer = context.createDeserializer(new ByteArrayInputStream(baos.toByteArray()));
		assertEquals( ((Schema030x) deserializer.next()).getS(), data.getS());
		
		assertNull(deserializer.next());
	}
	
	@Test
	public void testSerdeTranscoding() throws IOException, BsvException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsvSerializer serializer = context.createSerializer(baos, "03", '0');
		Schema030x data = new Schema030x();
		data.setVersion("03.0.0");
		data.setS("AB\nCD");
		
		serializer.next(data);
		serializer.close();
		
		BsvDeserializer deserializer = context.createDeserializer(new ByteArrayInputStream(baos.toByteArray()));
		assertEquals( ((Schema030x) deserializer.next()).getS(), data.getS());
		
		assertNull(deserializer.next());
	}
	
	
	@Test
	public void testSerdeMap() throws IOException, BsvException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsvSerializer serializer = context.createSerializer(baos, "03", '0');
		Schema030x data = new Schema030x();
		data.setVersion("03.0.0");
		Map<String, String> map = new HashMap<String, String>();
		map.put("a", "四");
		map.put("b", "地");
		data.setMap(map);
		serializer.next(data);
		serializer.close();



		BsvDeserializer deserializer = context.createDeserializer(new ByteArrayInputStream(baos.toByteArray()));
		Schema030x dataR = (Schema030x) deserializer.next();
		
		assertEquals(2, dataR.getMap().size());
		assertEquals("四", dataR.getMap().get("a"));
		assertEquals("地", dataR.getMap().get("b"));
		
		assertNull(deserializer.next());
	}
	
	@Test
	public void testSerdeList() throws IOException, BsvException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsvSerializer serializer = context.createSerializer(baos, "03", '0');
		Schema030x data = new Schema030x();
		data.setVersion("03.0.0");
		List<String> list = new ArrayList<String>();
		list.add("😀");
		list.add("😄");
		data.setArray(list);
		serializer.next(data);
		serializer.close();


		BsvDeserializer deserializer = context.createDeserializer(new ByteArrayInputStream(baos.toByteArray()));
		Schema030x dataR = (Schema030x) deserializer.next();
		
		assertEquals(2, dataR.getArray().size());
		assertEquals("😀", dataR.getArray().get(0));
		assertEquals("😄", dataR.getArray().get(1));
		
		assertNull(deserializer.next());
	}
	
	@Test
	public void testSerdeMultiline() throws IOException, BsvException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsvSerializer serializer = context.createSerializer(baos, "03", '0');
		Schema030x data = new Schema030x();
		data.setVersion("03.0.0");
		data.setS("test_string");
		
		serializer.next(data);
		serializer.next(data);
		serializer.close();
		
		BsvDeserializer deserializer = context.createDeserializer(new ByteArrayInputStream(baos.toByteArray()));
		assertEquals( ((Schema030x) deserializer.next()).getS(), data.getS());
		assertEquals( ((Schema030x) deserializer.next()).getS(), data.getS());
	}
}
