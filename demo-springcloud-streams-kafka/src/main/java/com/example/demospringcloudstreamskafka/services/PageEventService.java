package com.example.demospringcloudstreamskafka.services;

import com.example.demospringcloudstreamskafka.entities.PageEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service

public class PageEventService {
    @Bean
    public Consumer<PageEvent> pageEventConsumer() {

        return (input) -> {
            System.out.println("*****************");
            System.out.println(input.toString());
            System.out.println("*****************");

        };
    }

    @Bean
    public Supplier<PageEvent> pageEventSupplier() {

        return () -> new PageEvent(
                Math.random() > 0.5 ? "P1" : "P2",
                Math.random() > 0.5 ? "U1" : "U2",
                new Date(),
                new Random().nextInt(9000));
    }

    @Bean
    public Function<PageEvent, PageEvent> pageEventPageEventFunction() {

        return (input) -> {
            input.setName("L:" + input.getName().length());
            input.setUser("UUUUU");
            return input;
        };

    }

    @Bean
    public Function<KStream<String, PageEvent>, KStream<String, Long>> KStreamFunction() {
        return (input) -> {
            return input
                    .filter((k, v) -> v.getDuration() > 100)
                    .map((k, v) -> new KeyValue<>(v.getName(), 0L))
                    .peek((k, v) -> System.out.println(k + "=>" + v))
                    .groupBy((k, v) -> k, Grouped.with(Serdes.String(), Serdes.Long()))
                    .windowedBy(TimeWindows.of(Duration.ofDays(5000)))
                    .count(Materialized.as("page-count"))
                    .toStream()
                    .map((k, v) -> new KeyValue<>("=>" + k.window().startTime() + k.window().endTime() + k.key(), v));

        };
    }

}
