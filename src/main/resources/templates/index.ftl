<#import "template.ftl" as layout>
<@layout.main pageTitle="问答">
    请注意：本网站只保存最近的十条消息。


    <button class="ui red button" onclick="clearHistory()">删除历史记录</button>
    <div class="ui comments" id="comments">

    </div>

    <div class="ui fluid input">
        <input type="text" id="prompt" placeholder="询问">
    </div>

    <div id="loading" style="margin-top: 20px">
        <div class="ui active inline loader"></div> <span id="loadingText">正在连接到服务器，现在请不要提问……</span>
    </div>

    <div class="ui icon message" id="error_dialog">
        <img class="ui crab tiny image" alt="Some Rust Stuff" src="/static/error.png">
        <div class="content" style="margin-left: 20px">
            <div class="header">
                连接已断开
            </div>
            <p>由于某些原因，与服务器的连接已经断开。请刷新后重试。（原因：<span id="error_text">未给出</span>）</p>
        </div>
    </div>

    <div class="ui segment" style="background-color: inherit;" id="response">
    </div>


    <script>

        function clearHistory(){
            $.post("/clearHistory",function(){
                $('#comments').html('');
                $.toast({
                    title: '已清除',
                    message: '您的历史记录已经全部清除！',
                    showProgress: 'bottom',
                    class: 'success',
                });
            });
        }

        function addComment(image, time, text){
            let comments = $('#comments');
            let x=`
<div class="doSlide comment">
<a class="avatar">
  <img src="%a%">
</a>
<div class="content">
  <a class="author">%au%</a>
  <div class="metadata">
    <div class="date">%d%</div>
  </div>
  <div class="text">
    %t%
  </div>
</div>
</div>
`;
            x=x.replace("%a%",'static/'+image+".png").replace("%d%",time).replace("%t%",text);
            if(image==="user"){
                x=x.replace("%au%","用户");
            }else{
                x=x.replace("%au%","小助手");
            }
            comments.append(x);
        }

        let inputField = $('#prompt');
        let loading = $('#loading');
        let loadingText = $('#loadingText');
        let response = $('#response');
        let dia= $('#error_dialog');
        let error_text=$('#error_text');
        dia.hide();
        response.hide();

        inputField.on("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                const inputValue = inputField.val();

                loading.show();
                loadingText.text('正在发送问题……');
                inputField.attr('disabled','1');
                inputField.parent().addClass('testBorder');
                addComment("user",new Date().toLocaleString(),inputValue);
                ws.send(inputValue);
            }
        });

        //get url by replacing http with ws
        let markdown = "";
        let url = window.location.href.replace("http", "ws");
        let ws = new WebSocket(url);
        ws.onopen = function () {
            //TODO: add a loading animation
            loading.hide();
        };

        ws.onerror=function(){
            //TODO show error
            dia.show();
        };

        ws.onclose=function(event){
            inputField.parent().removeClass('testBorder');
            error_text.text(event.reason!=="" ? event.reason : "无")
            dia.show();
        }

        ws.onmessage = function (event) {
            let s = event.data;

            if(s[0]==='T'){
                //new token
                let token = s.substring(1);
                markdown+=token;
                response.html(marked.parse(markdown));
            }else if(s[0]==='S'){
                //started answering
                markdown="";
                response.html('');
                response.show('');
                loadingText.text("回答中……");
            }else if(s[0]==='E'){
                //ended
                loading.hide();
                inputField.removeAttr('disabled');
                inputField.parent().removeClass('testBorder');

                addComment("bot",new Date().toLocaleString(),marked.parse(markdown));
            }else if(s[0]==='l'){
                //last question
                inputField.attr('disabled','1');
                inputField.parent().addClass('testBorder');
                inputField.val(s.substring(1));
            }else if(s[0]==='L'){
                //last answer
                response.show()
                markdown=s.substring(1)
                response.html(marked.parse(markdown));
            }else if(s[0]==='H'){
                let content=JSON.parse(s.substring(1));
                addComment(content.role, new Date(content.time).toLocaleString(), marked.parse(content.content));
            }
        };
    </script>
</@layout.main>