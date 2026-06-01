package tn.iteam.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableFeignClients(basePackages = "tn.iteam.backend.client")
@EnableKafka
@EnableRetry
@EntityScan(basePackages = {
        "tn.iteam.backend.entity"
})
public class HrmServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmServiceApplication.class, args);
    }
}
