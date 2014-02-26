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

%>




<html>
<body>
	<h1>DGD Primer Designer Report</h1>

    <table>
        <tr>
            <td>QueryRange:<%=chr%>:<%=start%>-<%=stop%></td>
        </tr>
        <tr>
            <td>Genes In Region:
            <%
                for(UcscGene g:geneList){
                    out.print(g.getGeneSymbol());
                    out.print(",");
                }
            %>
            </td>
        </tr>
        <tr>
            <td>Exons in range:
            <%
                for(UcscGene gene : geneExonMap.keySet()){
                    out.print(geneExonMap.get(gene).getUcscExonId());
                    out.print("("+gene.getGeneSymbol()+")");
                    out.print(",");
                }
            %>
            </td>
        </tr>
        <tr>
            <td>Variants in Range:
                <%
                    for(Variation v : variationList){
                        out.print(v.getVarName());
                        out.print("("+v.getvObserved()+")");
                    }
                %>
            </td>
        </tr>
        <tr>
            <td>Amplicon Sequence:
                <pre><%
                    out.print(amplicon.getSequence().replace("\n","<br />"));
                %></pre>
            </td>
        </tr>
        <tr>
            <td>Results:</td>
        </tr>
        <tr>
            <td colspan="2"><pre><%=primerResults%></pre></td>
        </tr>
    </table>
    <br />
</body>
</html>

</script>

