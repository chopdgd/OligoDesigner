package edu.chop.dgd.dgdObjects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by jayaramanp on 9/5/14.
 */
public class MfoldHairpin {

    public List<OligoObject> getDeltaGValuesForHairpin(List<OligoObject> oligoObjectsFromPrimer3, String fileName, String mfoldOpDir, String dataDir) throws Exception {

        File mfoldFile = new File(dataDir+mfoldOpDir+"/"+fileName+".ct");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mfoldFile)));

        try{
            String line;
            while((line=reader.readLine()) != null){
                //line = reader.readLine();
                if(line.contains("dG")){
                    String lineArr[] = line.split("\t", -1);
                    String oligoHeader = lineArr[2];
                    for(OligoObject o:oligoObjectsFromPrimer3){
                        if(o.getInternalPrimerId().equals(oligoHeader)){
                            String hairpinValue = lineArr[1].split(" = ", -1)[1];
                            o.setHairpinValue(Float.parseFloat(hairpinValue));
                        }
                    }
                }
            }

        }finally {

            reader.close();

        }


        return oligoObjectsFromPrimer3;
    }
}
