package com.example.currencies.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

@EnableAsync
@Configuration
public class AppConfig {

    @Bean
    public RestClient restClientCBR(@Value("${cbr.base.url}") String url) {
        return RestClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .messageConverters(messageConverters -> {
                    messageConverters.add(xmlConverter());
                })
                .build();
    }

    @Bean
    public RestClient restClientKudaGo(@Value("${kudaGo.base.url}") String url) {
        return RestClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
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

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "kudaGoRateLimiterSemaphore")
    public Semaphore kudaGoRateLimiterSemaphore(@Value("${kudaGo.maxConcurrentRequests}") int maxConcurrentRequests) {
        return new Semaphore(maxConcurrentRequests);
    }
}
