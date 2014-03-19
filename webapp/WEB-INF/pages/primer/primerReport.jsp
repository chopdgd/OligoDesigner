<%@ page import="java.util.List" %>
<%@ page import="edu.chop.dgd.utils.Chromosome" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="edu.chop.dgd.web.DisplayMapper" %>
<%@ page import="edu.chop.dgd.web.HttpRequestFacade" %>
<%@ page import="java.util.Map" %>
<%@ page import="edu.chop.dgd.process.primerCreate.UcscGene" %>
<%@ page import="edu.chop.dgd.process.primerCreate.Variation" %>
<%@ page import="edu.chop.dgd.process.primerCreate.UcscGeneExon" %>
<%@ page import="edu.chop.dgd.process.primerCreate.AmpliconSeq" %>
<%
    String pageTitle = "Division of Genomic Diagnstics";
    String headContent = "Division of Genomic Diagnstics";
    String pageDescription = "Division of Genomic Diagnstics";
    String message = (String) request.getAttribute("message");
    int speciesTypeKey = 1;
    Chromosome chrObj = new Chromosome();
    ArrayList<String> chrNames = (ArrayList<String>) chrObj.getChromosomes(speciesTypeKey);
    System.out.println(chrNames);

    HttpRequestFacade req= new HttpRequestFacade(request);
    ArrayList error = new ArrayList();
    DisplayMapper dm = new DisplayMapper(req, error);

    int start = Integer.parseInt((String) request.getParameter("start"));
    int stop = Integer.parseInt((String) request.getParameter("stop"));
    String chr = "chr" + request.getParameter("selectObject");
    if(chr.equals("23")){
        chr = "chrX";
    }else if(chr.equals("24")){
        chr = "chrY";
    }

    Map<UcscGene, UcscGeneExon> geneExonMap = (Map<UcscGene, UcscGeneExon>) request.getAttribute("geneExonMap");
    List<UcscGene> geneList = (List<UcscGene>) request.getAttribute("geneList");
    List<Variation> variationList = (List<Variation>) request.getAttribute("variantsList");
    AmpliconSeq amplicon = (AmpliconSeq) request.getAttribute("amplicon");
    String primerResults = (String) request.getAttribute("primerResults");
    int varrelPos = variationList.get(0).getVstart()-amplicon.getAmpliconStart()+2;

    String optimalPrimers = (String) request.getAttribute("stats");
%>




<html>
<body>
<img src="dgd.png" alt="dgdLogo" width="42" height="42">

	<h1>DGD Primer Designer Report</h1>

    <table>
        <tr>
            <td>QueryRange:<%=chr%>:<%=start%>-<%=stop%></td>
        </tr>
        <tr>
            <td>Genes In Region:
            <%
                for(UcscGene g:geneList){
                    out.print(g.getGeneSymbol()+"("+g.getRefSeqGeneName()+")");
                    out.print(", ");
                }
            %>
            </td>
        </tr>
        <tr>
            <td>Exons in range:
            <%
                if(geneExonMap.keySet().size()>0){
                    for(UcscGene gene : geneExonMap.keySet()){
                        out.print(geneExonMap.get(gene).getUcscExonId());
                        out.print("("+gene.getGeneSymbol()+")");
                        out.print(", ");
                    }
                }else{
                    out.print("Possibly Intronic");
                }
            %>
            </td>
        </tr>
        <tr>
            <td>Variants in Range:
                <%
                    for(Variation v : variationList){
                        out.print(v.getVarName());
                        out.print("-"+v.getvChr()+":"+v.getVstart()+":"+v.getVstop()+":"+v.getvStrand());
                        out.print("("+v.getvObserved()+")\t");
                    }
                %>
            </td>
        </tr>
        <tr>
            <td>Amplicon Sequence:<%=amplicon.getChr()+":"+amplicon.getAmpliconStart()+".."+amplicon.getAmpliconEnd()%>
                <pre><%
                    for(int c=1; c<=amplicon.getSequence().length(); c++){
                        String value = amplicon.getSequence().substring(c - 1, c);
                        if(c==varrelPos){
                            out.print("<span style='color:red; font-weight:700;'>" + value + "</span>");
                        }else{
                            out.print(value);
                        }
                        if(c % 60 == 0){
                            out.print("\n");
                        }
                    }
                    out.println(" ");
                %></pre>
            </td>
        </tr>
        <tr>
            <td>Amplicon Masked Sequence:<%=amplicon.getChr()+":"+amplicon.getAmpliconStart()+".."+amplicon.getAmpliconEnd()%>
                <pre><%
                    for(int c=1; c<=amplicon.getMaskedSeq().length(); c++) {
                        String value = amplicon.getMaskedSeq().substring(c - 1, c);

                        if(variationList.get(0).getVarName().equals("novel")){
                            if(c==varrelPos){

                                out.print("<span style='color:red;background-color:#02599C; font-weight:700;'>" + value + "</span>");

                            }else if(value.equals("N")){

                                out.print("<span style='color:red; font-weight:700;'>" + value + "</span>");

                            }else{
                                out.print(value);
                            }
                        }else{
                            if(value.equals("N")){
                                if(c==varrelPos){
                                    out.print("<span style='color:red;background-color:#02599C; font-weight:700;'>" + value + "</span>");
                                }else{
                                    out.print("<span style='color:red; font-weight:700;'>" + value + "</span>");
                                }
                            }else{
                                out.print(value);
                            }
                        }

                        if(c % 60 == 0){
                            out.print("\n");
                        }
                    }
                    out.println(" ");
                    %>
                </pre>
            </td>
        </tr>
        <tr>
            <td>Results:</td>
        </tr>
        <tr>
            <td colspan="2"><pre><%=primerResults%></pre></td>
        </tr>

        <tr>
            <td><br/></td>
        </tr>
        <tr>
            <td>Optimal Primers are:</td>
        </tr>
        <tr>
            <td><%=optimalPrimers.replaceAll("\t","<br/>")%></td>
        </tr>
    </table>
    <br />
</body>
</html>


