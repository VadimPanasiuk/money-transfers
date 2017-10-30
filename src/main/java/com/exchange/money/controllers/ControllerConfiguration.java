package com.exchange.money.controllers;

import com.exchange.money.repos.inmemory.InMemoryReposConfiguration;
import com.exchange.money.services.SpringServiceConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ InMemoryReposConfiguration.class, SpringServiceConfiguration.class})
public class ControllerConfiguration {
}
