<#macro main pageTitle>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="AI Assistant for PA remastered">
    <title>${pageTitle} - ChatCode</title>
    <script src="/static/external/jquery.min.js"></script>

    <!-- Semantic -->
    <link rel="stylesheet" href="/static/external/semantic.min.css">
    <script src="/static/external/semantic.min.js"></script>

    <!-- Marked -->
    <script src="/static/external/marked.min.js"></script>
    <script src="/static/external/marked.umd.js"></script>
    <script src="/static/external/index.umd.js"></script>
    <script src="/static/external/index.umd.min.js"></script>
<#--    <link rel="stylesheet" href="/static/external/katex.min.css" crossorigin="anonymous">-->
<#--    <script src="/static/external/katex.min.js" crossorigin="anonymous"></script>-->
<#--    <script src="https://cdn.jsdelivr.net/npm/marked-katex-extension/lib/index.umd.js"></script>-->

    <!-- Highlight.js -->
    <link rel="stylesheet" href="/static/external/default.min.css">
    <script src="/static/external/highlight.min.js"></script>

    <!-- MarkMap -->
    <script src="/static/external/markmap-lib.js"></script>
    <script src="/static/external/d3@7.js"></script>
    <script src="/static/external/markmap-view.js"></script>
    <script>
        const { Transformer } = window.markmap;
        const { markmap } = window;
        const { Markmap, loadCSS, loadJS } = markmap;
    </script>

    <!-- Our CSS -->
    <link rel="stylesheet" href="/static/main.css">

</head>
<body>
<div class="ui container">
    <nav class="ui secondary pointing menu">
        <img src="/static/logo.png" alt="Logo" class="ui tiny image crab" onclick="$('.segment').toggleClass('testBorder')">
        <h1 class="header item"><a href="/">ChatCode问答系统</a></h1>
        <div class="right menu">
            <a class="item upItem" href="/">问答</a>
            <a class="item upItem" href="#" onclick="window.open(location.protocol+'//'+location.hostname+':9090')">数据<i class="external alternate icon"></i></a>
            <#if session.token??>
                <#if admin>
                    <a class="item upItem" href="/session">Session查看</a>
                    <a class="item upItem" href="/token">Token管理</a>
                </#if>
                <a class="item upItem" href="/logout">${name!"MISSING_NO"}</a>
            <#else>
            <a class="item upItem" href="/login">登录</a>
            </#if>
        </div>
    </nav>
</div>

<main class="ui container" style="margin-top: 20px; margin-bottom: 20px;">
    <#-- This is where the content of the specific page will be inserted -->
    <div class="ui segment">
        <h1>${pageTitle}</h1>

        <#nested>
    </div>
</main>

<div class="ui container">
    <div class="ui center aligned container">
        <p>&copy; 2025 XGN and XIZCM, Nanjing University</p>
    </div>
</div>
</body>
</html>
</#macro>