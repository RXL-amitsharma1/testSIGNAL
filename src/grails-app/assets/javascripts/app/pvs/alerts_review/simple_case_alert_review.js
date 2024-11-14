//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js

var sharedWithModalShow = false;
var emailToModalShow = false;

$(document).ready(function () {

    insertFilterDropDown($("#search-control"), $("#dropdownMenu1"));
    $(document).on("click",".openShareWithModal",function () {
        let isAutoSharedWith = $(this).closest("tr").find(".isAutoSharedWith").val();
        isAutoSharedWith = (isAutoSharedWith === "true")
        bindShareWith2WithData($("#sharedWith"),"/signal/user/searchShareWithUserGroupList","",true, isAutoSharedWith)
    });

    var genAlertNameLink = function (value, id) {
        return function () {
            return '<a style="white-space: pre;" href="' + (detailsUrl + '?' + 'callingScreen=review&' +
                    signal.utils.composeParams({configId: id})) + '">' + encodeToHTML(value) + '</a>'
        }
    };

    var initTable = function (table_rest_url) {
        signal.alerts_utils.get_priorities();
        signal.alerts_utils.get_workflowStates();
        if(appType===ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT)
            var sortColumnIndex=10;
        else if(ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT)
            var sortColumnIndex=9;

        var table = $('.simple-alert-table').DataTable({
            "sPaginationType": "bootstrap",
            "language": {
                "url": "/assets/i18n/dataTables_" + userLocale + ".json",
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            fnDrawCallback: function (){
                removeBorder($("#alertsFilter"));
                colEllipsis();
                webUiPopInit();
                closeInfoPopover();
                showInfoPopover();
                closePopupOnScroll();
            },
            fnInitComplete: function () {

                $(".generate-case-series").unbind().click(function () {
                    var id = $(this).attr("data-id");
                    var name = $(this).attr("data-name");
                    var caseSeriesModal = $("#case-series-modal");
                    caseSeriesModal.modal("show");
                    caseSeriesModal.find("#case-series-name").val(name + id);
                    caseSeriesModal.find(".save-case-series").unbind().click(function () {
                        var seriesName = caseSeriesModal.find("#case-series-name").val();
                        $.ajax({
                            url: generateCaseSeriesUrl + "?id=" + id + "&seriesName=" + encodeToHTML(seriesName) + "&isArchived=" + isArchived,
                            success: function (result) {
                                caseSeriesModal.modal("hide");
                            },
                            error: function (err) {
                                alert("Case Series Saving Failed.");
                                caseSeriesModal.modal("hide");
                            }
                        })
                    })
                });
                addGridShortcuts('#simpleCaseAlerts');
                showInfoPopover();
            },

            ajax: {
                "url": table_rest_url,
                cache: false,
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

                    }

                },
                "dataSrc": "aaData"
            },
            "aaSorting": [[sortColumnIndex, "desc"]],
            "processing": true,
            "oLanguage": {
               "sProcessing": '<div class="grid-loading"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>',
                "sZeroRecords": "No matching records found", "sEmptyTable": "No data available in table",
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            "bServerSide":true,
            "bLengthChange": true,
            "iDisplayLength": 25,
            "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
            columns: alert_review_column_data(),
            scrollX: true,
            columnDefs: [{
                "defaultContent": '',
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });

        return table
    };

    var alert_review_column_data = function () {
        var aoColumns = [
            {
                "mData": "name",
                "mRender": function (data, type, row) {
                    return genAlertNameLink(row.name, encodeToHTML(row.id))
                },
                "className": 'col-min-300 col-max-650 cell-break check-role-access'
            },
            {
                "mData": "productSelection",
                "className": 'col-min-150 col-max-300 cell-break',
                "bSortable" : false,
                "mRender": function (data, type, row) {
                    if(row.productSelection) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += defaultRender(row.productSelection, true);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + defaultRender(row.productSelection, true) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                    else if(row.studySelection){
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += defaultRender(row.studySelection, true);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + defaultRender(row.studySelection, true) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                }
            },
            {
                "mData": "description",
                "mRender": function (data, type, row) {
                    if (row.description) {
                        var result = "";
                        result = escapeHTML(encodeToHTML(row.description));
                        result = result.replaceAll('"','&quot;');
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement+=result;
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="'+encodeToHTML(result)+'"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    } else {
                        return "-"
                    }
                },
                "className": 'col-min-250 col-max-300 cell-break'
            },
            {
                "mData": "selectedDataSource",
                "mRender": function (data, type, row) {
                    return "<span>" + row.dataSource + "</span>"
                },
                "className": 'col-min-100'
            }
        ];
        if (appType === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
            aoColumns.push({
                "mData": "caseCount",
                "className": 'col-min-100',
                "bSortable" : true
            });
            aoColumns.push({
                "mData": "newCases",
                "className": 'col-min-100',
                "bSortable" : true
            });
            aoColumns.push({
                "mData": "closedCaseCount",
                "className": 'col-min-100',
                "bSortable" : true
            });
            aoColumns.push({
                "mData": "alertPriority",
                "className": 'col-min-100',
                "bSortable" : false
            });
            aoColumns.push({
                "mData": "singleDateRange",
                "className": "col-min-150",
                "bSortable" : false
            });
        } else if (appType === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
            aoColumns.push({
                "mData": "pecCount",
                "className": 'col-min-100',
                "bSortable" : true
            });
            aoColumns.push({
                "mData": "closedPecCount",
                "className": 'col-min-100',
                "bSortable" : true
            });
            aoColumns.push({
                "mData": "alertPriority",
                "className": 'col-min-100',
                "bSortable" : false
            });
            aoColumns.push({
                "mData": "dateRagne",
                "className": "col-min-150",
                "bSortable" : false
            });

        }
        aoColumns.push({
            "mData": "lastModified",
            "className": 'col-min-100'
        });

        aoColumns.push({
            "mData": "lastExecuted",
            "className": 'col-min-100'
        });
            aoColumns.push({
                "mData": null,
                "bSortable": false,
                "aTargets": ["id"],
                "className": 'col-min-150',
                "mRender": function (data, type, row) {
                    if (row.IsShareWithAccess) {
                        var actionButton = '<div class="hidden-btn btn-group hidden-btn dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs openShareWithModal" href="" id="' + row.id + '" data-toggle="modal" data-target="#sharedWithModal">' + $.i18n._('labelShare') + '</a> \
                            </div>\
                            <input class="isAutoSharedWith hide" value="'+row.isAutoSharedWith+'">'
                        return actionButton;
                    }
                    return ""
                }
            });

        return aoColumns
    };


    var table = initTable(listConfigUrl);
    actionButton('#simpleCaseAlerts');

    loadTableOption('#simpleCaseAlerts');

    $('.outside').hide();

    signal.alerts_utils.initializeShareWithSelect2();
    signal.alerts_utils.initializeShareWithValues();
    $(document).on("click",".check-role-access",function (e) {
        if(typeof rolesGranted !=="undefined" && !checkForReviewAccess(table.row($(this).closest("tr")).data().dataSource, rolesGranted)){
            e.preventDefault()
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
        }
    });

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
        $(".simple-alert-table").DataTable().ajax.reload();
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

function checkForReviewAccess(dataSource, roles) {
    let hasAccess = false;
    if(roles.AllDatasources){
        hasAccess = true;
    } else if(dataSource.includes("FAERS") && roles.faers){
        hasAccess = true;
    }
    else if(dataSource.includes("VAERS") && roles.vaers){
        hasAccess = true;
    }
    else if(dataSource.includes("VigiBase") && roles.vigibase){
        hasAccess = true;
    }
    else if(dataSource.includes("JADER") && roles.jader){
        hasAccess = true;
    }
    return hasAccess
}

var filters = [
    function (data) {
        var alertName = data[1]
        var filterValue = $('#alertNameFilter').val()
        if (filterValue) {
            var regx = new RegExp('.*' + filterValue + '.*', 'i')
            return regx.test(alertName)
        }

        return true
    },

    function (data) {
        return true
    }
]


$.fn.dataTable.ext.search.push(
    function (settings, data, dataIndex) {
        var fv = true

        for (var i = 0; i < filters.length; i++) {
            var nextFv = filters[i](data)
            fv &= nextFv
        }
        return fv
    }
)

Handlebars.registerHelper('option', function (value, label, selectedValue) {
    var selectedProperty = value == selectedValue ? 'selected="selected"' : '';
    return new Handlebars.SafeString(
        '<option value="' + value + '"' + selectedProperty + '>' + label + "</option>");
});

function checkAndLoad(name) {
    $.ajax({
        url: sporfireValidationUrl + "?name=" + name,
        success: function (result) {
            window.open(result.url, '_blank');
        },
        error: function (err) {
            $("#spotfireNotReady").modal("show");
        }
    })
}