package com.example.currencies.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;

@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient(@Value("${cbr.base.url}") String url) {
        return RestClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .messageConverters(messageConverters -> {
                    messageConverters.add(xmlConverter());
                })
                .build();
    }

    @Bean
    public MappingJackson2XmlHttpMessageConverter xmlConverter() {
        var bigDecimalModule = new SimpleModule("BigDecimalDeserialization", Version.unknownVersion())
                .addDeserializer(BigDecimal.class, new JsonDeserializer<>() {
                    @Override
                    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        return new BigDecimal(p.getValueAsString().trim().replace(",", "."));
                    }
                });

        var mapper = XmlMapper.xmlBuilder()
                .addModule(new JavaTimeModule())
                .addModule(bigDecimalModule)
                .build();

        return new MappingJackson2XmlHttpMessageConverter(mapper);
    }
}
