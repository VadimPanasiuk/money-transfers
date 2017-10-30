package com.exchange.money.repos.inmemory;

import com.exchange.money.repos.AccountRepo;
import com.exchange.money.repos.TransactionRepo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class InMemoryReposConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public AccountRepo accountRepo() {
        return new InMemoryAccountRepo();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public TransactionRepo transactionRepo() {
        return new InMemoryTransactionRepo();
    }

}
