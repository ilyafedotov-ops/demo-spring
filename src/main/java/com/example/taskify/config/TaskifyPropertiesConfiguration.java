package com.example.taskify.config;

import com.example.taskify.config.properties.TaskifyDataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TaskifyDataSourceProperties.class)
public class TaskifyPropertiesConfiguration {}
