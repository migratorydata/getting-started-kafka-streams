package com.migratorydata.example;

import com.migratorydata.client.MigratoryDataKafkaUtils;
import com.migratorydata.client.MigratoryDataMessage;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

/**
 * This class is a Kafka Streams Processor that adds MigratoryData headers to the Kafka records and compresses the value
 * of the record if the compression flag is set to true.
 */
public class MigratoryDataKafkaStreamProcessor implements Processor<String, byte[], String, byte[]> {

    private final boolean compression;
    private final boolean retained;
    private final boolean benchmarkTimestamp;
    private final MigratoryDataMessage.QoS qos;

    private ProcessorContext<String, byte[]> context;

    /**
     * Constructor
     *
     * @param compression        a boolean flag indicating whether the value of the record should be compressed
     * @param qos                the QoS level of the record
     * @param retained           a boolean flag indicating whether the record should be retained
     */
    public MigratoryDataKafkaStreamProcessor(boolean compression, MigratoryDataMessage.QoS qos, boolean retained) {
        this(compression, qos, retained, false);
    }

    /**
     * Constructor
     *
     * @param compression        a boolean flag indicating whether the value of the record should be compressed
     * @param qos                the QoS level of the record
     * @param retained           a boolean flag indicating whether the record should be retained
     * @param benchmarkTimestamp a boolean flag indicating whether the record should have a benchmark timestamp
     */
    public MigratoryDataKafkaStreamProcessor(boolean compression, MigratoryDataMessage.QoS qos, boolean retained, boolean benchmarkTimestamp) {
        this.compression = compression;
        this.qos = qos;
        this.retained = retained;
        this.benchmarkTimestamp = benchmarkTimestamp;
    }

    @Override
    public void init(final ProcessorContext<String, byte[]> context) {
        this.context = context;
    }

    @Override
    public void process(Record<String, byte[]> record) {
        MigratoryDataKafkaUtils.addRecordMetadata(record.headers(), compression, qos, retained, benchmarkTimestamp);
        if (compression) {
            byte[] compressedValue = MigratoryDataKafkaUtils.compressValue(record.value());
            context.forward(record.withValue(compressedValue));
        } else {
            context.forward(record);
        }
    }
}
