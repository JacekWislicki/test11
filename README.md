# AVRO SCHEMA EVOLUTION ISSUES IN PULSAR-FLINK CONNECTOR
This project in an MRE for some issues spotted when using Avro schema evolution in the Pulsar-Flink connector.
Library versions:
* Pulsar 3.0.1
* Flink 1.17.2
* Pulsar-Flink connector 4.1.0-1.17

# BUILD PROJECT TO GENERATE SCHEMAS AND MODELS
This is a simple Maven project, build it with:
```sh
mvn clean package
```
# Configure Pulsar

## Copy schemas
Copy generated Avro schemas to your Pulsar's schemas directory (the paths may be adjusted, here they match the script below):

* models-v1/target/schemas/avro/test/v1/TestMessage.json -> /pulsar/schemas/test11/v1/
* models-v2/target/schemas/avro/test/v2/TestMessage.json -> /pulsar/schemas/test11/v2/
* models-v3/target/schemas/avro/test/v3/TestMessage.json -> /pulsar/schemas/test11/v3/

## Run script
Execute the following script:
```sh
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
```

# USAGE
The tested Flink job is *com.example.test11.flink.job.MessageJob1* (in module flink-v1) expecting on the message schema v1. There are subtle differences in the schemas v2 and v3 comparing to v1:
* v1 -> v2: 
    * removed fields: status, properties and partInformation
    * added field: active
* v1 -> v3:
    * removed: enum field Type.TYPE_A
    * added: enum field Type.TYPE_C

Each schema version has its own producer:
* v1: *com.example.test11.pulsar.producer.MessageProducer1* in module pulsar-v1
* v2: *com.example.test11.pulsar.producer.MessageProducer2* in module pulsar-v2
* v3: *com.example.test11.pulsar.producer.MessageProducer3* in module pulsar-v3

Run *MessageJob1* and a selected producer to observe the behaviour.

# BEHAVIOUR
## VALID: expected schema v1 vs actual schema v1 (just a working reference)
Producer to use: *MessageProducer1*
Sent message: 
```
{"eventIdentifier": "eventId", "type": "TYPE_A", "status": "BAD", "properties": {"A": "1", "B": "2"}, "partInformation": [{"genuinePartNumber": "number1", "genuinePartQuantity": 1, "genuinePartMeasure": "measure1"}, {"genuinePartNumber": "number2", "genuinePartQuantity": 2, "genuinePartMeasure": "measure2"}]}
```
Expected decoded message:
```
{"eventIdentifier": "eventId", "type": "TYPE_A", "status": "BAD", "properties": {"A": "1", "B": "2"}, "partInformation": [{"genuinePartNumber": "number1", "genuinePartQuantity": 1, "genuinePartMeasure": "measure1"}, {"genuinePartNumber": "number2", "genuinePartQuantity": 2, "genuinePartMeasure": "measure2"}]}
```
Actual result:
```
{"eventIdentifier": "eventId", "type": "TYPE_A", "status": "BAD", "properties": {"A": "1", "B": "2"}, "partInformation": [{"genuinePartNumber": "number1", "genuinePartQuantity": 1, "genuinePartMeasure": "measure1"}, {"genuinePartNumber": "number2", "genuinePartQuantity": 2, "genuinePartMeasure": "measure2"}]}
```
## INVALID: expected schema v1 vs actual schema v2
Producer to use: *MessageProducer2*
Sent message: 
```
{"eventIdentifier": "eventId", "type": "TYPE_A", "active": true}
```
Expected decoded message (reasoning: fields *status*, *properties* and *partInformation* absent in v2 while *active* absent in v1):
```
{"eventIdentifier": "eventId", "type": "TYPE_A", "status": null, "properties": null, "partInformation": null}
```
Actual result (exception thrown):
```
java.lang.IndexOutOfBoundsException: Index -1 out of bounds for length 3
    at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
    at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
    at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:266)
    at java.base/java.util.Objects.checkIndex(Objects.java:359)
    at java.base/java.util.ArrayList.get(ArrayList.java:427)
    at org.apache.avro.generic.GenericDatumReader.readEnum(GenericDatumReader.java:268)
    at org.apache.avro.generic.GenericDatumReader.readWithoutConversion(GenericDatumReader.java:182)
    at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:161)
    at org.apache.avro.generic.GenericDatumReader.readWithoutConversion(GenericDatumReader.java:188)
    at org.apache.avro.specific.SpecificDatumReader.readField(SpecificDatumReader.java:136)
    at org.apache.avro.reflect.ReflectDatumReader.readField(ReflectDatumReader.java:298)
    at org.apache.avro.generic.GenericDatumReader.readRecord(GenericDatumReader.java:248)
    at org.apache.avro.specific.SpecificDatumReader.readRecord(SpecificDatumReader.java:123)
    at org.apache.avro.generic.GenericDatumReader.readWithoutConversion(GenericDatumReader.java:180)
    at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:161)
    at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:154)
    at org.apache.pulsar.client.impl.schema.reader.AvroReader.read(AvroReader.java:78)
    at org.apache.pulsar.client.api.schema.SchemaReader.read(SchemaReader.java:40)
    at org.apache.pulsar.client.impl.schema.reader.AbstractMultiVersionReader.read(AbstractMultiVersionReader.java:61)
    at org.apache.pulsar.client.api.schema.SchemaReader.read(SchemaReader.java:40)
    at org.apache.pulsar.client.impl.schema.AbstractStructSchema.decode(AbstractStructSchema.java:66)
    at org.apache.flink.connector.pulsar.source.reader.deserializer.PulsarSchemaWrapper.deserialize(PulsarSchemaWrapper.java:66)
    at org.apache.flink.connector.pulsar.source.reader.PulsarRecordEmitter.emitRecord(PulsarRecordEmitter.java:53)
    at org.apache.flink.connector.pulsar.source.reader.PulsarRecordEmitter.emitRecord(PulsarRecordEmitter.java:33)
    at org.apache.flink.connector.base.source.reader.SourceReaderBase.pollNext(SourceReaderBase.java:144)
    at org.apache.flink.connector.pulsar.source.reader.PulsarSourceReader.pollNext(PulsarSourceReader.java:130)
    at org.apache.flink.connector.base.source.reader.SourceReaderBase.pollNext(SourceReaderBase.java:157)
    at org.apache.flink.connector.pulsar.source.reader.PulsarSourceReader.pollNext(PulsarSourceReader.java:130)
    at org.apache.flink.streaming.api.operators.SourceOperator.emitNext(SourceOperator.java:419)
    at org.apache.flink.streaming.runtime.io.StreamTaskSourceInput.emitNext(StreamTaskSourceInput.java:68)
    at org.apache.flink.streaming.runtime.io.StreamOneInputProcessor.processInput(StreamOneInputProcessor.java:65)
    at org.apache.flink.streaming.runtime.tasks.StreamTask.processInput(StreamTask.java:550)
    at org.apache.flink.streaming.runtime.tasks.mailbox.MailboxProcessor.runMailboxLoop(MailboxProcessor.java:231)
    at org.apache.flink.streaming.runtime.tasks.StreamTask.runMailboxLoop(StreamTask.java:839)
    at org.apache.flink.streaming.runtime.tasks.StreamTask.invoke(StreamTask.java:788)
    at org.apache.flink.runtime.taskmanager.Task.runWithSystemExitMonitoring(Task.java:952)
    at org.apache.flink.runtime.taskmanager.Task.restoreAndInvoke(Task.java:931)
    at org.apache.flink.runtime.taskmanager.Task.doRun(Task.java:745)
    at org.apache.flink.runtime.taskmanager.Task.run(Task.java:562)
    at java.base/java.lang.Thread.run(Thread.java:833)
```
## INVALID: expected schema v1 vs actual schema v3
Producer to use: *MessageProducer3*
Sent message: 
```
{"eventIdentifier": "eventId", "type": "TYPE_C", "status": "BAD", "properties": {"A": "1", "B": "2"}, "partInformation": [{"genuinePartNumber": "number1", "genuinePartQuantity": 1, "genuinePartMeasure": "measure1"}, {"genuinePartNumber": "number2", "genuinePartQuantity": 2, "genuinePartMeasure": "measure2"}]}
```
Expected decoded message (*Type.TYPE_C* absent in v1, hence fallback to default *UNKNOWN* should be applied instead):
```
{"eventIdentifier": "eventId", "type": "UNKNOWN", "status": "BAD", "properties": {"A": "1", "B": "2"}, "partInformation": [{"genuinePartNumber": "number1", "genuinePartQuantity": 1, "genuinePartMeasure": "measure1"}, {"genuinePartNumber": "number2", "genuinePartQuantity": 2, "genuinePartMeasure": "measure2"}]}
```
Actual result (incorrect enum value used - *TYPE_B* instead of *UNKNOWN*):
```
{"eventIdentifier": "eventId", "type": "TYPE_B", "status": "BAD", "properties": {"A": "1", "B": "2"}, "partInformation": [{"genuinePartNumber": "number1", "genuinePartQuantity": 1, "genuinePartMeasure": "measure1"}, {"genuinePartNumber": "number2", "genuinePartQuantity": 2, "genuinePartMeasure": "measure2"}]}
```
