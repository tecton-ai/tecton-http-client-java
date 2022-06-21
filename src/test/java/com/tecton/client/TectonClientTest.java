package com.tecton.client;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class TectonClientTest {
    String url;
    String apiKey;

    @Before
    public void setup() {
        this.url = "https://test-url.com";
        this.apiKey = "12345";
    }

    @Test
    public void testEmptyUrl() {
        try {
            TectonClient tectonClient = new TectonClient("", apiKey);
            fail();
        } catch (TectonClientException e) {
            Assert.assertEquals(TectonErrorMessage.INVALID_URL, e.getMessage());
        }
    }

    @Test
    public void testEmptyKey() {
        try {
            TectonClient tectonClient = new TectonClient(url, "");
            fail();
        } catch (TectonClientException e) {
            Assert.assertEquals(TectonErrorMessage.INVALID_KEY, e.getMessage());
        }
    }

    @Test
    public void testNullKey() {
        try {
            TectonClient tectonClient = new TectonClient(url, null);
            fail();
        } catch (TectonClientException e) {
            Assert.assertEquals(TectonErrorMessage.INVALID_KEY, e.getMessage());
        }
    }

}
