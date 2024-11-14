//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js


var sharedWithModalShow = false;
$(document).ready(function () {

    insertFilterDropDown($("#search-control"), $("#dropdownMenu1"));
    var genAlertNameLink = function (value, id) {
        return function () {
            return '<a href="' + (detailsUrl + '?' + 'callingScreen=review&configId=' + id) + '">' + escapeHTML(value) + '</a>'
        }
    };

    var initTable = function (listConfigUrl) {

        var table = $('.simple-alert-table').DataTable({
            "sPaginationType": "bootstrap",
            "language": {
                "url": "/assets/i18n/dataTables_" + userLocale + ".json"
            },

            fnDrawCallback: function () {
                removeBorder($("#alertsFilter"));
                colEllipsis();
                webUiPopInit();
                closeInfoPopover();
                showInfoPopover();
                closePopupOnScroll();
            },
            fnInitComplete: function () {
                showInfoPopover();
            },
            ajax: {
                "url": listConfigUrl,
                "dataSrc": "aaData",
                data: function (args) {
                    let selectedData = $('#alertsFilter').val();
                    if(selectedData) {
                        args.selectedAlertsFilter = JSON.stringify(selectedData);
                    }
                    else {
                        let retainedData = $("#filterVals").val();
                        if(retainedData=="" || retainedData=="null"){
                            retainedData = null;
                        }
                        else {
                            retainedData = JSON.parse(retainedData);
                        }
                        args.selectedAlertsFilter = JSON.stringify(retainedData);

                    }
                    return {
                        "args": JSON.stringify(args),
                    };
                }
            },
            processing: true,
            serverSide: true,
            "order": [[5, 'desc']],
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
            "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
            columns: [
                {
                    "mData": "name",
                    "name": "name",
                    "mRender": function (data, type, row) {
                        return genAlertNameLink(row.name, row.id)
                    },
                    "className": 'col-min-300 col-max-650 cell-break'
                },
                {
                    "mData": "searchString",
                    "name": "searchString",
                    "className": 'col-min-150 col-max-300 cell-break',
                },
                {
                    "mData": "dateRange",
                    "orderable": false,
                    "bSortable": false,
                    "className": 'col-min-250 col-max-300 cell-break'
                },
                {
                    "mData": "selectedDatasource",
                    "name": "selectedDatasource",
                    "className": 'col-min-100'
                }, {
                    "mData": "lastUpdated",
                    "name": "lastUpdated",
                    "className": 'col-min-100'
                }, {
                    "mData": "dateCreated",
                    "name": "dateCreated",
                    "className": 'col-min-100'
                }, {
                    "mData": "action",
                    "bSortable": false,
                    "aTargets": ["id"],
                    "className": 'col-min-150',
                    "mRender": function (data, type, row) {

                        if (row.IsShareWithAccess) {
                            var actionButton = '<div class="hidden-btn btn-group hidden-btn dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="" id="' + row.id + '" data-toggle="modal" data-target="#sharedWithModal">' + $.i18n._('labelShare') + '</a> \
                            </div>'
                            return actionButton;
                        }
                        return ""

                    }
                }
            ],
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });

        return table
    };

    var table = initTable(listConfigUrl);
    actionButton('#simpleCaseAlerts');

    loadTableOption('#simpleCaseAlerts');

    signal.alerts_utils.initializeShareWithSelect2();
    signal.alerts_utils.initializeShareWithValues();

    $('#alertsFilter').on('change', function() {
        if($('#alertsFilter').val()==null) {
            $("#filterVals").val("")
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

Handlebars.registerHelper('option', function (value, label, selectedValue) {

    var selectedProperty = value == selectedValue ? 'selected="selected"' : '';
    return new Handlebars.SafeString(
        '<option value="' + value + '"' + selectedProperty + '>' + label + "</option>");
});
