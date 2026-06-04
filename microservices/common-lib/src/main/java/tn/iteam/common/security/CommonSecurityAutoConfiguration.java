package tn.iteam.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(ApiCorsProperties.class)
public class CommonSecurityAutoConfiguration {}
