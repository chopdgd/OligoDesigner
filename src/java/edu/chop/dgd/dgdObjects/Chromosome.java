package edu.chop.dgd.dgdObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jayaramanp on 1/27/14.
 */
public class Chromosome {

    int speciesTypeKey=1;

    public List<String> getChromosomes(int speciesKey) throws Exception{
        if (speciesKey==0){
            speciesKey=speciesTypeKey;
        }

        List<String> chrNames = new ArrayList<String>();
        if(speciesKey==1){
            for(int i=1; i<25; i++){
                if(i==23){
                    chrNames.add("ChrX");
                }else if(i==24){
                    chrNames.add("ChrY");
                }else{
                    chrNames.add("Chr"+i);
                }
            }
        }

        return chrNames;

    }

}
