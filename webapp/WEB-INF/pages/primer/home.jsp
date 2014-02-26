
<%@ page import="edu.chop.dgd.utils.Chromosome" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="edu.chop.dgd.web.DisplayMapper" %>
<%@ page import="edu.chop.dgd.web.HttpRequestFacade" %>
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

    String start = (String)req.getParameter("start");
    String stop = (String) req.getParameter("stop");
    String runId = (String) req.getParameter("runId");
    String chr = (String) req.getParameter("selectObject");

%>




<html>
<body>
	<h1>DGD Primer Designer Home</h1>
    <h2><%=message%></h2>

    <form id="primerCreate" action="/dgdweb/primer/primerReport.html" method="GET"
          onsubmit="return validateDetails()">

    <div id="regionId">
        <table>
            <tr>
                <td>Enter Chromosome</td>
                    <%
                    //for(int o=0; o<=chrNames.size(); o++){
                    %>
                <td>
                    <select id="<%="selectObject"+(1)%>" name="selectObject">
                        <option value="1" ${param.selectObject == '1' ? 'selected' : ''}>Chr1</option>
                        <option value="2" ${param.selectObject == '2' ? 'selected' : ''}>Chr2</option>
                        <option value="3" ${param.selectObject == '3' ? 'selected' : ''}>Chr3</option>
                        <option value="4" ${param.selectObject == '4' ? 'selected' : ''}>Chr4</option>
                        <option value="5" ${param.selectObject == '5' ? 'selected' : ''}>Chr5</option>
                        <%--<option value="0">
                        <c:if test='<%=dm.out("selectObject",chrNames.get(o), o).equals("0")%>'> selected="selected"</c:if>
                        Select Chromosome
                        </option>
                        <option value="1"<c:if test='<%=dm.out("selectObject",chrNames.get(o), o).equals("Chr1")%>'>selected="selected"</c:if>>Chr1</option>
                        <option value="2"<c:if test='<%=dm.out("selectObject",chrNames.get(o), o).equals("Chr2")%>'>selected="selected"</c:if>>Chr2</option>
                        <option value="3"<c:if test='<%=dm.out("selectObject",chrNames.get(o), o).equals("Chr3")%>'>selected="selected"</c:if>>Chr3</option>
                        <option value="4"<c:if test='<%=dm.out("selectObject",chrNames.get(o), o).equals("Chr4")%>'>selected="selected"</c:if>>Chr4</option>
                        <option value="5"<c:if test='<%=dm.out("selectObject",chrNames.get(o), o).equals("Chr5")%>'>selected="selected"</c:if>>Chr5</option>--%>

                    </select>
                </td>
                <%
                    //}
                %>
            </tr>
            <tr>
                <td>Enter position</td>
                <td><input id="start" name="start" type="text" size="20" value="<%=dm.out("start", start)%>"></td>
                <td><input id="stop" name="stop" type="text" size="20" value="<%=dm.out("stop", stop)%>"></td>
            </tr>
            <tr>
                <td>Enter Run number</td>
                <td><input id="runId" name="runId" type="text" size="10" value="<%=dm.out("runId", runId)%>"></td>
            </tr>
        </table>
    </div>
        <input type="submit" value="Submit search">
    </form>
    <br />

</body>
</html>

<script>
    var select = document.getElementById("selectNumber");
    alert(select.id);
    var options=["Chr1","Chr2","Chr3","Chr4","Chr5","Chr6","Chr7","Chr8","Chr9","Chr10","Chr11","Chr12","Chr13","Chr14","Chr15","Chr16","Chr17","Chr18","Chr19","Chr20","Chr21","Chr22","ChrX","ChrY"];

    for(var i = 0; i < options.length; i++) {
        var opt = options[i];

        var el = document.createElement("option");
        el.textContent = opt;
        el.value = opt;
        select.appendChild(el);
    }

function submitSearch(){
    var chr = document.getElementById('selectNumber').selectedIndex;
    if(chr==23){
        chr="X";
    }else if(chr==24){
        chr="Y";
    }
    //alert(chr);
}

function validateDetails() {
    var selected = document.getElementById("selectNumber");
    var chrSelected = selected.options[selected.selectedIndex].value;
    alert(chrSelected);
    var assembly = document.getElementById("selectNumber").value;
    alert(assembly);

    var result = "";
    var error = "";
    //var formValue = document.getElementById(formVal);
    //var gene = document.getElementById("gene_ens_id1").value;
    var startPos =  document.getElementById("start").value;
    var stopPos = document.getElementById("stop").value;
    var run = document.getElementById("runId").value;

    if(result.length>1){
        alert("Some fields need correction:\n" + result);
        return false;
    }else{
        return true;
    }

}

</script>

