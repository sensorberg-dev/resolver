package com.sensorberg.front.resolve.service
import com.fasterxml.jackson.databind.ObjectMapper
import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
/**
 * Created by ole on 13/10/16.
 */
class AzureEventHubServiceTest extends GroovyTestCase {
    private AzureEventHubService service;

    void setUp() {
        super.setUp();

        service = new AzureEventHubService();
    }

    void testEncodeMessageToJson() {
        LayoutCtx expectedLayout = new LayoutCtx();

        // handle rare case of 000 milliseconds:
        if (expectedLayout.eventDate.getTime() % 1000L == 0L) {
            expectedLayout.eventDate = new Date(expectedLayout.eventDate.getTime() - 23L);
        }

        String json = service.encodeMessageToJson(expectedLayout);
        System.out.println(json);
        ObjectMapper jsonMapper = new ObjectMapper();
        LayoutCtx actualLayout = jsonMapper.readValue(json, LayoutCtx.class);

        assertEquals(expectedLayout.eventDate, actualLayout.eventDate);
    }
}
