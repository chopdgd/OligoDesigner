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
            dataOutput2.writeFloat(oligoObject.getHairpinValue());
            dataOutput2.writeFloat(oligoObject.getHomodimerValue());

        }


    }

    @Override
    public OligoObject deserialize(@NotNull DataInput2 dataInput2, int i) throws IOException {
        //String chr, String assembly, String internalPrimerid, String internalSeq, String internalStart,
        // int internalLen, String internal3, String internalAny, String internalGc, Float hairpinValue, Float homodimerValue)
        //return null;
        return new OligoObject(dataInput2.readUTF(), dataInput2.readUTF(), dataInput2.readUTF(), dataInput2.readUTF(),
                dataInput2.readUTF(),dataInput2.readUnsignedShort(), dataInput2.readUTF(), dataInput2.readUTF(), dataInput2.readUTF(),
                dataInput2.readUTF(), dataInput2.readFloat(), dataInput2.readFloat());
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