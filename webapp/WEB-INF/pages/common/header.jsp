<%--
  Created by IntelliJ IDEA.
  User: jayaramanp
  Date: 11/5/14
  Time: 4:09 PM
  To change this template use File | Settings | File Templates.
--%>


<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
<!-- Optional theme -->
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="https://github.com/flatlogic/awesome-bootstrap-checkbox/blob/master/awesome-bootstrap-checkbox.css">
<link rel="stylesheet" href="navbar_changed.css">
<link rel="stylesheet" href="../../../resources/css/awesome-bootstrap-checkbox.css">

<!-- Latest compiled and minified JavaScript -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
<script src="../../js/validator.js"></script>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

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

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>

    <link rel="shortcut icon" href="/Antholigo/resources/images/favicon.ico" type="image/x-icon"/>
    <title><%=request.getParameter("pageTitle")%></title>

</head>
<body>

<nav class="navbar" role="navigation">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>

            <%
                String projectTitle = request.getParameter("projectTitle");
            %>


            <a class="navbar-brand" href="#"><%=projectTitle%></a>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li class="active"><a href=/Antholigo/oligo/home.html>Home</a></li>
                <li><a href="http://www.chop.edu/centers-programs/division-genomic-diagnostics">About</a></li>
                <li><a href="https://www.chop.edu/centers-programs/pathology-and-laboratory-medicine/contact">Contact</a></li>
            </ul>
            <ul class="nav navbar-right">
                <li><img src=<%=request.getParameter("logoImgPath")%> alt="dgdLogo"
                         width="200" height=60"/></li>
            </ul>
            <!--/.nav-collapse -->
        </div>
    </div>
</nav>




<br /> <br />