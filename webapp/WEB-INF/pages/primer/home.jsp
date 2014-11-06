
<%@ page import="edu.chop.dgd.dgdObjects.Chromosome" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="edu.chop.dgd.web.DisplayMapper" %>
<%@ page import="edu.chop.dgd.web.HttpRequestFacade" %>
<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">

<!-- Optional theme -->
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css">

<!-- Latest compiled and minified JavaScript -->
<script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>

<%
    String pageTitle = "Division of Genomics Diagnostics Primer Design Application";
    String headContent = "Division of Genomics Diagnostics Primer Design Application";
    String pageDescription = "Division of Genomics Diagnostics Primer Design Application";
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


<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%=pageTitle%></title>

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom styles for this template -->
    <link href="http://getbootstrap.com/examples/signin/signin.css" rel="stylesheet">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>
<body>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="js/bootstrap.min.js"></script>

<div class="container">

    <p><img src="/dgdweb/resources/images/dgd.png" alt="dgdLogo" width="100" height="75" style="border: inset"/></p>


    <form id="primerCreate" action="/dgdweb/primer/primerReport.html" method="GET"
          onsubmit="return validateDetails()">

    <div id="regionId">

        <table class="searchTable" style="align-content: center;" >
            <h2><%=message%></h2>

        <thead>
            <tr>
                <th><p>Enter Chromosome</p></th>
                <th>
                    <select id="<%="selectObject"+(1)%>" name="selectObject">

                        <%
                            for(int i=1; i<=chrNames.size(); i++){
                                out.print("<option value=\""+i+"\" ${param.selectObject == '"+i+"' ? 'selected' : ''}>"+chrNames.get(i-1)+"</option>");
                            }
                        %>
                    </select>
                </th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><p>Enter position</p></td>
            </tr>
            <tr>
                <td>
                    <input id="start" name="start" type="text" size="20" value="<%=dm.out("start", start)%>">
                </td>
                &NonBreakingSpace;
                <td>
                    <input id="stop" name="stop" type="text" size="20" value="<%=dm.out("stop", stop)%>">
                </td>
            </tr>
            <tr>
                <td>
                    <br/>
                </td>
            </tr>
        </tbody>
        <thead>
            <tr>
                <th>
                    <p><input type="submit" value="Submit search"></p>
                </th>
            </tr>
        </thead>

        </table>

    </div>
    </form>
    <br />



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

<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
<script src="../../dist/js/bootstrap.min.js"></script>
</div>
</body>
</html>