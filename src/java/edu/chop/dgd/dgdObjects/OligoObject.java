package edu.chop.dgd.dgdObjects;

import java.io.*;
import java.util.*;

/**
 * Created by jayaramanp on 9/26/14.
 */

public class OligoObject implements Serializable{


    String internalPrimerId;
    String internalSeq;
    int internalLen;
    String internalStart;
    String internalTm;
    String internalGc;
    String internalAny;
    String internal3;
    String chr;
    String assembly;
    List<BlatPsl> internalPrimerBlatList;
    float hairpinValue;
    float homodimerValue;
    float hetdimerValue;
   // LinkedHashMap<String, Float> heterodimerValues;


    public OligoObject(String chr, String assembly, String internalPrimerid, String internalSeq, String internalStart, int internalLen,
                       String internal3, String internalTm, String internalAny, String internalGc, Float hairpinValue, Float homodimerValue){
        super();
        this.chr = chr;
        this.assembly = assembly;
        this.internalPrimerId = internalPrimerid;
        this.internalSeq = internalSeq;
        this.internalLen = internalLen;
        this.internalStart = internalStart;
        this.internal3 = internal3;
        this.internalTm = internalTm;
        this.internalAny = internalAny;
        this.internalGc = internalGc;
        this.hairpinValue = hairpinValue;
        this.homodimerValue = homodimerValue;
        //this.internalPrimerBlatList = internalBlatList;

    }


    public int compare(OligoObject o) {

        //return (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1])));
        int compChr = (Integer.valueOf(o.getChr().split("chr", -1)[1]).compareTo(Integer.valueOf(this.getChr().split("chr", -1)[1])));

        if(compChr==0){
            int compSeqObjStart = (Integer.valueOf(o.getInternalPrimerId().split("_", -1)[0].split(":",-1)[1]).compareTo(Integer.valueOf(this.getInternalPrimerId().split("_", -1)[0].split(":",-1)[1])));

            if(compSeqObjStart == 0){
                int compSeqEnd = (Integer.valueOf(o.getInternalPrimerId().split("_", -1)[0].split(":",-1)[2]).compareTo(Integer.valueOf(this.getInternalPrimerId().split("_", -1)[0].split(":",-1)[2])));

                if(compSeqEnd == 0){
                    int compSubsect = (Integer.valueOf(o.getInternalPrimerId().split("_",-1)[1]).compareTo(Integer.valueOf(this.getInternalPrimerId().split("_",-1)[1])));

                    if(compSubsect ==0){
                        return (Integer.valueOf(o.getInternalPrimerId().split("_",-1)[2].split("O",-1)[1]).compareTo(Integer.valueOf(this.getInternalPrimerId().split("_",-1)[2].split("O",-1)[1])));

                    }else{
                        return compSubsect;
                    }
                }else{
                    return compSeqEnd;
                }
            }else {
                return compSeqObjStart;
            }
        }else{
            return compChr;
        }

    }






    public float getHairpinValue() {
        return hairpinValue;
    }

    public void setHairpinValue(float hairpinValue) {
        this.hairpinValue = hairpinValue;
    }

    public float getHomodimerValue() {
        return homodimerValue;
    }

    public void setHomodimerValue(float homodimerValue) {
        this.homodimerValue = homodimerValue;
    }

    /*public LinkedHashMap<String, Float> getHeterodimerValues() {
        return heterodimerValues;
    }

    public void setHeterodimerValues(LinkedHashMap<String, Float> heterodimerValues) {
        this.heterodimerValues = heterodimerValues;
    }*/

    public float getHetdimerValue() {
        return hetdimerValue;
    }

    public void setHetdimerValue(float hetdimerValue) {
        this.hetdimerValue = hetdimerValue;
    }

    public String getInternalPrimerId() {
        return internalPrimerId;
    }

    public void setInternalPrimerId(String internalPrimerId) {
        this.internalPrimerId = internalPrimerId;
    }

    public String getInternalSeq() {
        return internalSeq;
    }

    public void setInternalSeq(String internalSeq) {
        this.internalSeq = internalSeq;
    }

    public int getInternalLen() {
        return internalLen;
    }

    public void setInternalLen(int internalLen) {
        this.internalLen = internalLen;
    }

    public String getInternalStart() {
        return internalStart;
    }

    public void setInternalStart(String internalStart) {
        this.internalStart = internalStart;
    }

    public String getInternalTm() {
        return internalTm;
    }

    public void setInternalTm(String internalTm) {
        this.internalTm = internalTm;
    }

    public String getInternalGc() {
        return internalGc;
    }

    public void setInternalGc(String internalGc) {
        this.internalGc = internalGc;
    }

    public String getInternalAny() {
        return internalAny;
    }

    public void setInternalAny(String internalAny) {
        this.internalAny = internalAny;
    }

    public String getInternal3() {
        return internal3;
    }

    public void setInternal3(String internal3) {
        this.internal3 = internal3;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public List<BlatPsl> getInternalPrimerBlatList() {
        return internalPrimerBlatList;
    }

    public void setInternalPrimerBlatList(List<BlatPsl> internalPrimerBlatList) {
        this.internalPrimerBlatList = internalPrimerBlatList;
    }


}
