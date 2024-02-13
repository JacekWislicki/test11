#!/bin/bash

cd /pulsar/bin

## NAMESPACES
./pulsar-admin namespaces create public/test11
./pulsar-admin namespaces set-is-allow-auto-update-schema --disable public/test11
./pulsar-admin namespaces set-schema-compatibility-strategy --compatibility ALWAYS_COMPATIBLE public/test11
./pulsar-admin namespaces set-schema-validation-enforce --enable public/test11

## TOPICS
#./pulsar-admin topics delete-partitioned-topic persistent://public/test11/topic1

./pulsar-admin topics create-partitioned-topic --partitions 1 persistent://public/test11/topic1
./pulsar-admin topics create-subscription persistent://public/test11/topic1 -s topic1-flink

## SCHEMAS
#./pulsar-admin schemas delete persistent://public/test11/topic1

## SCHEMAS v1
./pulsar-admin schemas upload \
    --filename /pulsar/schemas/test11/v1/TestMessage.json \
    persistent://public/test11/topic1

## SCHEMAS v2
./pulsar-admin schemas upload \
    --filename /pulsar/schemas/test11/v2/TestMessage.json \
    persistent://public/test11/topic1

## SCHEMAS v3
./pulsar-admin schemas upload \
    --filename /pulsar/schemas/test11/v3/TestMessage.json \
    persistent://public/test11/topic1
