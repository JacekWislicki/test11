package com.example.test11.pulsar.producer;

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import com.example.test11.commons.pulsar.producer.AbstractProducer;
import com.example.test11.commons.utils.Config;
import com.example.test11.model.TestMessage;
import com.example.test11.model.enums.Type;

public class MessageProducer2 extends AbstractProducer<TestMessage> {

    MessageProducer2(String topic) throws PulsarClientException {
        super(topic, Schema.AVRO(TestMessage.class));
    }

    private TestMessage buildTestMessage() {
        TestMessage message = new TestMessage();
        message.setEventIdentifier("eventId");
        message.setType(Type.TYPE_A);
        message.setActive(true);
        return message;
    }

    public static void main(String[] args) throws PulsarClientException {
        try (MessageProducer2 producer = new MessageProducer2(Config.TOPIC_1_NAME);) {
            TestMessage message = producer.buildTestMessage();
            producer.produce(message);
        }
    }
}
