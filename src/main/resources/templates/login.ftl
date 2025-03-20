<#import "template.ftl" as layout>
<@layout.main pageTitle="登录">

<form class="ui form" method="post">
    <div class="field">
        <label>Token</label>
        <input type="password" name="token" placeholder="请输入Token">
    </div>
    <button class="ui button" type="submit">登录</button>
</form>
</@layout.main>