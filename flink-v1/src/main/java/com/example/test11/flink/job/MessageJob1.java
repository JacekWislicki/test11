package com.example.test11.flink.job;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.connector.pulsar.source.PulsarSource;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.example.test11.commons.utils.Config;
import com.example.test11.flink.function.MessageProcessFunction;
import com.example.test11.model.TestMessage;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageJob1 extends BaseJob {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment environment = prepareEnvironment();
        environment.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);

        PulsarSource<TestMessage> source = createPulsarSource(Config.TOPIC_1_NAME, Config.TOPIC_1_SUB, TestMessage.class);
        new MessageJob1(source).build(environment);
        environment.execute(MessageJob1.class.getSimpleName());
    }

    private final Source<TestMessage, ?, ?> pulsarSource;

    void build(StreamExecutionEnvironment environment) {
        DataStream<TestMessage> pulsarStream =
            environment.fromSource(pulsarSource, WatermarkStrategy.noWatermarks(), "pulsar-source", TypeInformation.of(TestMessage.class))
                .uid("source-id");
        pulsarStream
            .process(new MessageProcessFunction())
            .uid("process-id");
    }
}
