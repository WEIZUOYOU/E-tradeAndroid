package com.example.e_tradeandroid.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * 解析 Integer 字段时兼容布尔值 true/false -> 1/0
 */
public class BooleanIntAdapter extends TypeAdapter<Integer> {

    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }

    @Override
    public Integer read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1 : 0;
        } else if (in.peek() == JsonToken.NUMBER) {
            return in.nextInt();
        } else if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            in.skipValue();
            return null;
        }
    }
}