//= require app/pvs/report/report_history
//= require app/pvs/common/rx_common
//= require app/pvs/common/rx_handlebar_ext.js

var signal = signal || {};
var ICSR_BY_CASE_REPORT = "ICSR_BY_CASE_CHARACTERISTICS"

signal.graph = (function () {

    var bindReportScreenEvents = function () {

        $("#reportType").on('change', function () {
            $('.graphReport').addClass('hide');
            $(".zoom").hide();
            var reportType = $(this).val();
            if (reportType === 'Memo Reports') {
                $("#memo-report-name").removeClass('hide');
            } else {
                $("#memo-report-name").addClass('hide');
            }
        });

        $(".generate-graph-report").click(function () {
            var reportType = $("#reportType").val();
            if (reportType == ICSR_BY_CASE_REPORT && $("#dataSource").val() != "eudra"){
                showMessage('ICSRs by Case Characteristics report is supported only for the EVDAS data source. ', 'error',false);
                return false;
            }

            $(this).prop('disabled', true);
            var data = {};
            if ($('input[name=optradio]:checked').val() === "group") {
                data.productGroupIds = $("#productGroups").val();
            } else {
                data.productSelection = $("#productSelection").val();
            }
            data.productGroupSelection = $("#productGroupSelection").val();
            data.isMultiIngredient = $("#isMultiIngredient").val();
            data.reportType = reportType;
            data.dataSource = $("#dataSource").val();
            data.reportName = $("#reportName").val();
            data.socList = [];
            data.socListName = [];
            $('#socListForCasesByReactionGroup :checked').each(function () {
                data.socList.push($(this).val());
                data.socListName.push($(this).next().html());
            });
            data = getDateRangeData(data);

            var url = data.reportType === ICSR_BY_CASE_REPORT ? fetchICSRsReportUrl : requestReportRestUrl;

            $.ajax({
                type: "POST",
                url: url,
                dataType: "json",
                data: data,
                async: false,
                beforeSend: function () {
                    $("#report-generating").removeClass('hide');
                },
                success: function (response) {
                    if (data.reportType === ICSR_BY_CASE_REPORT) {
                        showGraph(response);
                        $(".zoom").show();
                        $("#reportHistoryId").val(response["reportHistoryId"])
                    } else {
                        showMessage(response.message, 'success',true);
                        $(".zoom").hide();
                        $("#reportHistoryId").val("");
                    }
                },
                error: function (err) {
                    $("#notification").html(err.responseText);
                },
                complete: function () {
                    $("#report-generating").addClass('hide');
                    $(".generate-graph-report").prop('disabled', false);
                }
            });
        });

        $("#refreshCasesByReactionGroup").click(function () {
            var data = {};
            if ($('input[name=optradio]:checked').val() === "group") {
                data.productGroupIds = $("#productGroups").val();
            } else {
                data.productSelection = $("#productSelection").val();
            }
            //multi-ingredient-set value for flaf
            data.reportType = $("#reportType").val();
            data.dataSource = $("#dataSource").val();
            data.reportName = $("#reportName").val();
            data.socList = [];
            $('#socListForCasesByReactionGroup :checked').each(function () {
                data.socList.push($(this).val());
            });
            data = getDateRangeData(data);
            if (data.socList.length > 0) {
                $.ajax({
                    type: "POST",
                    url: fetchReactionGroupReportUrl,
                    dataType: "json",
                    data: data,
                    beforeSend: function () {
                        $("#refreshCasesByReactionGroup").addClass("fa-spin");
                    }, success: function (response) {
                        signal.graphReport.prepareReactionGroupChart(response['reactionGroup']);
                        $("#refreshCasesByReactionGroup").removeClass("fa-spin");
                    },
                    error: function (err) {
                        $("#notification").html(err.responseText);
                        $("#refreshCasesByReactionGroup").removeClass("fa-spin");
                    }
                });
            }
        });

        $("#refreshCasesByReaction").click(function () {
            var data = {};
            if ($('input[name=optradio]:checked').val() === "group") {
                data.productGroupIds = $("#productGroups").val();
            } else {
                data.productSelection = $("#productSelection").val();
            }
            //multi-ingedient-set value for flag
            data.reportType = $("#reportType").val();
            data.dataSource = $("#dataSource").val();
            data.reportName = $("#reportName").val();
            data.selectedSOCIdForReaction = $("#groups-for-reaction").val();
            data.ptList = [];
            $('#socListForCasesByReaction :checked').each(function () {
                data.ptList.push($(this).val());
            });
            data = getDateRangeData(data);
            if (data.ptList.length > 0) {
                $.ajax({
                    type: "POST",
                    url: fetchReactionReportUrl,
                    dataType: "json",
                    data: data,
                    beforeSend: function () {
                        $("#refreshCasesByReaction").addClass("fa-spin");
                    }, success: function (response) {
                        signal.graphReport.prepareReactionChart(response['reaction']);
                        $("#refreshCasesByReaction").removeClass("fa-spin");
                    },
                    error: function (err) {
                        $("#notification").html(err.responseText);
                        $("#refreshCasesByReaction").removeClass("fa-spin");
                    }
                });
            }
        });

        $("#groups-for-reaction").change(function () {
            var soc = $(this).val();
            $(".reactionRowLabel").html("Refreshing Reactions...");
            $(".reactionRow").html('');
            $.ajax({
                url: "getPTsFromSoc",
                data: {
                    soc: soc
                },
                success: function (result) {
                    $(".reactionRowLabel").html("Reactions");
                    var pt_list_content = signal.utils.render('pt_list', result);
                    $(".reactionRow").html(pt_list_content);
                },
                error: function () {
                }
            });
        });

        $("#saveICSRs").click(function () {
            var data = {};
            if (!$("#refreshCasesByReactionGroup").hasClass("fa-spin") && !$("#refreshCasesByReaction").hasClass("fa-spin")) {
                if ($('input[name=optradio]:checked').val() === "group") {
                    data.productGroupIds = $("#productGroups").val();
                } else {
                    data.productSelection = $("#productSelection").val();
                }
                data.selectedSOCForReaction = $("#groups-for-reaction :selected").text();
                data.selectedSOCIdForReaction = $("#groups-for-reaction").val();
                data.ptList = [];
                data.ptListName = [];
                $('#socListForCasesByReaction :checked').each(function () {
                    data.ptList.push($(this).val());
                    data.ptListName.push($(this).next().html());
                });
                data.socList = [];
                data.socListName = [];
                $('#socListForCasesByReactionGroup :checked').each(function () {
                    data.socList.push($(this).val());
                    data.socListName.push($(this).next().html());
                });
                data.reportHistory = $("#reportHistoryId").val();
                $('a[href="/signal/report/view?reportHistoryId=' + data.reportHistory + '"]').parent().html('<i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="Generating"></i>');
                $.ajax({
                    type: "POST",
                    url: saveICSRsReportHistoryUrl,
                    dataType: "json",
                    data: data,
                    success: function (response) {
                        showMessage(response.message, 'success',true);
                    },
                    error: function (err) {
                        $("#notification").html(err.responseText);
                    }
                });
            }
        });

        $("#socListForCasesByReactionGroup INPUT[type='checkbox']").change(function () {
            if ($("#socListForCasesByReactionGroup :checked").size() > 0) {
                $("#refreshCasesByReactionGroup").removeClass("fa-disabled");
            } else {
                $("#refreshCasesByReactionGroup").addClass("fa-disabled");
            }
        });
        $("#deselectAllReactionGroups").click(function () {
            $('#socListForCasesByReactionGroup INPUT[type="checkbox"]').each(function () {
                $(this).prop('checked', false)
            });
            $("#refreshCasesByReactionGroup").addClass("fa-disabled");
        });

        $("#selectAllReactionGroups").click(function () {
            $('#socListForCasesByReactionGroup INPUT[type="checkbox"]').each(function () {
                $(this).prop('checked', true)
            });
            $("#refreshCasesByReactionGroup").removeClass("fa-disabled");
        });


        $("#socListForCasesByReaction INPUT[type='checkbox']").change(function () {
            if ($("#socListForCasesByReaction :checked").size() > 0) {
                $("#refreshCasesByReaction").removeClass("fa-disabled");
            } else {
                $("#refreshCasesByReaction").addClass("fa-disabled");
            }
        });
        $("#deselectAllReactions").click(function () {
            $('#socListForCasesByReaction INPUT[type="checkbox"]').each(function () {
                $(this).prop('checked', false)
            });
            $("#refreshCasesByReaction").addClass("fa-disabled");
        });
        $("#selectAllReactions").click(function () {
            $('#socListForCasesByReaction INPUT[type="checkbox"]').each(function () {
                $(this).prop('checked', true)
            });
            $("#refreshCasesByReaction").removeClass("fa-disabled");
        });

        $("#reportHistoryTab").click(function () {
            $("#notification").html("");
            signal.reportHistory.refresh_report_history_table();
        });
        bindDateRangeDates();
    };

    var bindDateRangeDates = function () {

        var from = null;
        var to = null;
        $('#datePickerFromDiv').datepicker({
            allowPastDates: true,
            formatDate: function (date) {
              return moment(date).format(DATE_DISPLAY);
            }
        }).on('changed.fu.datepicker', function (evt, date) {
            from = date;
        }).click(function (evt) {
            from = $('#datePickerFromDiv').datepicker('getDate');
        });

        $('#datePickerToDiv').datepicker({
            allowPastDates: true,
            formatDate: function (date) {
               return moment(date).format(DATE_DISPLAY);
            }
        }).on('changed.fu.datepicker', function (evt, date) {
            to = date;
        }).click(function () {
            to = $('#datePickerToDiv').datepicker('getDate');
        });

    };

    $("#dateRangeStart").focusout(function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()))
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
        document.getElementById('dateRangeStartAbsolute').value=moment($(this).val(),['DD/MM/YYYY','DD-MMM-YYYY',]).utc($(this).val()).format(REPORT_GENERATE_DATE_FORMAT);
    });

    $("#dateRangeEnd").focusout(function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()))
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
        document.getElementById('dateRangeEndAbsolute').value=moment($(this).val(),['DD/MM/YYYY','DD-MMM-YYYY']).utc($(this).val()).format(REPORT_GENERATE_DATE_FORMAT);
    });


    var showMessage = function (message, type, refTable = true) {
        var data = {
            notification: {
                type: type,
                message: message
            }
        };
        var data_content = signal.utils.render('notifications', data);
        $("#notification").html(data_content);
        if(refTable==true)
        signal.reportHistory.refresh_report_history_table();
    };

    var getDateRangeData = function (obj) {
        var dateRangeEnumVal = $("#dateRangeEnum").val();
        if (dateRangeEnumVal === 'CUSTOM') {
            obj.startDate = $("#dateRangeStart").val();
            obj.endDate = $("#dateRangeEnd").val();
            obj.dateRangeType = dateRangeEnumVal;
        } else if (dateRangeEnumVal === 'CUMULATIVE') {
            obj.dateRangeType = dateRangeEnumVal
        } else {
            obj.dateRangeType = dateRangeEnumVal;
            obj.relativeValue = $("#relativeDateRangeValue").val()
        }
        return obj;
    };

    var showGraph = function (result) {
        $('.graphReport').removeClass('hide');

        signal.graphReport.prepareIndividualCasesChart(result['individualCasesChart']);
        signal.graphReport.prepareIndividualCasesByReactionGroupChart(result['individualCasesByReactionGroup']);
        signal.graphReport.prepareReactionGroupChart(result['reactionGroup']);
        if (result['reaction']) {
            signal.graphReport.prepareReactionChart(result['reaction']);
        }
    };

    return {
        bindReportScreenEvents: bindReportScreenEvents,
        showGraph: showGraph
    }

})();
$(document).ready(function() {
    $('#productModal').on('show.bs.modal', function(){
        $("#memo-report-generation-note").removeClass('hide');
    });
});

