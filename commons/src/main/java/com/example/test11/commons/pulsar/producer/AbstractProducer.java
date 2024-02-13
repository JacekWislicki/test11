package com.example.test11.commons.pulsar.producer;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import com.example.test11.commons.utils.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractProducer<T> implements AutoCloseable {

    private final PulsarClient client;
    private final Producer<T> producer;
    private final Schema<T> schema;

    protected AbstractProducer(String pulsarServiceUrl, String topic, Schema<T> schema) throws PulsarClientException {
        this.client = PulsarClient.builder().serviceUrl(pulsarServiceUrl).build();
        this.producer = client.newProducer(schema).topic(topic).create();
        this.schema = schema;
    }

    protected AbstractProducer(String topic, Schema<T> schema) throws PulsarClientException {
        this(Config.getServiceUrl(), topic, schema);
    }

    public void produce(T message) throws PulsarClientException {
        producer
            .newMessage(schema)
            .value(message)
            .send();
        log.info("Sent: " + message);
    }

    public void produce(T message, String key) throws PulsarClientException {
        producer
            .newMessage(schema)
            .key(key)
            .value(message)
            .send();
        log.info("Sent: " + message);
    }

    @Override
    public void close() throws PulsarClientException {
        producer.close();
        client.close();
    }
}
