package com.peluqueria.estructura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class MongoConfig {

    /**
     * Configuración para validación de documentos MongoDB
     */
    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(
            LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }

    /**
     * Configuración de conversiones personalizadas para tipos de fecha/hora de Java
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new LocalDateToDateConverter(),
                new DateToLocalDateConverter(),
                new LocalTimeToDateConverter(),
                new DateToLocalTimeConverter(),
                new LocalDateTimeToDateConverter(),
                new DateToLocalDateTimeConverter()
        ));
    }

    // Conversores para LocalDate <-> Date
    static class LocalDateToDateConverter implements Converter<LocalDate, Date> {
        @Override
        public Date convert(LocalDate source) {
            return source == null ? null : java.sql.Date.valueOf(source);
        }
    }

    static class DateToLocalDateConverter implements Converter<Date, LocalDate> {
        @Override
        public LocalDate convert(Date source) {
            return source == null ? null : new java.sql.Date(source.getTime()).toLocalDate();
        }
    }

    // Conversores para LocalTime <-> Date
    static class LocalTimeToDateConverter implements Converter<LocalTime, Date> {
        @Override
        public Date convert(LocalTime source) {
            return source == null ? null : java.sql.Time.valueOf(source);
        }
    }

    static class DateToLocalTimeConverter implements Converter<Date, LocalTime> {
        @Override
        public LocalTime convert(Date source) {
            return source == null ? null : new java.sql.Time(source.getTime()).toLocalTime();
        }
    }

    // Conversores para LocalDateTime <-> Date
    static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        @Override
        public Date convert(LocalDateTime source) {
            return source == null ? null : java.sql.Timestamp.valueOf(source);
        }
    }

    static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        @Override
        public LocalDateTime convert(Date source) {
            return source == null ? null : new java.sql.Timestamp(source.getTime()).toLocalDateTime();
        }
    }
}