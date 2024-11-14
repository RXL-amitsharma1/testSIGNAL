$(document).ready(function () {
    var addReference = false;
    var addReferenceName;
    var referenceList=["AssessmentDetails","Charts","Seriousness_Counts_over_time","Age_Group_Over_time","Gender_Over_time","Country_Over_time","Case_Outcome","Source_Over_time"];
    $(".addReference").click(function () {
        addReferenceName = $(this).attr('data-name');
        if (referenceList.includes(addReferenceName)) {
            addReference = true;
            var modal =  $("#assesmentCommentModal");
            modal.find(".modal-title").text("Comments");
            modal.modal();
            modal.find("#assessmentNotes").val('');
            modal.find("#assessmentNotes").attr('placeholder','Write your comment here...');
        }
    });

    $("#assessmentCustomStartDate").focusout(function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
    });

    $("#assessmentCustomEndDate").focusout(function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
    });
    function getFormattedDate(date) {
        date = date.replace(/-/g, '/');
        date = date.replace(date.slice(3, 6), months[date.slice(3, 6)]);
        return date
    }
    var months= {'Jan':'01', 'Feb':'02', 'Mar':'03', 'Apr':'04', 'May':'05', 'Jun':'06', 'Jul':'07', 'Aug':'08', 'Sep':'09', 'Oct':'10', 'Nov':'11', 'Dec':'12'};
        $(document).on('click',"#assesmentCommentModal .btn.btn-primary.add-assessment-comment",function (e) {
            if(addReference) {
                var formdata = new FormData();
                if ($("#dateRange").val() === "CUSTOM") {
                    formdata.append("startDate", getFormattedDate($("#assessmentCustomStartDate").val()));
                    formdata.append("endDate", getFormattedDate($("#assessmentCustomEndDate").val()));
                }
                formdata.append('addReferenceName', addReferenceName);
                formdata.append('dateRange', $("#dateRange").val());
                formdata.append('productSelection', $("#productSelectionAssessment").val());
                formdata.append('productGroupSelection', $("#productGroupSelectionAssessment").val());
                formdata.append('eventSelection', $("#eventSelectionAssessment").val());
                formdata.append('eventGroupSelection', $("#eventGroupSelectionAssessment").val());
                formdata.append('signalId', signalId);
                formdata.append('outputFormat', "PDF");
                formdata.append('reportType', 'peber');
                formdata.append('description', $("#assessmentNotes").val());
                $.Notification.notify('success', 'top right', "Success", "Signal attachment save is in progress.", {autoHideDelay: 40000});
                $.ajax({
                    url: uploadSignalAssessmentReportUrl,
                    type: "POST",
                    mimeType: "multipart/form-data",
                    processData: false,
                    contentType: false,
                    data: formdata,
                    success: function (data) {
                        $.Notification.notify('success', 'top right', "Success", "Report has been added successfully.", {autoHideDelay: 40000});
                        $('#reference-table').DataTable().ajax.reload();
                        console.log(data);
                    },
                    error: function (data) {
                        $.Notification.notify('error', 'top right', "Error", "Adding assessment report failed.", {autoHideDelay: 100000});
                        $('#reference-table').DataTable().ajax.reload();
                    }
                });

                addReference = false;
            }
        });
    var executedIdArray = [];
    var signalId = $("#signalId").val();

    var getChartResults = function (executedId, chartName) {
        $.ajax({
            url: 'getChartData?executedId=' + executedId + "&signalId=" + signalId + "&chartName=" + chartName,
            success: function (result) {
                var chartId = "#" + chartName;
                $(chartId).highcharts(result.responseText.options);
            }
        })
    };

    var initChartDataGen = function (signalId, chartName) {
        var data = {};
        var eventSelection = $('#eventSelectionAssessment').val();
        var productSelection = $('#productSelectionAssessment').val();
        var isMultiIngredient = $("#isMultiIngredientAssessment").val();
        data['dataSource'] = $('#dataSources').val();
        data['dateRange'] = $('#dateRange').val();
        data['productSelection'] = productSelection;
        data['productGroupSelection'] = $('#productGroupSelectionAssessment').val();
        data['eventSelection'] = eventSelection;
        data['eventGroupSelection'] = $('#eventGroupSelectionAssessment').val();
        data['validatedSignal.id'] = $("#signalIdPartner").val();
        data['chartType'] = chartName;
        data['isMultiIngredient'] = isMultiIngredient;
        if ($("#dateRange").val() === "CUSTOM") {
            data['startDate'] = getFormattedDate($("#assessmentCustomStartDate").val());
            data['endDate'] = getFormattedDate($("#assessmentCustomEndDate").val());
        }
        $.ajax({
            type: "POST",
            url: graphReportRestUrl,
            data: data,
            success: function (response) {
                if (response.status) {
                    var result = response.data;
                    switch (chartName) {
                        case SIGNAL_CHARTS.AGE_GROUP :
                            age_group_data.options.series = result[SIGNAL_CHARTS.AGE_GROUP];
                            age_group_data.options.xAxis[0].categories = getCategoriesFromData(result[SIGNAL_CHARTS.AGE_GROUP][0]['data']);
                            signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.AGE_GROUP, age_group_data.options);
                            break;
                        case SIGNAL_CHARTS.SERIOUSNESS :
                            seriousness_data.options.series = result[SIGNAL_CHARTS.SERIOUSNESS];
                            seriousness_data.options.xAxis[0].categories = getCategoriesFromData(result[SIGNAL_CHARTS.SERIOUSNESS][0]['data']);
                            signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.SERIOUSNESS, seriousness_data.options);
                            break;
                        case SIGNAL_CHARTS.COUNTRY :
                            country_data.options.series = result[SIGNAL_CHARTS.COUNTRY];
                            country_data.options.xAxis[0].categories = getCategoriesFromData(result[SIGNAL_CHARTS.COUNTRY][0]['data']);
                            signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.COUNTRY, country_data.options);
                            break;
                        case SIGNAL_CHARTS.GENDER :
                            gender_data.options.series = result[SIGNAL_CHARTS.GENDER];
                            gender_data.options.xAxis[0].categories = getCategoriesFromData(result[SIGNAL_CHARTS.GENDER][0]['data']);
                            signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.GENDER, gender_data.options);
                            break;
                        case SIGNAL_CHARTS.OUTCOME :
                            outcome_data.options.series = result[SIGNAL_CHARTS.OUTCOME];
                            outcome_data.options.xAxis[0].categories = getCategoriesFromData(result[SIGNAL_CHARTS.OUTCOME][0]['data']);
                            signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.OUTCOME, outcome_data.options);
                            break;
                        case SIGNAL_CHARTS.SERIOUS_PIE_CHART:
                            pie_chart_data.options.series = result[SIGNAL_CHARTS.SERIOUS_PIE_CHART];
                            pie_chart_data.options.xAxis[0].categories = getCategoriesFromData(result[SIGNAL_CHARTS.SERIOUS_PIE_CHART][0]['data']);
                            signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.SERIOUS_PIE_CHART, pie_chart_data.options);
                            break;
                        case SIGNAL_CHARTS.HEAT_MAP :
                            var xAxis = result['systemOrganClass']['years'];
                            var yAxis = result['systemOrganClass']['socs'];
                            var chartData = result['systemOrganClass']['data'];
                            init_system_organ_heat_chart(xAxis, yAxis, chartData);
                            break;
                        case SIGNAL_CHARTS.ASSESSMENT_DETAILS :
                            $("#assessmentDetails .rxmain-container-content").html(result['assessmentDetailView']);
                            if (!$("#assessmentDetails").hasClass('in')) {
                                $('div[href="#assessmentDetails"]').click();
                            }
                            break;
                    }
                } else {
                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 20000});
                    $("#" + chartName).html('<span style="margin-left: 40%; margin-top:30%">No Data</span>');
                }
            },
            error:function (response) {
                $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 20000});
                $("#" + chartName).html('<span style="margin-left: 40%; margin-top:30%">No Data</span>');
            }
        });
    };

    var caseCountChart = function () {
        var caseCountArgus = $("#caseCountArgus").val();
        var pecCountArgus = $("#pecCountArgus").val();
        var pecCountEvdas = $("#pecCountEvdas").val();
        var isEvdasEnabled = $("#isEvdasEnabled").val();

        var series = ['PVA'];
        var caseCountSeries = [parseInt(caseCountArgus)];
        var pecCountSeries = [parseInt(pecCountArgus)];
        if (isEvdasEnabled == 'true') {
            series.push('EVDAS');
            caseCountSeries.push(0);
            pecCountSeries.push(parseInt(pecCountEvdas));
        }
        $("#caseCount").css("height", 250);
        $("#caseCount").highcharts({
            chart: {
                type: 'column'
            },
            title: {
                text: ''
            },
            xAxis: {
                title: 'Data-Sources',
                categories: series
            },
            yAxis: {
                title: 'Case Counts',
                tickInterval: 1
            },
            series: [{
                name: 'Case Count',
                data: caseCountSeries
            },
                {
                    name: 'PEC Count',
                    data: pecCountSeries
                }],
            credits: {
                enabled: false
            },
            legend: {
                enabled: true
            }
        });
    };
    caseCountChart();

    var medicalConceptChart = function () {
        var isEvdasEnabled = $("#isEvdasEnabled").val();

        var medicalConceptSeries = [{
            name: 'Case Count',
            data: chartMap('medConceptsDataCC').map(Number)
        }, {
            name: 'PEC(PVA)',
            data: chartMap('medConceptsDataPA').map(Number)
        }];

        if (isEvdasEnabled == 'true') {
            medicalConceptSeries.push({
                name: 'PEC(EVDAS)',
                data: chartMap('medConceptsDataPE').map(Number)
            })
        }

        $("#medicalConcept").highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: ''
            },
            xAxis: {
                title: 'Medical Concepts',
                categories: chartMap('medConceptsData')
            },
            yAxis: {
                title: 'Case Count',
                tickInterval: 1
            },
            series: medicalConceptSeries,
            credits: {
                enabled: false
            }
        });
    };

    var chartMap = function (medStringId) {
        var medStringEle = $("#" + medStringId);
        var medString = medStringEle.val();
        if (medString != "" && medString != null) {
            var medStringa = medString.slice(1, -1);
            var medArray = medStringa.split(",");
            return medArray
        }
        return []
    };

    medicalConceptChart();

    $(".caseCountChart").click(function () {
        var renderingNotification = '<span style="margin-left: 40%; margin-top:30%">Rendering Chart....</span>'
        $("#caseCount").html(renderingNotification);
        caseCountChart()
    });

    $(".medicalConceptChart").click(function () {
        var renderingNotification = '<span style="margin-left: 40%; margin-top:30%">Rendering Chart....</span>'
        $("#medicalConcept").html(renderingNotification);
        medicalConceptChart()
    });

    $("#dateRange").change(function () {
        if ($(this).val() === "CUSTOM") {
            $("#assessmentCustomDateRange").removeClass('hide');
        } else {
            $("#assessmentCustomDateRange").addClass('hide');
        }
    });
    $(".generate-assessment-reports").click(function (event) {
        var flag=checkAssessmentFilter();
        if(!flag){
            $.Notification.notify('warning', 'top right', "Warning", "Please provide information for all the mandatory fields which are marked with an asterisk (*)", {autoHideDelay: 20000});
            return
        }
        const date = new Date();
        let day = date.getDate();
        let month = date.toLocaleString('default', { month: 'short' })
        let year = date.getFullYear();
        let currentDate = `${day}/${month}/${year}`;
        if ($('#dateRange').val()=='SIGNAL_DATA') {
            $('.soc_data').hide();
        }else{
            $('.soc_data').show();
        }
        var renderingNotification = '<span style="margin-left: 40%; margin-top:30%">Rendering Chart...</span>';
        $('.assessment-chart').html(renderingNotification);
        $("#assessmentDetails .rxmain-container-content").html('<span style="margin-left: 40%; margin-top:30%">Rendering Table...</span>');
        $('#report-generating').show();
        $('.generate-assessment-reports').hide();
        var data = {};
        var eventSelection = $('#eventSelectionAssessment').val();
        var eventGroupSelection = $('#eventGroupSelectionAssessment').val();
        var productSelection = $('#productSelectionAssessment').val();
        var productGroupSelection = $('#productGroupSelectionAssessment').val();
        if ($("#dateRange").val() === "CUSTOM") {
            data['startDate'] = getFormattedDate($("#assessmentCustomStartDate").val())
            data['endDate'] = getFormattedDate($("#assessmentCustomEndDate").val())
        }
        var isMultiIngredient = $("#isMultiIngredientAssessment").val();
        data['dataSource'] = $('#dataSources').val();
        data['dateRange'] = $('#dateRange').val();
        data['productSelection'] = productSelection;
        data['productGroupSelection'] = productGroupSelection;
        data['eventSelection'] = eventSelection;
        data['eventGroupSelection'] = eventGroupSelection;
        data['isMultiIngredient'] = isMultiIngredient;
        data['validatedSignal.id'] = $("#signalIdPartner").val();
        if ($("#dateRange").val() === "CUSTOM" && ($.datepicker.parseDate('dd/mm/yy', data['startDate']) > new Date() || $.datepicker.parseDate('dd/mm/yy', data['endDate']) > new Date())) {
            event.preventDefault();
            $('.generate-assessment-reports').show();
            $.Notification.notify('error', 'top right', "Error", "The start/end date cannot be a future date.", {autoHideDelay: 10000});
        } else if ($("#dateRange").val() === "CUSTOM" && $.datepicker.parseDate('dd/mm/yy', data['startDate']) > $.datepicker.parseDate('dd/mm/yy', data['endDate'])){
            event.preventDefault();
            $('.generate-assessment-reports').show();
            $.Notification.notify('error', 'top right', "Error", "Start Date shall be less than the end start.", {autoHideDelay: 10000});
        } else if (isEnableSignalCharts && $('#showProductSelectionAssessment').html() && $('#showEventSelectionAssessment').html()) {
            var tempChartType = ['age-grp-over-time-chart', 'seriousness-over-time-chart', 'country-over-time-chart', 'gender-over-time-chart',
                'outcome-over-time-chart', 'seriousness-count-pie-chart', 'system-organ-heat-map', 'assessmentDetails'];
            _.each(tempChartType, function (chartName, index) {
                data['chartType'] = chartName;
                data['allChartGenerate'] = chartName === 'age-grp-over-time-chart' && event.hasOwnProperty('originalEvent');
                $.ajax({
                    type: "POST",
                    url: graphReportRestUrl,
                    data: data,
                    success: function (response) {
                        if (response.status) {
                            var result = response.data;
                            switch (chartName) {
                                case SIGNAL_CHARTS.AGE_GROUP :
                                    age_group_data.options.series = result[SIGNAL_CHARTS.AGE_GROUP];
                                    if(age_group_data.options.series!="")
                                    age_group_data.options.xAxis[0].categories = getCategoriesFromData(age_group_data.options.series[0]['data']);
                                    signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.AGE_GROUP, age_group_data.options);
                                    break;
                                case SIGNAL_CHARTS.SERIOUSNESS :
                                    seriousness_data.options.series = result[SIGNAL_CHARTS.SERIOUSNESS];
                                    if(seriousness_data.options.series!="")
                                    seriousness_data.options.xAxis[0].categories = getCategoriesFromData(seriousness_data.options.series[0]['data']);
                                    signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.SERIOUSNESS, seriousness_data.options);
                                    break;
                                case SIGNAL_CHARTS.COUNTRY :
                                    country_data.options.series = result[SIGNAL_CHARTS.COUNTRY];
                                    if(country_data.options.series!="")
                                    country_data.options.xAxis[0].categories = getCategoriesFromData(country_data.options.series[0]['data']);
                                    signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.COUNTRY, country_data.options);
                                    break;
                                case SIGNAL_CHARTS.GENDER :
                                    gender_data.options.series = result[SIGNAL_CHARTS.GENDER];
                                    if(gender_data.options.series!="")
                                    gender_data.options.xAxis[0].categories = getCategoriesFromData(gender_data.options.series[0]['data']);
                                    signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.GENDER, gender_data.options);
                                    break;
                                case SIGNAL_CHARTS.OUTCOME :
                                    outcome_data.options.series = result[SIGNAL_CHARTS.OUTCOME];
                                    if(outcome_data.options.series!="")
                                    outcome_data.options.xAxis[0].categories = getCategoriesFromData(outcome_data.options.series[0]['data']);
                                    signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.OUTCOME, outcome_data.options);
                                    break;
                                case SIGNAL_CHARTS.SERIOUS_PIE_CHART:
                                    pie_chart_data.options.series = result[SIGNAL_CHARTS.SERIOUS_PIE_CHART];
                                    if(pie_chart_data.options.series!="")
                                    pie_chart_data.options.xAxis[0].categories = getCategoriesFromData(pie_chart_data.options.series[0]['data']);
                                    signal.charts.direct_draw_bar_chart(SIGNAL_CHARTS.SERIOUS_PIE_CHART, pie_chart_data.options);
                                    break;
                                case SIGNAL_CHARTS.HEAT_MAP :
                                    var xAxis = result['systemOrganClass']['years'];
                                    var yAxis = result['systemOrganClass']['socs'];
                                    var chartData = result['systemOrganClass']['data'];
                                    if(typeof  chartData !=="undefined" && chartData!="")
                                    {
                                        init_system_organ_heat_chart(xAxis, yAxis, chartData);
                                    }
                                    break;
                                case SIGNAL_CHARTS.ASSESSMENT_DETAILS :
                                    $("#assessmentDetails .rxmain-container-content").html(result['assessmentDetailView']);
                                    if (!$("#assessmentDetails").hasClass('in')) {
                                        $('div[href="#assessmentDetails"]').click();
                                    }
                                    break;
                            }
                            $('#report-generating').hide();
                            $('.generate-assessment-reports').show();
                            $("#"+chartName).closest(".panel-default").find(".refresh-charts").show();
                        } else {
                            $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 20000});
                            $("#" + chartName).html('<span style="margin-left: 40%; margin-top:30%">No Data</span>');
                            $('#report-generating').hide();
                            $('.generate-assessment-reports').show();
                        }
                    },
                    error: function (response) {
                        $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 20000});
                        renderingNotification = '<span style="margin-left: 40%; margin-top:30%">No Data</span>';
                        $("#"+chartName).html(renderingNotification);
                        $('#report-generating').hide();
                        $('.generate-assessment-reports').show();
                    }
                });
            });

        } else {
            renderingNotification = '<span style="margin-left: 30%; margin-top:30%">No Product or Event Selected</span>';
            $('.assessment-chart').html(renderingNotification);
            $("#assessmentDetails .rxmain-container-content").html(renderingNotification);
            $('#report-generating').hide();
            $('.generate-assessment-reports').show();
        }
    });
var checkAssessmentFilter=function(){
    var flag=false;
    var dateRange=$("#dateRange").val();
    var startDate=$("#assessmentCustomStartDate").val();
    var endDate=$("#assessmentCustomEndDate").val();
    var product=$("#showProductSelectionAssessment").text();
    var event=$("#showEventSelectionAssessment").text();
    if(dateRange==="CUSTOM"){
        if(startDate!=="" && endDate!=="" && product && event){
            flag=true;
        }
    } else if(product && event){
        flag=true;
    }
    return flag;
}
    $(".data-analysis-assessment").click(function () {
        var flag=checkAssessmentFilter();
        if(flag===false){
            $.Notification.notify('warning', 'top right', "Warning", "Please provide information for all the mandatory fields which are marked with an asterisk (*)", {autoHideDelay: 20000});
            return
        }
        var assessmentButton=$(this)
        assessmentButton.removeClass('btn-primary');
        assessmentButton.addClass('pv-btn-grey');
        var data = {};
        if ($("#dateRange").val() === "CUSTOM") {
            data['startDate'] = getFormattedDate($("#assessmentCustomStartDate").val());
            data['endDate'] = getFormattedDate($("#assessmentCustomEndDate").val());
        }
        data['dateRange'] = $('#dateRange').val();
        data['productSelection'] = $('#productSelectionAssessment').val();
        data['productGroupSelection'] = $('#productGroupSelectionAssessment').val();
        data['eventSelection'] = $('#eventSelectionAssessment').val();
        data['eventGroupSelection'] = $('#eventGroupSelectionAssessment').val();
        data['signalId'] = $("#signalIdPartner").val();

        $.ajax({
            type: "POST",
            url: generateSpotfireReportUrl,
            data: data,
            success: function (data) {
                if (data.status) {
                    $.Notification.notify('success', 'top right', "Success", "You will be notified once report is generated.", {autoHideDelay: 20000});
                } else {
                    $.Notification.notify('error', 'top right', "Error", data.message, {autoHideDelay: 20000});
                }
                assessmentButton.addClass('btn-primary');
                assessmentButton.removeClass('pv-btn-grey');
            },
            error: function (data) {
                $.Notification.notify('error', 'top right', "Error", "Error occured while generating spotfire file.", {autoHideDelay: 20000});
                assessmentButton.addClass('btn-primary');
                assessmentButton.removeClass('pv-btn-grey');
            }
        })
    });


    $(".refresh-charts").unbind().click(function (e) {
        e.preventDefault();
        var chartName = $(this).attr('data-id');
        var renderingNotification = '<span style="margin-left: 40%; margin-top:30%">Rendering Chart...</span>';
        $("#" + chartName).html(renderingNotification);
        $("#" + chartName).css("height", 400);
        initChartDataGen(signalId, chartName)
    });

    $(".refresh-table").unbind().click(function (e) {
        var tableName = $(this).attr('data-id');
        var renderingNotification = '<span style="margin-left: 40%; margin-top:30%">Rendering Table...</span>';
        $("#" + tableName + " .rxmain-container-content").html(renderingNotification);
        initChartDataGen(signalId, tableName)
    });

    $(".refresh-analysis-table").on('click',function(){
        $('#signal-analysis-table').DataTable().ajax.reload();
    });

    $("#signal-analysis-table").DataTable({
        destroy: true,
        searching: false,
        sPaginationType: "bootstrap",
        responsive: false,
        dom: '<"top"f>rt<"row col-xs-12"<"col-xs-1 pt-8"l><"col-xs-5 dd-content"i><"col-xs-6 pull-right"p>>',
        "pagination": true,
        "iTotalDisplayRecords": "5",
        language: {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "ajax": {
            "url": fetchAnalysisDataUrl,
            "dataSrc": "aaData",
            data: {'signalId': $("#signalIdPartner").val()}
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_",

            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
        },
        "aoColumns": [
            {
                "mData": "fileName",
                "mRender": function (data, type, row) {
                    return "<a target='_blank' href='" + row.fileUrl + "'>" + row.fileName + "</a></div>";
                }
            },
            {
                "mData": "product"
            },
            {
                "mData": "event"
            },
            {
                "mData": "dateRange"
            },
            {
                "mData": "generatedBy"
            },
            {
                "mData": "generatedOn"
            }
        ],
        scrollY:true,
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text()
        }],
        "fnDrawCallback": function(oSettings){
            var rowsDataAR = $('#signal-analysis-table').DataTable().rows().data();
            pageDictionary($('#signal-analysis-table_wrapper'),rowsDataAR.length);
            showTotalPage($('#signal-analysis-table_wrapper'),rowsDataAR.length);
        },
        "bAutoWidth": false,
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text()
        }]
    });

    actionButton('#signal-analysis-table');

});

var getCategoriesFromData = function (dataList) {
    var categories = [];
    dataList.forEach(function (data) {
        categories.push(data['name'])
    });
    return categories
};

var age_group_data = {
    "options": {
        "chart": {
            backgroundColor: null,
            "type": "column",
            zoomType: 'x'
        },
        "credits": {
            "enabled": false
        },
        "lang": {
            "message": {
                "code": "app.label.widget.noData"
            }
        },
        legend: {
            enabled: true
        },
        "plotOptions": {
            "series": {
                "dataLabels": {
                    "enabled": true
                },
                "showInLegend": true,
                "stacking": "normal"
            }
        },
        "series": [],
        "title": {
            "text": null
        },
        "xAxis": [
            {
                "categories": [
                    "2000-MAR",
                    "2001-JAN",
                    "2003-MAR",
                    "2011-JAN",
                    "2011-MAR",
                    "2012-MAY",
                    "2013-FEB",
                    "2014-AUG",
                    "2015-FEB",
                    "2015-OCT",
                    "2016-FEB"
                ],
                "type": "category"
            }
        ],
        "yAxis": [
            {
                tickInterval: 1,
                "title": {
                    "text": "Case Count"
                }
            }
        ],
        zoomType: 'x'
    },
    "title": "Product AJ Review-1502428490133"
};

var country_data = {
    "options": {
        "background2": "#F0F0EA",
        "chart": {
            "backgroundColor": null,
            "style": {
                "fontFamily": "Dosis, sans-serif"
            },
            type: "column",
            zoomType: 'x'
        },
        "colors": [
            "#7cb5ec",
            "#f7a35c",
            "#90ee7e",
            "#7798BF",
            "#aaeeee",
            "#ff0066",
            "#eeaaee",
            "#55BF3B",
            "#DF5353",
            "#7798BF",
            "#aaeeee"
        ],
        "credits": {
            "enabled": false
        },
        "lang": {
            "message": {
                "code": "app.label.widget.noData"
            }
        },
        "legend": {
            enabled: true
        },
        "plotOptions": {
            "candlestick": {
                "lineColor": "#404048"
            },
            "series": {
                "dataLabels": {
                    "enabled": true
                },
                "showInLegend": true,
                "stacking": "normal"
            }
        },
        "series": [],
        "title": {
            "style": {
                "fontSize": "16px",
                "fontWeight": "bold",
                "textTransform": "uppercase"
            },
            "text": null
        },
        "tooltip": {
            "backgroundColor": "rgba(219,219,216,0.8)",
            "borderWidth": 0,
            "shadow": false
        },
        "xAxis": [
            {
                "categories": [
                    "2000-MAR",
                    "2001-JAN",
                    "2003-MAR",
                    "2011-JAN",
                    "2011-MAR",
                    "2012-MAY",
                    "2013-FEB",
                    "2015-FEB"
                ],
                "type": "category"
            }
        ],
        "yAxis": [
            {
                tickInterval: 1,
                "title": {
                    "text": "Case Count"
                }
            }
        ]
    },
    "title": "Product AJ Review-1501748382376"
};

var gender_data = {
    "options": {
        "background2": "#F0F0EA",
        "chart": {
            "type": "column",
            "backgroundColor": "#FFFFFF",
            zoomType: 'x'
        },
        "colors": [
            "#058DC7",
            "#50B432",
            "#ED561B",
            "#DDDF00",
            "#24CBE5",
            "#64E572",
            "#FF9655",
            "#FFF263",
            "#6AF9C4",
            "#7798BF",
            "#aaeeee"
        ],
        "credits": {
            "enabled": false
        },
        "labels": {
            "style": {
                "color": "#99b"
            }
        },
        "lang": {
            "message": {
                "code": "app.label.widget.noData"
            }
        },
        "legend": {
            enabled: true
        },
        "navigation": {
            "buttonOptions": {
                "theme": {
                    "stroke": "#CCCCCC"
                }
            }
        },
        "plotOptions": {
            "candlestick": {
                "lineColor": "#404048"
            },
            "series": {
                "dataLabels": {
                    "enabled": true
                },
                "showInLegend": true,
                "stacking": "normal"
            }
        },
        "series": [],
        "subtitle": {
            "style": {
                "color": "#666666",
                "font": "bold 12px \"Trebuchet MS\", Verdana, sans-serif"
            }
        },
        "title": {
            "style": {
                "color": "#000",
                "font": "bold 16px \"Trebuchet MS\", Verdana, sans-serif",
                "fontSize": "16px",
                "fontWeight": "bold",
                "textTransform": "uppercase"
            },
            "text": null
        },
        "tooltip": {
            "backgroundColor": "rgba(219,219,216,0.8)",
            "borderWidth": 0,
            "shadow": false
        },
        "xAxis": [
            {
                "categories": [
                    "2000-MAR",
                    "2001-JAN",
                    "2003-MAR",
                    "2011-JAN",
                    "2011-MAR",
                    "2012-MAY",
                    "2013-FEB",
                    "2014-AUG",
                    "2015-FEB",
                    "2015-OCT",
                    "2016-FEB"
                ],
                "type": "category"
            }
        ],
        "yAxis": [
            {
                tickInterval: 1,
                "title": {
                    "text": "Case Count"
                }
            }
        ]
    },
    "title": "Product AJ Review-1502946976720"
};

var seriousness_data = {
    "options": {
        "chart": {
            "backgroundColor": null,
            "style": {
                "fontFamily": "'Lucida Grande', 'Lucida Sans Unicode', Verdana, Arial, Helvetica, sans-serif",
                "fontSize": "12px"
            },
            "type": "column",
            zoomType: 'x'
        },
        "colors": [
            "#7cb5ec",
            "#434348",
            "#90ed7d",
            "#f7a35c",
            "#8085e9",
            "#f15c80",
            "#e4d354",
            "#2b908f",
            "#f45b5b",
            "#91e8e1"
        ],
        "credits": {
            "enabled": false
        },
        "labels": {
            "style": {
                "color": "#333333"
            }
        },
        "lang": {
            "message": {
                "code": "app.label.widget.noData"
            }
        },
        "legend": {
            enabled: true
        },
        "navigation": {
            "buttonOptions": {
                "theme": {
                    "stroke": "#CCCCCC"
                }
            }
        },
        "plotOptions": {
            "series": {
                "dataLabels": {
                    "enabled": true
                },
                "showInLegend": true,
                "stacking": "normal"
            }
        },
        "series": [],
        "subtitle": {
            "style": {
                "color": "#666666"
            }
        },
        "title": {
            "style": {
                "color": "#333333",
                "fontSize": "18px"
            },
            "text": null
        },
        "xAxis": [
            {
                "categories": [
                    "2000-MAR",
                    "2001-JAN",
                    "2003-MAR",
                    "2011-JAN",
                    "2011-MAR",
                    "2012-MAY",
                    "2013-FEB",
                    "2014-AUG",
                    "2015-FEB",
                    "2015-OCT",
                    "2016-FEB"
                ],
                "type": "category"
            }
        ],
        "yAxis": [
            {
                tickInterval: 1,
                "title": {
                    "text": "Case Count"
                }
            }
        ]
    },
    "title": "Product AJ Review-1504150592989"
};

var outcome_data = {
    "options": {
        "background2": "#F0F0EA",
        "chart": {
            "type": "column",
            "backgroundColor": "#FFFFFF",
            zoomType: 'x'
        },
        "colors": [
            "#058DC7",
            "#50B432",
            "#ED561B",
            "#DDDF00",
            "#24CBE5",
            "#64E572",
            "#FF9655",
            "#FFF263",
            "#6AF9C4",
            "#7798BF",
            "#aaeeee"
        ],
        "credits": {
            "enabled": false
        },
        "labels": {
            "style": {
                "color": "#99b"
            }
        },
        "lang": {
            "message": {
                "code": "app.label.widget.noData"
            }
        },
        "legend": {
            enabled: true
        },
        "navigation": {
            "buttonOptions": {
                "theme": {
                    "stroke": "#CCCCCC"
                }
            }
        },
        "plotOptions": {
            "candlestick": {
                "lineColor": "#404048"
            },
            "series": {
                "dataLabels": {
                    "enabled": true
                },
                "showInLegend": true,
                "stacking": "normal"
            }
        },
        "series": [],
        "subtitle": {
            "style": {
                "color": "#666666",
                "font": "bold 12px \"Trebuchet MS\", Verdana, sans-serif"
            }
        },
        "title": {
            "style": {
                "color": "#000",
                "font": "bold 16px \"Trebuchet MS\", Verdana, sans-serif",
                "fontSize": "16px",
                "fontWeight": "bold",
                "textTransform": "uppercase"
            },
            "text": null
        },
        "tooltip": {
            "backgroundColor": "rgba(219,219,216,0.8)",
            "borderWidth": 0,
            "shadow": false
        },
        "xAxis": [
            {
                "categories": [
                    "2000-MAR",
                    "2001-JAN",
                    "2003-MAR",
                    "2011-JAN",
                    "2011-MAR",
                    "2012-MAY",
                    "2013-FEB",
                    "2014-AUG",
                    "2015-FEB",
                    "2015-OCT",
                    "2016-FEB"
                ],
                "type": "category"
            }
        ],
        "yAxis": [
            {
                tickInterval: 1,
                "title": {
                    "text": "Case Count"
                }
            }
        ]
    },
    "title": "Product AJ Review-1502946976720"
};

var pie_chart_data = {
    "options": {
        "background2": "#F0F0EA",
        "chart": {
            "type": "column",
            "backgroundColor": "#FFFFFF",
            zoomType: 'x'
        },
        "colors": [
            "#058DC7",
            "#50B432",
            "#ED561B",
            "#DDDF00",
            "#24CBE5",
            "#64E572",
            "#FF9655",
            "#FFF263",
            "#6AF9C4",
            "#7798BF",
            "#aaeeee"
        ],
        "credits": {
            "enabled": false
        },
        "labels": {
            "style": {
                "color": "#99b"
            }
        },
        "lang": {
            "message": {
                "code": "app.label.widget.noData"
            }
        },
        legend: {
            enabled: true
        },
        "navigation": {
            "buttonOptions": {
                "theme": {
                    "stroke": "#CCCCCC"
                }
            }
        },
        "plotOptions": {
            "candlestick": {
                "lineColor": "#404048"
            },
            "series": {
                "dataLabels": {
                    "enabled": true
                },
                "showInLegend": true,
                "stacking": "normal"
            }
        },
        "series": [],
        "subtitle": {
            "style": {
                "color": "#666666",
                "font": "bold 12px \"Trebuchet MS\", Verdana, sans-serif"
            }
        },
        "title": {
            "style": {
                "color": "#000",
                "font": "bold 16px \"Trebuchet MS\", Verdana, sans-serif",
                "fontSize": "16px",
                "fontWeight": "bold",
                "textTransform": "uppercase"
            },
            "text": null
        },
        "tooltip": {
            "backgroundColor": "rgba(219,219,216,0.8)",
            "borderWidth": 0,
            "shadow": false
        },
        "xAxis": [
            {
                "categories": [
                    "2000-MAR",
                    "2001-JAN",
                    "2003-MAR",
                    "2011-JAN",
                    "2011-MAR",
                    "2012-MAY",
                    "2013-FEB",
                    "2014-AUG",
                    "2015-FEB",
                    "2015-OCT",
                    "2016-FEB"
                ],
                "type": "category"
            }
        ],
        "yAxis": [
            {
                tickInterval: 1,
                "title": {
                    "text": "Case Count"
                }
            }
        ]
    },
    "title": "Product AJ Review-1502946976720"
};