<#import "template.ftl" as layout>
<@layout.main pageTitle="Token管理">
<div class="ui segment">
    请下载Tokens.csv文件，修改后重新上传。<b>上传后源文件将会被覆盖，同时会保留有且仅有一份备份，供应急恢复.</b> <br/>

    <div class="ui vertical animated green button" tabindex="0" onclick="window.open('tokenFile')">
        <div class="visible content">下载</div>
        <div class="hidden content">
            <i class="cloud download alternate icon"></i>
        </div>
    </div>

    <form action="/tokenUpload" method="post" enctype="multipart/form-data">
        <input class="ui input" type="file" name="file" id="file" accept=".csv">
        <input type="submit" class="ui primary button" value="上传">
    </form>
</div>

<div class="ui segment">
    当前Token数量:${token_count}。 <br/>

    所有可用的Token:${tokens}
</div>
</@layout.main>