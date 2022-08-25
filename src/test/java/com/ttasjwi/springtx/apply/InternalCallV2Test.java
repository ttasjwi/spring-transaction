package com.ttasjwi.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired
    CallService callService;

    @Autowired
    InternalService internalService;


    @Test
    void printProxy() {
        log.info("callService class= {}", callService.getClass());
        assertThat(AopUtils.isAopProxy(callService)).isFalse();

        log.info("internalService class = {}", internalService.getClass());
        assertThat(AopUtils.isAopProxy(internalService)).isTrue();
    }

    @Test
    void internalCall(){
        internalService.internal();
    }

    @Test
    public void externalCallV2() {
        callService.external();
    }


    @TestConfiguration
    static class InternalCallV2TestConfig {
        @Bean
        CallService callService() {
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("call external");
            printTxInfo(); // this.printTxInfo();
            internalService.internal(); // this.internal();
        }
        public void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active : {}", txActive);
        }
    }

    @Slf4j
    static class InternalService {

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        public void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active : {}", txActive);
        }
    }
}
