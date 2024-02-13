package com.example.test11.flink.job;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.pulsar.common.config.PulsarOptions;
import org.apache.flink.connector.pulsar.sink.PulsarSink;
import org.apache.flink.connector.pulsar.sink.PulsarSinkOptions;
import org.apache.flink.connector.pulsar.source.PulsarSource;
import org.apache.flink.connector.pulsar.source.PulsarSourceOptions;
import org.apache.flink.connector.pulsar.source.enumerator.cursor.StartCursor;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.pulsar.client.api.Schema;

import com.example.test11.commons.utils.Config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("java:S119")
public abstract class BaseJob implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MEMORY = "128MB";

    protected static StreamExecutionEnvironment prepareEnvironment() {
        StreamExecutionEnvironment environment;
        String environmentName = Config.getEnvironment();
        if (Config.VP_LOCAL_ENV.equals(environmentName)) {
            var configuration = new Configuration();
            configuration.set(TaskManagerOptions.NETWORK_MEMORY_FRACTION, 0.2f);
            configuration.set(TaskManagerOptions.NETWORK_MEMORY_MIN, MemorySize.parse(DEFAULT_MEMORY));
            configuration.set(TaskManagerOptions.NETWORK_MEMORY_MAX, MemorySize.parse(DEFAULT_MEMORY));
            environment = StreamExecutionEnvironment.createLocalEnvironment(configuration);
        } else {
            environment = StreamExecutionEnvironment.getExecutionEnvironment();
        }
        environment.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        environment.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        return environment;
    }

    public static <OUT> Sink<OUT> createPulsarSink(String topic, Class<OUT> outClass) {
        return PulsarSink.builder()
            .setProducerName(BaseJob.class.getName())
            .setServiceUrl(Config.getServiceUrl())
            .setTopics(Arrays.asList(topic))
            .setConfig(PulsarOptions.PULSAR_ENABLE_TRANSACTION, true)
            .setConfig(PulsarSinkOptions.PULSAR_WRITE_DELIVERY_GUARANTEE, DeliveryGuarantee.EXACTLY_ONCE)
            .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
            .enableSchemaEvolution()
            .setSerializationSchema(Schema.AVRO(outClass), outClass)
            .build();
    }

    public static <IN> PulsarSource<IN> createPulsarSource(String topic, String subscription, Class<IN> inClass) {
        return PulsarSource.builder()
            .setServiceUrl(Config.getServiceUrl())
            .setStartCursor(StartCursor.earliest())
            .setTopics(topic)
            .setSubscriptionName(subscription)
            .setConfig(PulsarOptions.PULSAR_ENABLE_TRANSACTION, true)
            .setConfig(PulsarSourceOptions.PULSAR_ACK_RECEIPT_ENABLED, true)
            .setConfig(PulsarSourceOptions.PULSAR_MAX_FETCH_RECORDS, 1)
            .setConfig(PulsarSourceOptions.PULSAR_ENABLE_AUTO_ACKNOWLEDGE_MESSAGE, false)
            .enableSchemaEvolution()
            .setDeserializationSchema(Schema.AVRO(inClass), inClass)
            .build();
    }
}
