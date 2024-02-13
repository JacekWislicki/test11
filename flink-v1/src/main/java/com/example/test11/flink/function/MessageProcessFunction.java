package com.example.test11.flink.function;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import com.example.test11.model.TestMessage;

public class MessageProcessFunction extends ProcessFunction<TestMessage, TestMessage> {

    private static final long serialVersionUID = 1L;

    @Override
    public void processElement(TestMessage value, ProcessFunction<TestMessage, TestMessage>.Context ctx, Collector<TestMessage> out)
        throws Exception {
        System.out.println("************ IN: " + value);
    }
}
