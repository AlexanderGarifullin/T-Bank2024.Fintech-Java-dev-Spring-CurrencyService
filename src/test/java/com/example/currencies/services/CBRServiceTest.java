package com.example.currencies.services;

import com.example.currencies.CurrenciesApplication;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchNullPointerException;


@ActiveProfiles("test")
@SpringBootTest(classes = CurrenciesApplication.class)
@TestPropertySource(properties = {
        "spring.cache.type=none"
})
class CBRServiceTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    private CBRService cbrService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("cbr.base.url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
    }

    @Test
    void getValCurs_Success() {
        wireMockServer.stubFor(get(urlEqualTo("/XML_daily.asp"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBodyFile("XML_daily.xml")));

        var r = cbrService.getValCurs();
        assertThat(r)
                .isPresent()
                .hasValueSatisfying(valCurs -> assertThat(valCurs.getValutes())
                        .isNotEmpty()
                        .hasSize(44));

    }

    @Test
    void getValutagetValCurs_Success() {
        wireMockServer.stubFor(get(urlEqualTo("/XML_valFull.asp"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBodyFile("XML_valFull.xml")));

        var r = cbrService.getValuta();
        assertThat(r)
                .isPresent()
                .hasValueSatisfying(valuta -> assertThat(valuta.getItems())
                        .isNotEmpty()
                        .hasSize(71));
    }


}