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

        if (oligoObject.getInternalPrimerId() == null) {
            System.err.println("Custom Oligo serializer called with 'null'");
        }else{
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


    }

    @Override
    public OligoObject deserialize(@NotNull DataInput2 dataInput2, int i) throws IOException {
        //return null;
        /*OligoObject o = new OligoObject();
        o.setInternalPrimerId(dataInput2.readUTF());
        o.setChr(dataInput2.readUTF());
        o.setAssembly(dataInput2.readUTF());
        o.setInternalStart(dataInput2.readUTF());
        o.setInternalLen(Integer.parseInt(dataInput2.readUTF()));
        o.setInternalSeq(dataInput2.readUTF());
        o.setInternalGc(dataInput2.readUTF());
        o.setInternalTm(dataInput2.readUTF());
        o.setInternalAny(dataInput2.readUTF());
        o.setInternal3(dataInput2.readUTF());
        return o;*/
        return new OligoObject();
    }

    @Override
    public int compare(OligoObject o1, OligoObject o2) {

        int compareVal0 = o1.getInternalPrimerId().split("_",-1)[0].compareTo(o2.getInternalPrimerId().split("_",-1)[0]);

        if(compareVal0 ==0){
            int compareVal1 = Integer.valueOf(o1.getInternalPrimerId().split("_",-1)[1]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_",-1)[1]));

            if(compareVal1 ==0){

                return (Integer.valueOf(o1.getInternalPrimerId().split("_",-1)[2].split("O",-1)[1]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_",-1)[2].split("O",-1)[1])));

            }else{
                return compareVal1;
            }

        }else{
            return compareVal0;
        }
    }
}