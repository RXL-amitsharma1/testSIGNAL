
<%@ page import="com.rxlogix.config.ActionType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="action.type.create" default="Create ActionType" /></title>
    </head>
    <rx:container title="Create an Action Type">
        <div class="nav bord-bt">
            <span class="menuButton"><g:link class="list btn btn-primary" action="list"><g:message code="action.type.list" default="Action Type List" /></g:link></span>
        </div>
        <div class="body">
            <g:if test="${flash.error}">
                <div class="alert alert-danger alert-dismissible" role="alert" style="word-break: break-all">
                    <button type="button" class="close" data-dismiss="alert">
                        <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
                        <span class="sr-only"><g:message code="default.button.close.label" /></span>
                    </button>
                    <g:if test="${flash.error}">
                        <g:if test="${flash.error.contains( '<linkQuery>' )}">
                            ${flash.error.substring( 0, flash.error.indexOf( '<linkQuery>' ) )}
                            <a href="${flash.error.substring( flash.error.indexOf( '<linkQuery>' ) + 11 )}"><g:message
                                    code="see.details"/></a>
                        </g:if>
                        <g:else>
                            <g:if test="${flash.error instanceof List}">
                            <g:each in="${flash.error}" var="field">
                                ${raw(field)}<br>
                            </g:each>
                            </g:if>
                            <g:else>
                                ${raw(flash.error)}<br>
                            </g:else>
                        </g:else>
                    </g:if>
                </div>
            </g:if>
            <g:form action="save" method="post" name="actionTypeCreateForm" id="actionTypeCreateForm">
                <g:render template="form" model="[actionTypeInstance: actionTypeInstance]" />
            </g:form>
        </div>
    </rx:container>
</html>
<script>
    $(document).ready(function () {
        addCountBoxToInputField(255, $('#description'));
        disableCreateButton();
    });
    function disableCreateButton() {
        $('.actionTypeSaveButton').on('click', function () {
            $(this).attr('disabled', true);
            $('#actionTypeCreateForm').submit();
        });
    }
</script>
