//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js

$(document).ready( function () {

    insertFilterDropDown($("#search-control"), $("#dropdownMenu1"));
    var genAlertNameLink = function(value, id) {
        return function() {
            return '<a href="' + (detailsUrl + '?' + 'callingScreen=review&'+ signal.utils.composeParams({configId: id})) + '">' + escapeHTML(value) + '</a>'
        }
    };

    var initTable = function(table_rest_url) {

        signal.alerts_utils.get_priorities()
        signal.alerts_utils.get_workflowStates()

        var table = $('#evdasAlerts').DataTable({

            "sPaginationType": "bootstrap",
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            fnInitComplete: function () {
                addGridShortcuts('#evdasAlerts');
                showInfoPopover();
            },
            fnDrawCallback: function (){
                removeBorder($("#alertsFilter"));
                colEllipsis();
                webUiPopInit();
                closeInfoPopover();
                showInfoPopover();
                closePopupOnScroll()
            },

            "ajax": {
                "url": table_rest_url,
                data : function (d) {
                    let selectedData = $('#alertsFilter').val();
                    if(selectedData) {
                        d.selectedAlertsFilter = JSON.stringify(selectedData);
                    }
                    else {
                        let retainedData = $("#filterVals").val();
                        if(retainedData=="" || retainedData=="null"){
                            retainedData = null;
                        }
                        else {
                            retainedData = JSON.parse(retainedData);
                        }
                        d.selectedAlertsFilter = JSON.stringify(retainedData);

                    }                },
                "dataSrc": "aaData"
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
            "bLengthChange": true,
            "iDisplayLength": 25,
            "processing": true,
            "oLanguage": {
                "sProcessing": '<div class="grid-loading"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>',
                 "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                'sSearch': '',
            },

            "bServerSide": true,
            "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
            "aaSorting": [[8, "desc"]],
            "aoColumns": [
                {
                    "mData": "name",
                    'className': 'col-min-150 cell-break',
                    "mRender": function(data, type, row) {
                        return genAlertNameLink(row.name, row.id)
                    }
                },
                {
                    "mData": "description",
                    'className': 'col-min-250 col-max-300 cell-break',
                    "mRender": function (data, type, row) {
                        var result = "";
                        result = escapeHTML(row.description);
                        result = result.replaceAll('"','&quot;');
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += result
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(result) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "productSelection",
                    'className': '',
                    'bSortable': false
                },
                {
                    "mData" : "caseCount",
                    'className': 'col-min-50',
                    'bSortable':true
                },
                {
                    "mData" : "newCases",
                    'className': '',
                    "bSortable":true
                },
                {
                    "mData" : "closedCaseCount",
                    'className': '',
                    'bSortable':true
                },
                {
                    "mData" : "priority",
                    'className': '',
                    'bSortable':false
                },
                {
                    "mData" : "dateRagne",
                    "className" : "col-min-100",
                    'bSortable' : false
                },
                {
                    "mData" : "lastModified",
                    "className" : ""
                },
                {
                    "mData" : "lastExecuted",
                    "className" : ""
                },
                {
                    "bSortable": false,
                    "aTargets": ["id"],
                    "className": 'col-min-150',
                    "mRender": function (data, type, row) {
                        if (row.IsShareWithAccess) {
                            var actionButton = '<div class="hidden-btn btn-group hidden-btn dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="" id="' + row.id + '" data-toggle="modal" data-target="#sharedWithModal">' + $.i18n._('labelShare') + '</a> \
                            </div>';
                            return actionButton;
                        }
                        return ""
                    }
                }
            ],
            scrollX: true,
            scrollY:'50vh',
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });
        return table
    };

    var table = initTable(listConfigUrl);
    $('#evdasAlerts_filter .dt-search').attr('placeholder','Search');
    actionButton('#evdasAlerts');
    loadTableOption('#evdasAlerts');
    signal.alerts_utils.initializeShareWithSelect2();
    signal.alerts_utils.initializeShareWithValues();

    $('#alertsFilter').on('change', function() {
        if($('#alertsFilter').val()==null) {
            $("#filterVals").val("");
        }
        let selectedData = $('#alertsFilter').val();
        let selectedAlertsFilter = JSON.stringify(selectedData);
        $.ajax({
            url: sessionRefreshUrl,
            async: false,
            data: {selectedAlertsFilter: selectedAlertsFilter},
            success: function (result) {
                console.log("success");
            },
            error: function (err) {
                console.log("error");
            }
        })
        $("#evdasAlerts").DataTable().ajax.reload();
    });
    var _searchTimer = null
    $('#custom-search').keyup(function (){
        clearInterval(_searchTimer)
        _searchTimer = setInterval(function (){
            table.search($('#custom-search').val()).draw() ;
            clearInterval(_searchTimer)
        },1500)

    });
});

