<%@attribute name="title" required="false"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ tag import="com.google.gson.Gson" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<head>
    <base href="${config.issuer}">
    <meta charset="utf-8">
    <title>${config.topbarTitle} - ${title}</title>

    <!-- meta -->
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0" />
    <meta name="robots" content="noindex, nofollow" />

    <!-- LINKS -->
    <link rel="icon" type="image/icon"
          href="https://login.elixir-czech.org/proxy/module.php/elixir/res/img/icons/favicon.ico" />
    <link rel="stylesheet" type="text/css"
          href="https://login.elixir-czech.org/proxy/resources/default.css" />
    <link rel="stylesheet" type="text/css"
          href="https://login.elixir-czech.org/proxy/module.php/elixir/res/bootstrap/css/bootstrap.min.css" />
    <link rel="stylesheet" type="text/css"
          href="https://login.elixir-czech.org/proxy/module.php/elixir/res/css/elixir.css" />
    <!-- ELIXIR -->
    <link rel="stylesheet" media="screen" type="text/css"
          href="https://login.elixir-czech.org/proxy/module.php/consent/style.css" />
    <link rel="stylesheet" media="screen" type="text/css"
          href="https://login.elixir-czech.org/proxy/module.php/elixir/res/css/consent.css" />

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="resources/js/lib/html5.js"></script>
    <![endif]-->
</head>
<body>
    <div id="wrap">
        <div id="header">
            <img src="https://login.elixir-czech.org/proxy/module.php/elixir/res/img/logo_256.png" alt="Elixir logo">
            <h1>Consent about releasing personal information</h1><!--TODO message from spring files -->
        </div>