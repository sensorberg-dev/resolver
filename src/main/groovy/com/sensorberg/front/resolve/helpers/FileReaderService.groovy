package com.sensorberg.front.resolve.helpers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext
import org.springframework.stereotype.Service

/**
 * helper class to read files from archive
 */
@Service
class FileReaderService {

    @Autowired
    private AnnotationConfigEmbeddedWebApplicationContext ctx

    def String contentAsString(def fileName) {
        ctx.getResource("classpath:${fileName}").inputStream.getText()
    }
}
