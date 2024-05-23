package com.migratorydata.example;

import com.migratorydata.client.MigratoryDataMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * This class is the main class of the Kafka Streams application that processes the raw data from the input topic
 * and produces compressed MigratoryData records to the output topic.
 */
public final class Main {

    public static final String INPUT_TOPIC = "server-raw";
    public static final String OUTPUT_TOPIC = "server";

    static Properties getStreamsConfig(final String[] args) throws IOException {
        final Properties props = new Properties();
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "compress-demo");
        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9094");
        props.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArraySerde.class);

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    public static void main(final String[] args) throws IOException {
        final Properties props = getStreamsConfig(args);

        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(INPUT_TOPIC).process(new ProcessorSupplier() {
            @Override
            public Processor<String, byte[], String, byte[]> get() {
                return new MigratoryDataKafkaStreamProcessor(true, MigratoryDataMessage.QoS.STANDARD, false);
            }
        }).to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.ByteArray()));

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("compress-demo-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
