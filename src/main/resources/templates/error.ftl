<#import "template.ftl" as layout>
<@layout.main pageTitle="错误">


<div class="ui icon negative message">
        <img class="ui crab tiny image" alt="Some Rust Stuff" src="/static/error.png">
        <div class="content" style="margin-left: 20px">
                <div class="header">
                        不，这不对！
                </div>
                <p>${error}</p>
        </div>
</div>

</@layout.main>