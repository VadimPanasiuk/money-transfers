package com.exchange.money.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(basePackages = { "com.exchange.money.controllers" })
public class SpringRootConfig {

}
