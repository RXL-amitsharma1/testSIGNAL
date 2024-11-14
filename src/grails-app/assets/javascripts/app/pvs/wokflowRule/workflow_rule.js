$(document).ready(function () {
    var initWorflowRuleTable = function () {
        var columns = create_worflow_rule_table_columns();
        var table = $('#worflowRuleTable').DataTable({
            sPaginationType: "bootstrap",
            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            search: {
                smart: false
            },
            "ajax": {
                "url": workflowListUrl,
                "dataSrc": ""
            },
            "oLanguage": {
                "sProcessing": '<div class="grid-loading"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>',
               "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            drawCallback: function (settings) {
                tagEllipsis($("#worflowRuleTable"));
                colEllipsis();
                webUiPopInit();
            },
            "fnInitComplete": function () {
                if (isAdmin) {
                    var buttonHtml = '<a class="btn btn-primary m-r-15" href="' + createUrl + '" >' + "New Workflow Rule" + '</a> ';
                    var $divToolbar = $('#worflowRuleTable_filter');
                    $divToolbar.prepend(buttonHtml);
                }
                addGridShortcuts(this);
            },
            "aaSorting": [],
            "bLengthChange": true,
            "bStateSave": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "aoColumns": columns,
            "scrollX":true,
            "scrollY":'calc(100vh - 251px)',
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });

    };

    var create_worflow_rule_table_columns = function () {
        var aoColumns = [
            {
                "className":"col-min-150 col-max-200",
                "mRender": function (data,type,row) {
                    return '<a href="' + 'edit' + '?' + 'id=' + row.id + '">' + encodeToHTML(row.name)+ '</a>';
                }
            },
            {
                "mData": "description",
                "className":"col-min-150 col-max-300 cell-break textPre",
                "mRender": function(data, type, row) {
                    var description= ''
                    if(row.description){
                        description = "<span>" + addEllipsisForDescriptionText(encodeToHTML(row.description)) + "</span>";
                    }
                    return description;
                }
            },
            {
                "mData":"incomingDisposition",
                "className":"col-min-150 col-max-200"
            },
            {
                "mData": "targetDisposition",
                "className":"col-min-150 col-max-200"
            },
            {
                "mData": "workflowGroups",
                "className":"col-min-150 col-max-200"
            },
            {
                "mData": "allowedUserGroups",
                "className":"col-min-150 col-max-200"
            },
            {
                "mRender": function (data, type, row) {
                    if (row.display) {
                        return "<span>Yes</span>"
                    } else {
                        return "<span>No</span>"
                    }
                }
            }
        ];

        return aoColumns
    };


    initWorflowRuleTable();

});