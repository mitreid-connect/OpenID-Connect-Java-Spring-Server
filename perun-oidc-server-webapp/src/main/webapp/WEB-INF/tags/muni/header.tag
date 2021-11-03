<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags/common" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="reqURL" required="true" %>
<%@ attribute name="baseURL" required="true" %>
<%@ attribute name="samlResourcesURL" required="true" %>
<%@ attribute name="cssLinks" required="true" type="java.util.ArrayList<java.lang.String>" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html class="no-js touch no-touch" lang="${langProps['other_lang']}">
<head>
    <meta cahrset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta name="author" content="Masarykova univerzita" />

    <title>${langProps['unified_login']} | ${title}</title>

    <link rel="stylesheet" type="text/css" href="https://id.muni.cz/simplesaml/module.php/muni/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="https://id.muni.cz/simplesaml/module.php/muni/css/style-ie.css?1.2">
    <link rel="stylesheet" type="text/css" href="https://id.muni.cz/simplesaml/module.php/muni/css/style.css?1.2">
    <link rel="stylesheet" type="text/css" href="https://id.muni.cz/simplesaml/module.php/muni/css/style2.css?1.2">

    <style type="text/css">
        .checkbox-wrapper {
            float: left;
        }
        .attrname-formatter {
            display: block;
            margin-left: 2em !important;
        }
    </style>

    <o:headerCssLinks cssLinks="${cssLinks}"/>

</head>

<body>
    <c:set var="alternateURL" value="${reqURL}&lang=${langProps['other_lang']}"/>
    <p class="menu-accessibility">
        <a title="${langProps['go_to_login_title']}" accesskey="2" href="#main">
            ${langProps['go_to_login_text']}
        </a>
    </p>
    <div class="header u-mb-0">
        <div class="row-main">
            <div class="header__wrap">
                <h1 class="header__logo">
                    <img src="${samlResourcesURL}/module.php/${theme}/img/${langProps['img_name']}.png"
                         width="${langProps['img_width']}" height="${langProps['img_height']}" alt="${langProps['muni_logo']}"/>
                </h1>
                <div class="header__side">
                    <div class="menu-lang" role="navigation">
                        <p class="menu-lang__selected">
                            <a href="${alternateURL}" rel="alternate" hreflang="${langProps['other_lang']}"
                               lang="${langProps['other_lang']}" class="menu-lang__selected__link">
                                ${langProps['other_language']}
                            </a>
                        </p>
                    </div>
                    <nav class="menu-mobile" role="navigation">
                        <div class="menu-mobile__wrap">
                            <div class="row-main">
                                <ul class="menu-mobile__list">
                                    <li class="menu-mobile__item">
                                        <a href="${alternateURL}" rel="alternate" hreflang="${langProps['other_lang']}"
                                            class="menu-mobile__link menu-mobile__link--lang" lang="${langProps['other_lang']}">
                                            ${langProps['other_language']}
                                        </a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </nav>
                </div>
            </div>
        </div>
    </div>

    <!-- END MU HEADER -->
    <main class="main">
        <div class="box-hero box-hero--particles box-hero--login u-mb-0 u-pt-50">
            <div class="row-main">
                <div>
