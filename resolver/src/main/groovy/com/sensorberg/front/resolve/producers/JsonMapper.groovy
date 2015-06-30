package com.sensorberg.front.resolve.producers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.text.DateFormat
import java.text.SimpleDateFormat

@Service
class JsonMapper {

    final ObjectMapper mapper = new ObjectMapper()

    @PostConstruct
    def init() {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    @Bean
    ObjectMapper get() {
        return mapper
    }
}
