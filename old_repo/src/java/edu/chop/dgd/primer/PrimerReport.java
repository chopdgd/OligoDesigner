package edu.chop.dgd.primer;

import edu.chop.dgd.process.primerCreate.AmpliconSeq;
import edu.chop.dgd.process.primerCreate.UcscGene;
import edu.chop.dgd.process.primerCreate.UcscGeneExon;
import edu.chop.dgd.process.primerCreate.Variation;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by jayaramanp on 3/20/14.
 */
public class PrimerReport {

    PrintWriter reportWriter;

    List<UcscGene> geneList;
    List<Variation> variationList;
    Map<UcscGene, UcscGeneExon> geneExonMap;
    AmpliconSeq amplicon;
    String response;
    int varrelPos=0;


    public PrimerReport(PrintWriter reportWriter) {
        this.reportWriter = reportWriter;
    }


    public void createReport() throws Exception{

        reportWriter.print("<html><body><img src=\"../../dgd.png\" alt=\"dgdLogo\" width=\"42\" height=\"42\"><h1>DGD Primer Designer Report</h1>");
        reportWriter.print("<table><tr><td>QueryRange:"+variationList.get(0).getvChr()+":"+variationList.get(0).getVstart()+"-"+variationList.get(0).getVstop()+"</td></tr><tr><td>Genes In Region:");

        for(UcscGene g:geneList){
            reportWriter.print(g.getGeneSymbol() + "(" + g.getRefSeqGeneName() + ")");
            reportWriter.print(", ");
        }

        reportWriter.print("</td></tr><tr><td>Exons in range:");

        if(geneExonMap.keySet().size()>0){
            for(UcscGene gene : geneExonMap.keySet()){
                reportWriter.print(geneExonMap.get(gene).getUcscExonId());
                reportWriter.print("(" + gene.getGeneSymbol() + ")");
                reportWriter.print(", ");
            }
        }else{
            reportWriter.print("Possibly Intronic");
        }


        reportWriter.print("</td></tr><tr><td>Variants in Range:");

        for(Variation v : variationList){
            reportWriter.print(v.getVarName());
            reportWriter.print("-" + v.getvChr() + ":" + v.getVstart() + ":" + v.getVstop() + ":" + v.getvStrand());
            reportWriter.print("(" + v.getvObserved() + ")\t");
        }


        reportWriter.print("</td></tr><tr><td>Amplicon Sequence:");
        reportWriter.print(amplicon.getChr() + ":" + amplicon.getAmpliconStart() + ".." + amplicon.getAmpliconEnd());
        reportWriter.print("<pre>");

        for(int c=1; c<=amplicon.getSequence().length(); c++){
            String value = amplicon.getSequence().substring(c - 1, c);
            if(c==varrelPos){
                reportWriter.print("<span style='color:red; font-weight:700;'>" + value + "</span>");
            }else{
                reportWriter.print(value);
            }
            if(c % 60 == 0){
                reportWriter.print("\n");
            }
        }

        reportWriter.println(" ");
        reportWriter.print("</pre>");
        reportWriter.print("</td></tr><tr><td>Amplicon Masked Sequence:");
        reportWriter.print(amplicon.getChr() + ":" + amplicon.getAmpliconStart() + ".." + amplicon.getAmpliconEnd());
        reportWriter.print("<pre>");

        for(int c=1; c<=amplicon.getMaskedSeq().length(); c++) {
            String value = amplicon.getMaskedSeq().substring(c - 1, c);
            if(variationList.get(0).getVarName().equals("novel")){
                if(c==varrelPos){
                    reportWriter.print("<span style='color:red;background-color:#02599C; font-weight:700;'>" + value + "</span>");
                }else if(value.equals("N")){
                    reportWriter.print("<span style='color:red; font-weight:700;'>" + value + "</span>");
                }else{
                    reportWriter.print(value);
                }
            }else{
                if(value.equals("N")){
                    if(c==varrelPos){
                        reportWriter.print("<span style='color:red;background-color:#02599C; font-weight:700;'>" + value + "</span>");
                    }else{
                        reportWriter.print("<span style='color:red; font-weight:700;'>" + value + "</span>");
                    }
                }else{
                    reportWriter.print(value);
                }
            }

            if(c % 60 == 0){
                reportWriter.print("\n");
            }
        }
        reportWriter.println(" ");
        reportWriter.print("</pre>");
        reportWriter.print("</td></tr><tr><td>Results:</td></tr><tr><td colspan=\"2\">");
        reportWriter.print("<pre>");
        reportWriter.print(response);
        reportWriter.print("</pre>");
        reportWriter.print("</td></tr><tr><td><br/></td></tr></table><br /></body></html>");


    }



    public PrintWriter getReportWriter() {
        return reportWriter;
    }

    public void setReportWriter(PrintWriter reportWriter) {
        this.reportWriter = reportWriter;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<UcscGene> getGeneList() {
        return geneList;
    }

    public void setGeneList(List<UcscGene> geneList) {
        this.geneList = geneList;
    }

    public List<Variation> getVariationList() {
        return variationList;
    }

    public void setVariationList(List<Variation> variationList) {
        this.variationList = variationList;
    }

    public Map<UcscGene, UcscGeneExon> getGeneExonMap() {
        return geneExonMap;
    }

    public void setGeneExonMap(Map<UcscGene, UcscGeneExon> geneExonMap) {
        this.geneExonMap = geneExonMap;
    }

    public AmpliconSeq getAmplicon() {
        return amplicon;
    }

    public void setAmplicon(AmpliconSeq amplicon) {
        this.amplicon = amplicon;
    }

    public int getVarrelPos() {
        return varrelPos;
    }

    public void setVarrelPos(int varrelPos) {
        this.varrelPos = varrelPos;
    }
}
