package com.example.test11.pulsar.producer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import com.example.test11.commons.pulsar.producer.AbstractProducer;
import com.example.test11.commons.utils.Config;
import com.example.test11.model.PartInformation;
import com.example.test11.model.TestMessage;
import com.example.test11.model.enums.Status;
import com.example.test11.model.enums.Type;

public class MessageProducer1 extends AbstractProducer<TestMessage> {

    MessageProducer1(String topic) throws PulsarClientException {
        super(topic, Schema.AVRO(TestMessage.class));
    }

    private TestMessage buildTestMessage() {
        TestMessage message = new TestMessage();
        message.setEventIdentifier("eventId");
        List<PartInformation> parts = Arrays.asList(
            new PartInformation("number1", 1, "measure1"),
            new PartInformation("number2", 2, "measure2"));
        message.setPartInformation(parts);
        message.setType(Type.TYPE_A);
        message.setStatus(Status.BAD);
        message.setProperties(Map.of(
            "A", "1",
            "B", "2"));
        return message;
    }

    public static void main(String[] args) throws PulsarClientException {
        try (MessageProducer1 producer = new MessageProducer1(Config.TOPIC_1_NAME);) {
            TestMessage message = producer.buildTestMessage();
            producer.produce(message);
        }
    }
}
