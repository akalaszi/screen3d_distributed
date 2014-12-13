package org.akalaszi;

import com.google.gson.Gson;

public class SerializableMRecordPair {
    SerializableMRecord record1;
    SerializableMRecord record2;

    public SerializableMRecordPair(SerializableMRecord record1, SerializableMRecord record2) {
        this.record1 = record1;
        this.record2 = record2;
    }

    public SerializableMRecord getRecord1() {
        return record1;
    }

    public SerializableMRecord getRecord2() {
        return record2;
    }

    String toJSON() {
        return new Gson().toJson(this);
    }

    static SerializableMRecordPair fromJSON(String json) {
        return new Gson().fromJson(json, SerializableMRecordPair.class);
    }
}
