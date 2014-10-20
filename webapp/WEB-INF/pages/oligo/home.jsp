<%@ page import="edu.chop.dgd.web.DisplayMapper" %>
<%@ page import="edu.chop.dgd.web.HttpRequestFacade" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.w3c.dom.*" %>
<%@ page import="sun.plugin.javascript.navig.*" %>
<%--
  Created by IntelliJ IDEA.
  User: jayaramanp
  Date: 6/1/14
  Time: 7:35 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/core"%>

<%--
<script type="text/javascript" src="jquery-1.2.6.min.js">


    //init JavaPowUpload JavaScript object
    var JavaPowUpload;
    JavaPowUpload = document.getElementById("JavaPowUpload");
    //where "javaPowUpload" is id attribute of <applet> tag>
    //Now we can use javaPowUpload methods, for example show browse dialog
    JavaPowUpload.clickBrowse();

</script>
--%>

<script type="text/javascript">
    function getProjectId(id){
        var projectId = document.getElementById(id);
        var projectName = projectId.value;

        return projectName;
    }

    function validateForm(formId){
        var form = document.getElementById(formId);
        alert("Everything looks good for:" + form.projectName.value);
    }
</script>


<%
    String uploadedFilePath = (String) request.getAttribute("uploads");
    HttpRequestFacade req= new HttpRequestFacade(request);
    ArrayList error = new ArrayList();
    DisplayMapper dm = new DisplayMapper(req, error);
    String pid = request.getParameter("proj_id");
%>


<html>
<body>
<form id="uploadForm" name="uploadForm" action="/dgdweb/oligo/fileUpload.html" method="GET" onsubmit="return validateForm(this)">
    <table>
        <tr>
            <td>
                <b>Enter Project ID</b>
            </td>
            <td>
                <input id="proj_id" name="proj_id" type="text" size="94" align="left"
                       value="<%=dm.out("proj_id", pid)%>" onkeyup="document.uploadFileForm.projectName.value=document.uploadForm.proj_id.value"/>
            </td>
        </tr>
        <tr>
            <td>
                <input id="uploadFolderPath" name="uploadFolderPath"
                       value="<%=dm.out("uploadFolderPath", uploadedFilePath)%>" type="hidden">
            </td>
        </tr>
        <tr>
            <td>
            <h3>Please select a file to upload !</h3>
            </td>
        </tr>
        <tr>
            <td colspan="2">
            <applet CODE="wjhk.jupload2.JUploadApplet"
                    CODEBASE="/dgdweb/upload/"
                    NAME="JUpload"
                    ARCHIVE="wjhk.jupload.jar"
                    WIDTH="800"
                    HEIGHT="600"
                    MAYSCRIPT="true"
                    ALT="The java plugin must be installed.">
                <PARAM name="postURL" value="/dgdweb/upload/parseRequest.jsp" />
                <PARAM name="formdata" value="uploadFileForm" />
                <PARAM name="error" value="true" />
                <!--<PARAM name="afterUploadURL" value="/dgdweb/oligo/fileUpload.html?"/>-->
                <!-- Optionnal, see code comments -->
                <PARAM name="ftpCreateDirectoryStructure" value="true" />
                <PARAM name="urlToSendErrorTo" value="/dgdweb/upload/uploadError.txt"/>

                <!-- Optionnal, see code comments -->
                <PARAM name="showLogWindow" value="true" />
                <PARAM name="debugLevel" value="99" />"
            </applet>
            </td>
        </tr>
        <tr>
            <td>
                <!-- <input type="submit" onclick="alert(document.uploadFileForm.projectName.value)" value="click This!!"> -->
                <br />
                <h4>Compile Results..<input type="submit" size="200" >
                </h4>
            </td>
        </tr>
    </table>
</form>

<form name="uploadFileForm" id="uploadFileForm" action="">
    <input name="uploadedFileLoc" id ="uploadedFileLoc" type="text" value="<%=uploadedFilePath%>">
    <input name="projectName" id="projectName" type="text" disabled >
</form>

<SCRIPT TYPE="TEXT/JAVASCRIPT">
    document.uploadForm.proj_id.focus()
</SCRIPT>

</body>
</html>
