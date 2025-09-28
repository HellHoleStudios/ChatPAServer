<#import "template.ftl" as layout>
<@layout.main pageTitle="Session">
    <p>模型端连接状态：${serverSession}</p>
    <div class="ui fluid styled accordion" id="ac">
        <#list sessions as session>
            <div class="title" style="min-height: 30px">
                <b>${session.getKey()!"?"}</b>
<#--                <div style="float:left">-->
<#--                    <b>${session.token!"?"}</b>-->
<#--                </div>-->

                    <#assign s=session.getValue().status>
                    <#if s=="NO_QUESTION">
                        <div style="float:right; color: grey;">
                            <i class="ellipsis horizontal icon"></i>
                            <span class="ui grey text">无问题</span>
                    <#elseif s=="WAITING">
                        <div style="float:right; color: orangered;">
                            <i class="clock icon"></i>
                            <span class="ui yellow text">等待中</span>
                            <span>${(currentTime-session.getValue().startTime)/1000} s</span>
                    <#elseif s=="RUNNING">
                        <div style="float:right; color: green;">
                            <i class="recycle icon"></i>
                            <span class="ui green text">生成中</span>
                            <span>${(currentTime-session.getValue().startTime)/1000} s</span>
                    <#elseif s=="LOST">
                        <div style="float:right; color: saddlebrown;">
                            <i class="question icon"></i>
                            <span class="ui black text">未送达</span>
                            <span>${(currentTime-session.getValue().startTime)/1000} s</span>
                    <#elseif s=="ENDED">
                    <div style="float:right; color: deeppink;">
                        <i class="check circle icon"></i>
                        <span class="ui purple text">已完成</span>
                        <span>${(session.getValue().endTime-session.getValue().startTime)/1000.0} s</span>
                    </#if>
                </div>
            </div>
            <div class="content">
                <h3>问题：${session.getValue().lastQuestion}</h3>
                答：${session.getValue().answer}
            </div>
        </#list>
    </div>

    <script>
        $('#ac')
            .accordion()
        ;
    </script>
</@layout.main>