var signal = signal || {};

signal.reportHistory = (function () {

    var reportHistoryTable = null;

    var init_report_history_table = function (url) {
        if (typeof reportHistoryTable != "undefined" && reportHistoryTable != null) {
            refresh_report_history_table();
        } else {
            reportHistoryTable = $('#reportsHistory').DataTable({
                "sPaginationType": "bootstrap",
                "ajax": {
                    "url": url,
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
                "aaSorting": [[5, "desc"]],
                "bLengthChange": true,
                "iDisplayLength": 50,
                "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
                "fnDrawCallback": function (oSettings) {
                    $(".downloadReport li").click(function () {
                        var reportId = $(this).parent().data("report-id");
                        var outputType = $(this).data("type");
                        var url = downloadReportUrl + "?id=" + reportId + "&type=" + outputType
                        window.location.href = url;
                    })
                },
                "aoColumns": [
                    {
                        "mData": "reportName",
                        'className': 'dt-center'
                    },
                    {
                        "mData": "dataSource",
                        'className': 'dt-center'
                    },
                    {
                        "mData": "productName",
                        'className': 'dt-center'
                    },
                    {
                        "mData": "reportType",
                        'className': 'dt-center'
                    },
                    {
                        "mData": "summaryDateRange",
                        'className': 'dt-center'
                    },
                    {
                        "mData": "generatedOn",
                        'className': 'dt-center',
                        "width": "15%"
                    },
                    {
                        "width": "5%",
                        "mRender": function (data, type, row) {
                            if (row.reportType !== "ICSRs by Case Characteristics") {
                                if (row.reportGenerated) {
                                    return '<li class="dropdown downloadReport" style="list-style: none;">' +
                                        '<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false" title="Export to"><i class="mdi mdi-export font-22 theme-color" aria-hidden="true"></i> <span class="caret hidden"></span></a>' +
                                        '<ul class="dropdown-menu dropdown-menu-right" style="min-width: 70px" data-report-id="' + row.downloadId + '">' +
                                        '<li data-type="PDF"><a href="#"><img src="/signal/assets/pdf-icon.jpg" class="pdf-icon" height="16" width="16">Save as PDF</a></li>' +
                                        '<li data-type="XLSX"><a href="#"><img src="/signal/assets/excel.gif" class="pdf-icon" height="16" width="16">Save as Excel</a></li>' +
                                        '<li data-type="DOCX"><a href="#"><img src="/signal/assets/word-icon.png" class="pdf-icon" height="16" width="16">Save as Word</a></li>' +
                                        '</ul>' +
                                        '</li>'
                                }  else if (row.reportGenerated == null) {
                                    return 'Failed'
                                } else {
                                    return '<i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="Generating" ></i>'
                                }
                            } else {
                                if (row.reportGenerated) {
                                    return '<a href="' + viewICSRReportUrl + '?reportHistoryId=' + row.downloadId + '" role="button" aria-haspopup="true" aria-expanded="false" target="_blank" title="Show report"><i class="mdi mdi-file-chart theme-color font-20"></i></a>'
                                } else if (row.reportGenerated == null) {
                                    return 'Failed'
                                } else {
                                    return '<i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="Generating" ></i>'
                                }
                            }
                        }
                    }
                ],
                columnDefs: [{
                    "targets": '_all',
                    "render": $.fn.dataTable.render.text()
                }]
            });
        }
    };

    var clear_report_history_table = function () {
        if (typeof reportHistoryTable != "undefined" && reportHistoryTable != null) {
            reportHistoryTable.clear().draw();
        }
    };

    var refresh_report_history_table = function () {
        if (typeof reportHistoryTable != "undefined" && reportHistoryTable != null) {
            $("#report-history-fetch").show();
            clear_report_history_table();
            reportHistoryTable.ajax.reload(function(){
                $("#report-history-fetch").hide();
            });
        }
    };

    return {
        init_report_history_table: init_report_history_table,
        clear_report_history_table: clear_report_history_table,
        refresh_report_history_table: refresh_report_history_table
    }

})();
