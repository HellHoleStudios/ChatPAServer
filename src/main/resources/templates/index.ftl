<#import "template.ftl" as layout>
<@layout.main pageTitle="问答">
    请注意：本网站只保存最近的十条消息。


    <button class="ui red button" onclick="clearHistory()">删除历史记录</button>

    <div class="ui accordion">
        <div class="title">
            <i class="dropdown icon"></i>
            对话总结
        </div>
        <div class="content">
            <div class="ui segment no-animation">
                <div class="ui segment no-animation">
                    <button class="ui primary button" onclick="generateSummary()">生成报告</button>
                    <button class="ui green button" onclick="parseSummary()">查看报告</button>
                    <p id="no_summary_prompt">您还没有生成过报告，请点击上方按钮以授权使用您的对话历史记录生成一个报告。</p>
                </div>
                <div class="ui segment no-animation" id="summary_prompt">
                    报告已经准备就绪，请按上方按钮查看报告。
                    <div class="ui active dimmer" id="summary_dimmer">
                        <div class="ui text loader">请稍等，生成报告大概需要1分钟时间……</div>
                    </div>
                    <svg id="markmap" style="width: 100%;height:500px;"></svg>
                </div>
            </div>
        </div>
    </div>

    <div class="ui comments" id="comments" style="max-width: 100%; max-height: 500px; overflow: scroll; ">

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
        $('.ui.accordion')
            .accordion()
        ;
        $('#summary_prompt').hide();

        let summaryMD=""
        let manualSummary=false;

        const { Marked } = globalThis.marked;
        const { markedHighlight } = globalThis.markedHighlight;
        const marked = new Marked(
            markedHighlight({
                emptyLangClass: 'hljs',
                langPrefix: 'hljs language-',
                highlight(code, lang, info) {
                    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
                    return hljs.highlight(code, { language }).value;
                }
            })
        );
        marked.use(markedAlert())

        // const options = {
        //     throwOnError: false,
        //     nonStandard: true
        // };
        // marked.use(markedKatex(options));

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

        function generateSummary(){
            ws.send("$$");
            manualSummary=true;
            $('#summary_dimmer').addClass('active');
            $('#no_summary_prompt').hide();
            $('#summary_prompt').show();
        }

        function parseSummary(){
            if(summaryMD===""){
                $.toast({
                    title: '无法显示报告',
                    message: '您还没有生成过报告，请先生成报告。',
                    showProgress: 'bottom',
                    class: 'warning',
                });
                return;
            }

            let md=summaryMD;

            // No plugin at all
            const transformer = new Transformer();

            // 1. transform Markdown
            const { root, features } = transformer.transform(md);

            // or get all possible assets that could be used later
            const assets = transformer.getAssets();

            $('#markmap').empty();
            Markmap.create('#markmap', undefined, root); // -> returns a Markmap instance

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
        let ws = new WebSocket(url.replace('#',''));
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
            error_text.text(event.reason!=="" ? event.reason : "未知原因");
            dia.show();

            $.toast({
                title: '连接已断开',
                message: '由于'+(event.reason!=="" ? event.reason : "未知原因")+'，与服务器的连接已经断开，请刷新网站。',
                showProgress: 'bottom',
                class: 'error',
                timeout: 10
            });
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

                //scroll to answer
                setTimeout(()=>{
                    inputField[0].scrollIntoView({behavior: 'smooth',
                        block: 'nearest'})
                },1);

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
            }else if(s[0]==='s'){
                $('#summary_dimmer').removeClass('active');
                summaryMD=s.substring(1);

                if(manualSummary){
                    $.toast({
                        title: '报告已生成',
                        message: '您的对话报告已经生成，请点击查看报告按钮查看。',
                        showProgress: 'bottom',
                        class: 'success',
                    });
                    manualSummary=false;
                }
                $('#no_summary_prompt').hide();
                $('#summary_prompt').show();
            }
        };
    </script>
</@layout.main>