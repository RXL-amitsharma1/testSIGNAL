package com.rxlogix.crypto

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.encrypt.TextEncryptor

@Configuration
class CustomEncryptionBootstrapConfiguration {

    @Configuration
    protected static class RxCodecConfiguration {
        @Bean
        @ConditionalOnClass(CustomEncrytor.class)
        public TextEncryptor rxEncryptor() {
            return new CustomEncrytor()
        }
    }
}
