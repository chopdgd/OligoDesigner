package edu.chop.dgd.Process;

import edu.chop.dgd.dgdObjects.OligoObject;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by jayaramanp on 10/4/17.
 */
public class OligoSerializer implements Serializer<OligoObject>, Serializable {

 /*   @Override
    public void serialize(DataOutput out, Person value) throws IOException {
        out.writeUTF(value.getName());
        out.writeUTF(value.getCity());
    }

    @Override
    public Person deserialize(DataInput in, int available) throws IOException {
        return new Person(in.readUTF(), in.readUTF());
    }*/

    @Override
    public void serialize(@NotNull DataOutput2 dataOutput2, @NotNull OligoObject oligoObject) throws IOException {
        dataOutput2.writeUTF(oligoObject.getInternalPrimerId());
        dataOutput2.writeUTF(oligoObject.getChr());
        dataOutput2.writeUTF((oligoObject.getAssembly()));
        dataOutput2.writeUTF(oligoObject.getInternalStart());
        dataOutput2.writeInt(oligoObject.getInternalLen());
        dataOutput2.writeUTF(oligoObject.getInternalSeq());
        dataOutput2.writeUTF(oligoObject.getInternalGc());
        dataOutput2.writeUTF(oligoObject.getInternalTm());
        dataOutput2.writeUTF(oligoObject.getInternalAny());
        dataOutput2.writeUTF(oligoObject.getInternal3());
    }

    @Override
    public OligoObject deserialize(@NotNull DataInput2 dataInput2, int i) throws IOException {
        //return null;
        return new OligoObject();
    }
}