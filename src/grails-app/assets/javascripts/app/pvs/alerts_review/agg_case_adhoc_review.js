//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js

var sharedWithModalShow = false;
var emailToModalShow = false;

$(document).ready(function () {

    insertFilterDropDown($("#search-control"), $("#dropdownMenu1"));
    var genAlertNameLink = function (value, id) {
        return function () {
            return '<a href="' + (detailsUrl + '?' + 'callingScreen=review&' +
                    signal.utils.composeParams({configId: id})) + '">' + escapeHTML(value) + '</a>'
        }
    };

    var initTable = function (table_rest_url) {
        signal.alerts_utils.get_priorities();
        signal.alerts_utils.get_workflowStates();

        var table = $('.simple-alert-table').DataTable({
            "sPaginationType": "bootstrap",
            "bServerSide": true,
            "language": {
                "url": "/assets/i18n/dataTables_" + userLocale + ".json",
                "sProcessing": '<div class="grid-loading"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>',
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
                            url: generateCaseSeriesUrl + "?id=" + id + "&seriesName=" + encodeToHTML(seriesName),
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
            "aaSorting": [[6, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 25,
            "bProcessing": true,
            "bServerSide": true,
            "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
            columns: alert_review_column_data(),
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });
        return table
    };
    function updateQueryStringParameter(uri, key, value) {
        var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
        var separator = uri.indexOf('?') !== -1 ? "&" : "?";
        if (uri.match(re)) {
            return uri.replace(re, '$1' + key + "=" + value + '$2');
        }
        else {
            return uri + separator + key + "=" + value;
        }
    }

    var alert_review_column_data = function () {
        var aoColumns = [
            {
                "mData": "name",
                "sWidth": "15%",
                "mRender": function (data, type, row) {
                    return genAlertNameLink(row.name, row.id)
                },
                "className": 'check-role-access'
            },
            {
                "mData": "description",
                'className': 'col-min-300 col-max-500 cell-break',
                "mRender": function (data, type, row) {
                    if (row.description) {
                        return  addEllipsis(row.description)
                    } else {
                        return "-"
                    }
                },
            },
            {
                "mData": "productSelection",
                "className": 'col-min-150 col-max-300 cell-break',
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return addEllipsis(row.productSelection);
                }
            },
            {
                "mData": "selectedDataSource",
                "mRender": function (data, type, row) {
                    return "<span>" + row.dataSource + "</span>"
                }
            }
        ];
        if (appType == ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
            aoColumns.push({
                "mData": "caseCount",
                "bSortable": true
            });
            aoColumns.push({
                "mData": "alertPriority"
            });
        } else if (appType == ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
            aoColumns.push({
                "mData": "pecCount",
                "bSortable": true
            });
            aoColumns.push({
                "mData": "dateRagne",
                "bSortable": false
            });

        }
        aoColumns.push({
            "mData": "lastModified"
        });

        aoColumns.push({
            "mData": "lastExecuted"
        });

        aoColumns.push({
            "bSortable": false,
            "aTargets": ["id"],
            "className": 'col-min-150',
            "mRender": function (data, type, row) {
                if (row.IsShareWithAccess) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" id="delete-pop-up" align="center"> \
                            <a class="btn btn-success btn-xs" href="" id="' + row.id + '" data-toggle="modal" data-target="#sharedWithModal">' + $.i18n._('labelShare') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" id="dropdwn" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" class="del-aod-alert"  href="#" data-instancetype="alert" data-toggle="modal" data-target="#deleteModal" data-instanceid="' + row.id +  '">' + $.i18n._('labelDelete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
                return '';
            }
        });
        return aoColumns
    };


    var table = initTable(listConfigUrl);
    actionButton('#simpleCaseAlerts');

    loadTableOption('#simpleCaseAlerts');
    signal.alerts_utils.initializeShareWithSelect2();
    signal.alerts_utils.initializeShareWithValues();
    $('.outside').hide();

    $(document).on("click",".check-role-access",function (e) {
        if(typeof rolesGranted !=="undefined" && !checkForReviewAccess(table.row($(this).closest("tr")).data().dataSource, rolesGranted)){
            e.preventDefault()
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
        }
    });

    $(document).on("click",".btn btn-default btn-xs dropdown-toggle, #delete-pop-up,#dropdwn",function (e) {
        if($('#delete-pop-up').hasClass("dropup")) {
            $("#delete-pop-up").css('z-index', 1050);
            $("#delete-pop-up").css('position', 'fixed');
        }
        else{
            $("#delete-pop-up").css('z-index', '');
            $("#delete-pop-up").css('position', '');
        }
    });

    $('#alertsFilter').on('change', function() {
        if($('#alertsFilter').val()==null) {
            $("#filterVals").val("");
        }
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
    return hasAccess
}

var filters = [
    function (data) {
        var alertName = data[1];
        var filterValue = $('#alertNameFilter').val();
        if (filterValue) {
            var regx = new RegExp('.*' + filterValue + '.*', 'i');
            return regx.test(alertName)
        }

        return true
    },

    function (data) {
        return true
    }
];


$.fn.dataTable.ext.search.push(
    function (settings, data, dataIndex) {
        var fv = true;

        for (var i = 0; i < filters.length; i++) {
            var nextFv = filters[i](data);
            fv &= nextFv;
        }
        return fv;
    }
);

Handlebars.registerHelper('option', function (value, label, selectedValue) {
    var selectedProperty = value == selectedValue ? 'selected="selected"' : '';
    return new Handlebars.SafeString(
        '<option value="' + value + '"' + selectedProperty + '>' + label + "</option>");
});