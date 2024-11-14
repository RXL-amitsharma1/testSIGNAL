//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/activity/activities.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/dataTableActionButtons.js
//= require app/pvs/alerts_review/alert_review
//= require app/pvs/alertComments/alertComments.js

var userLocal = "en";
var applicationName = "Ad-Hoc Alert";
var applicationLabel = "Ad-Hoc Alert";
var table;
var dir = '';
var index = -1;
var commentList;

var prev_page = [];
var selectedCases = [];
var selectedCasesInfo = [];
var adhocEntriesCount=sessionStorage.getItem('adhocPageEntries')!=null?sessionStorage.getItem('adhocPageEntries'):25


$.fn.dataTable.ext.order['dom-priority'] = function (settings, col) {
    var json = $.parseJSON(priorities);
    return this.api().column(col, {order: 'index'}).nodes().map(function (td, i) {
        var priority = $('a', td)[0].firstChild.getAttribute('data-value');
        for (var i = 0; i < json.length; ++i) {
            if (json[i][0] == priority)
                return json[i][1];
        }
    });
};

$(document).ready(function () {
    insertFilterDropDown($("#search-control"), $(".pos-rel"));
    var alertDetailsTable;
    assignedToData = [];

    var checkedIdList = [];
    var checkedRowList = [];
    signal.alertReview.setSortOrder();


    var initAlertDetailsTable = function () {

        var isFilterRequest = true;
        var filterValues = [];
        var prefix = "adhoc_";
        if (window.sessionStorage) {
            if (signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
                filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
            } else {
                signal.alertReview.removeFiltersFromSessionStorage(prefix);
                isFilterRequest = false;
            }
        }

        $(document).on('click', '#alertsDetailsTable_paginate', function () {
            if($('.alert-select-all').is(":checked") && !prev_page.includes($('li.active').text().slice(-3).trim())){
                prev_page.push($('li.active').text().slice(-3).trim());
            }
            if((!$('.alert-select-all').is(":checked") && prev_page.includes($('li.active').text().slice(-3).trim()))){
                var position = prev_page.indexOf($('li.active').text().slice(-3).trim());
                prev_page.splice(position,1);
            }
        });

        var constructColumns = function(){
           var aoColumns = [
                {
                    "mData": "selected",
                    "mRender": function (data, type, row) {
                        if (selectedCases.includes(row.id.toString())){
                            return  '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';
                        } else{
                            return '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' />';

                        }
                    },
                    "className": "",
                    "orderable": false,
                    "visible": true
                },
                {
                    "mData": "dropdown",
                    "className": "col-min-20",
                    "mRender": function (data, type, row, meta) {
                        var actionButton = '<div style="display: block;" class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="dropdown-toggle" data-toggle="dropdown" tabindex="0"> \
                        <span style="cursor: pointer;font-size: 125%;" class="glyphicon glyphicon-option-vertical"></span><span class="sr-only">Toggle Dropdown</span> \
                        </a>';

                        if(row.notes != ''){
                            actionButton +=  '<i class="mdi mdi-chat blue-2 font-13 pos-ab comment" title="' + $.i18n._('commentAvailable') + '"></i>';
                        }

                        actionButton += '<ul class="dropdown-menu menu-cosy" role="menu"><li role="presentation"><a tabindex="0" class="review-row-icon comment-icon" data-info="row" data-id=' + row.id + '><span class="fa fa-comment m-r-10" ></span>' + $.i18n._('comments') + '</a></li>';
                        if((row.isUndoEnabled=="true" && row.isDefaultState === "true") || row.isDefaultState === "false"){
                            if(!row.isValidationStateAchieved && (row.isUndoEnabled === "true" && (isAdmin || row.dispPerformedBy === currUserName))) {
                                actionButton += '<li role="presentation" class="popover-parent">' +
                                    '<a tabindex="0" data-id ="' + row.id + '" title="Undo Disposition Change" data-html="true" class="review-row-icon undo-alert-disposition" ' +
                                    'data-toggle="popover" data-content="<textarea class=\'form-control editedJustification\'>' +
                                    '</textarea>' +
                                    '<ol class=\'confirm-options\' id=\'revertConfirmOption\'>' +
                                    '<li><a tabindex=\'0\' href=\'javascript:void(0);\' title=\'Save\'><i class=\'mdi mdi-checkbox-marked green-1\' data-id =\'' + row.id + '\' id=\'confirmUndoJustification\'></i></a></li>' +
                                    '<li><a tabindex=\'0\' href=\'javascript:void(0);\' title=\'Close\'><i class=\'mdi mdi-close-box red-1\' id=\'cancelUndoJustification\'></i> </a></li>' +
                                    '</ol>" '+
                                    '</a>' +
                                    '<span class="md md-undo m-r-10"></span>Undo Disposition Change';
                                actionButton += '</li>';
                            } else {
                                actionButton += '<li role="presentation">' +
                                    '<a tabindex="0" data-id ="' + row.id + '"   class="review-row-icon undo-alert-disposition" style="cursor: not-allowed; opacity: 0.65"> <span class="md md-undo m-r-10"></span>Undo Disposition Change</a>';
                            }
                        }
                        actionButton += '</ul></div>';
                        return actionButton;
                    },
                    "orderable": false,
                    "visible": true,
                    "className":"dropDown"
                }];
            if(isPriorityEnabled) {
                aoColumns.push.apply(aoColumns, [
                    {
                    "mData": "priority",
                    "aTargets": ["priority"],
                    "orderDataType": "dom-priority",
                    "mRender": function (data, type, row) {
                        var isPriortyChangeAllowed = (isProductSecurity == 'true' && isSafetyLeadAllowed(allowedProductsAsSafetyLead, row.productId)) || isProductSecurity == 'false'
                        return signal.utils.render('priority', {
                            priorityValue: row.priority.value,
                            priorityClass: row.priority.iconClass,
                            isPriorityChangeAllowed: isPriortyChangeAllowed
                        });
                    },
                    'className': 'text-center col-min-30 priorityParent',
                    "visible": true
                }]);
            }
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "name",
                    "orderable": false,
                    "className": "col-min-250 col-max-650 cell-break",
                    mRender: function (data, type, row) {
                        var colElement = '<div><div class="col-height">';
                        colElement += "<input type='hidden' class='row-product-json-container' value='" + row.productSelectionJson + "'/>" ,
                            colElement += "<a href='/signal/adHocAlert/alertDetail?id=" + row.id + "'>" +
                                escapeHTML(row.name) + "</a>";
                        colElement += '</div></div>';
                        return colElement;
                    },
                    "visible": true
                },
                {
                    "mData": "productSelection",
                    "orderable": false,
                    "className": "col-min-150",
                    "mRender": function (data, type, row) {
                        return "<span data-field ='productName' data-id='" + row.productSelection + "'>" + (row.productSelection) + "</span>"
                    },
                    "visible": signal.fieldManagement.visibleColumns('productSelection')
                },
                {
                    "mData": "eventSelection",
                    "orderable": false,
                    "className": "col-min-100",
                    "visible": signal.fieldManagement.visibleColumns('eventSelection')
                },
                {
                    "mData": "issueTracked",
                    "visible": signal.fieldManagement.visibleColumns('issueTracked')
                },
                {
                    "mData": "numOfIcsrs",
                    "className": "col-min-75",
                    "visible": signal.fieldManagement.visibleColumns('numOfIcsrs')
                },
                {
                    "mData": "initDataSrc",
                    "className": "col-min-100",
                    "visible": signal.fieldManagement.visibleColumns('initDataSrc')
                },
                {
                    "mData": 'signalsAndTopics',
                    "orderable": false,
                    "className": "col-min-150 col-max-200 signalInformation",
                    "mRender": function (data, type, row) {
                        var signalAndTopics = '';
                        $.each(row.signalsAndTopics, function(i, obj){
                            var url = signalDetailUrl + '?id=' + obj['signalId'];
                            signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150"><a  class="cell-break" title="' + obj.disposition.displayName + '" onclick="validateAccess(event,' + obj['signalId'] + ')" href="' + url + '">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                            signalAndTopics = signalAndTopics + ","
                        });
                        if(signalAndTopics.length > 1)
                            return '<div class="cell-break word-wrap-break-word col-max-150>' + signalAndTopics.substring(0, signalAndTopics.length - 1) + '</div>';
                        else
                            return '-';

                    },
                    "visible": signal.fieldManagement.visibleColumns('signalsAndTopics')
                },
                {
                    "mData": "currentDisposition",
                    "mRender": function (data, type, row) {
                        return signal.utils.render('disposition_dss3', {
                            allowedDisposition: dispositionIncomingOutgoingMap[row.disposition],
                            currentDisposition: row.disposition,
                            forceJustification: forceJustification,
                            isValidationStateAchieved: row.isValidationStateAchieved,
                            id:row.currentDispositionId
                        });
                    },
                    "visible": signal.fieldManagement.visibleColumns('currentDisposition'),
                    "class": 'col-max-300 dispositionAction'
                },
                {
                    "mData": "disposition",
                    "className": "col-max-250 currentDisposition",
                    "mRender": function (data, type, row) {
                        return row.disposition;
                    },
                    "visible": signal.fieldManagement.visibleColumns('disposition')

                },
                {
                    "mData": "assignedTo",
                    "className": "col-min-100 col-max-150",
                    "mRender": function (data, type, row) {
                        return signal.list_utils.assigned_to_comp(row.id, row.assignedToValue)
                    },
                    "visible": signal.fieldManagement.visibleColumns('assignedTo'),
                    "orderable": false
                },
                {
                    "mData": "detectedDate",
                    "className": "col-min-100",
                    "visible": signal.fieldManagement.visibleColumns('detectedDate')
                },
                {
                    "mData": "dueIn",
                    "className": "col-min-100 dueIn",
                    "mRender": function (data, type, row) {
                        return signal.list_utils.due_in_comp(row.dueIn)
                    },
                    "visible": signal.fieldManagement.visibleColumns('dueIn')
                }
            ]);
            return aoColumns
        };
        var allowClear = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess){
            allowClear = false
        }

        table = $('#alertsDetailsTable').DataTable({
            "sPaginationType": "bootstrap",
            "processing": true,
            "serverSide": true,
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "rowCallback": function (row, data, index) {
                //Bind AssignedTo Select Box
                if(index == 0){
                    assignedToData = [];
                }
                assignedToData.push({name: data.assignedToValue.name, id: data.assignedToValue.id});
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".assignedToSelect").select2({
                        minimumInputLength: 0,
                        multiple: false,
                        placeholder: 'Select Assigned To',
                        allowClear: false,
                        width: "100%",})
                }
            },
            fnDrawCallback: function (settings) {
                removeBorder($("#alertsFilter"));
                var current_page = $('li.active').text().slice(-3).trim();
                if(typeof prev_page != 'undefined' && $.inArray(current_page,prev_page) == -1){
                    $(".alert-select-all").prop('checked',false);
                } else {
                    $(".alert-select-all").prop('checked',true);
                }
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(".changeDisposition").removeAttr("data-target");
                    $(".changePriority").removeAttr("data-target");
                    $("#dispositionJustificationPopover").addClass("hide")
                }
                if(typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess) {
                    $(".changeDisposition[data-validated-confirmed=true]").removeAttr("data-target");
                }
                var rowsDataAR = $('#alertsDetailsTable').DataTable().rows().data();
                if(settings.json != undefined) {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

                }else {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], 50, rowsDataAD.length);
                }

                initPSGrid($('#alertsDetailsTable_wrapper'));
                focusRow($("#alertsDetailsTable").find('tbody'));

                signal.list_utils.flag_handler("adHocAlert", "toggleFlag");
                $(".assignedToSelect").each(function (i) {
                    signal.user_group_utils.bind_assign_to($(this), searchUserGroupListUrl, assignedToData[i], allowClear);
                });
                signal.alertReview.sortIconHandler()
                colEllipsis();
                webUiPopInit();
                populateSelectedCases();
                closeInfoPopover();
                showInfoPopover();
                $('.dt-pagination').on('change', function () {
                    var countVal = $('.dt-pagination').val()
                    sessionStorage.setItem("adhocPageEntries", countVal);
                    adhocEntriesCount = sessionStorage.getItem("adhocPageEntries");
                })
            },

            fnInitComplete: function (settings, json) {
                assignedToData = [];
                signal.alertReview.bindGridDynamicFilters(json.filters, prefix, json.configId);

                var theDataTable = $('#alertsDetailsTable').DataTable();

                $("#closedCheckBox").click(function () {
                    //TODO: Filters for gird to be introduced.
                });

                $("#toggle-column-filters, #ic-toggle-column-filters").click(function () {
                    var ele = $('.yadcf-filter-wrapper');
                    var inputEle = $('.yadcf-filter');
                    if (ele.is(':visible')) {
                        ele.hide();
                    } else {
                        ele.show();
                        inputEle.first().focus();
                    }
                    theDataTable.columns.adjust().fixedColumns().relayout();
                });
                $('.yadcf-filter-wrapper').hide();
                theDataTable.draw();
                var fixedCols = isPriorityEnabled ? 5 : 4;
                signal.fieldManagement.init($('#alertsDetailsTable').DataTable(), '#alertsDetailsTable', fixedCols, true);
                bindAssignToSelection(searchUserGroupListUrl, assignToGroupUrl, table, allowClear);
                addGridShortcuts('#alertsDetailsTable');
                showInfoPopover();
            },

            "ajax": {
                "url": listConfigUrl + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURIComponent(JSON.stringify(filterValues)),
                type: 'POST',
                "data": function (d) {
                    d.disposition = $('#dispositionFilter').val()
                    let selectedData = $('#alertsFilter').val();
                    if(selectedData) {
                        if(callingScreen == CALLING_SCREEN.DASHBOARD){
                            d.selectedAlertsFilterForDashboard = JSON.stringify(selectedData);
                        }else{
                            d.selectedAlertsFilter = JSON.stringify(selectedData);
                        }
                    }
                    else if(callingScreen == CALLING_SCREEN.DASHBOARD){
                        let retainedData = $("#filterValsForDashboard").val();
                        if(retainedData=="" || retainedData=="null"){
                            retainedData = null;
                        }
                        else {
                            retainedData = JSON.parse(retainedData);
                        }
                        d.selectedAlertsFilterForDashboard = JSON.stringify(retainedData);

                    }else{
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
                "dataSrc": "data"
            },
            "aaSorting": [],
            "bLengthChange": true,
            "iDisplayLength": parseInt(adhocEntriesCount),
            "bProcessing": true,
            "responsive": true,
            "colReorder": {
                "realtime": false
            },
            "MSG_LOADING": '',
            "oLanguage": {
                "sProcessing": '<div class="grid-loading"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>',
                "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table",
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu":"Show _MENU_",
            },
            "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
            "dom": '<"top">rt<"row col-xs-12"<"col-xs-1 pt-15 width-auto"l><"col-xs-4 dd-content"i><"col-xs-6 pull-right"p>>',
            searching: true,
            "aoColumns": constructColumns(),
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });
        new $.fn.dataTable.FixedColumns(table, {
            leftColumns: isPriorityEnabled?4:3,
            rightColumns: 0,
            heightMatch: 'auto'
        });

        table.on( 'xhr', function () {
            commentList = table.ajax.json().commentList;
        });

        init_filter(table);
        return table
    };

    var init_filter = function (data_table) {

        var filterOptions =isPriorityEnabled? [
            {
                column_number: 3
            },  //name
            {
                column_number: 4
            },  //product
            {
                column_number: 5
            },  //event
            {
                column_number: 6
            },  //is issue tracked
            {
                column_number: 7
            },  //num icr
            {
                column_number: 8
            }, //init data sources
            {
                column_number: 9
            }, //signal
            {
                column_number: 11
            }, //current disposition
            {
                column_number: 12
            }, //detected date
            {
                column_number: 13
            }, //due in
            {
                column_number: 14
            }, //due in

        ]:[
            {
                column_number: 2
            },  //name
            {
                column_number: 3
            },  //product
            {
                column_number: 4
            },  //event
            {
                column_number: 5
            },  //issues tracked
            {
                column_number: 6
            },  //num icsr
            {
                column_number: 7
            },  //init datasource
            {
                column_number: 8
            }, //signal
            {
                column_number: 10
            }, //current dispostion
            {
                column_number: 11
            }, //detected date
            {
                column_number: 12
            }, //detected date
            {
                column_number: 13
            }  //due in
        ];
        yadcf.init(data_table, filterOptions,
            {
                filter_type: 'text',
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            });
    };


    $('i#copySelection').click(function () {
        showAllSelectedCaseNumbers()
    });

    var showAllSelectedCaseNumbers = function () {
        $('#copyCaseNumberModel').modal({
            show: true
        });

        var numbers = _.map($('input.copy-select:checked').parent().parent().find('td:nth-child(4)'), function (it) {
            return $(it).text()
        });
        $("#caseNumbers").text(numbers);
    };

    var init = function () {
        signal.fieldManagement.populateColumnList(gridColumnsViewUrl, gridColumnsViewUpdateUrl);
        alertDetailsTable = initAlertDetailsTable();
        $('i#copySelection').click(function () {
            showAllSelectedCaseNumbers()
        });

        $(document).on('click', 'input#select-all', function () {
            $(".copy-select").prop('checked', this.checked);
            $(".alert-select-all").prop('checked', this.checked);
            if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                checkboxSelector = 'table#alertsDetailsTable .copy-select';
            } else {
                checkboxSelector = 'table.DTFC_Cloned .copy-select';
            }
            $.each($(checkboxSelector), function () {
                if(selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')){
                    selectedCases.push($(this).attr("data-id"));
                    var selectedRowIndex = $(this).closest('tr').index();
                    if (isAbstractViewOrCaseView(selectedRowIndex)) {
                        selectedRowIndex = selectedRowIndex / 2
                    }
                    selectedCasesInfo.push(populateDispositionDataFromGrid(selectedRowIndex));
                } else if(selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')){
                    selectedCases.splice( $.inArray($(this).attr("data-id"), selectedCases), 1 );
                    selectedCasesInfo.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
                }
            })
        });
        bindCommentNotes();
    };

    init();

    $(document).on('click', '.alert-check-box', function () {
        if (!this.checked) {
            if ( $('table.DTFC_Cloned #select-all').is(':checked')) {
                $('table.DTFC_Cloned #select-all').prop('checked', false)
            }
        }
    });

    $('#exportTypes a[href]').click(function (e) {
        var clickedURL = e.currentTarget.href;
        var ids = [];
        var search = $("#custom-search").val();

        $('table.DTFC_Cloned .copy-select:checked').each(function () {
            if ($(this)[0]['checked']) {
                ids.push($(this).attr('data-id'))
            }
        });
        var isFilterRequest = true;
        var filterValues = [];
        var prefix = "adhoc_";
        $('.dynamic-filters').each(function() {
            if ($(this).is(':checked')) {
                filterValues.push($(this).val());
            }
        });
        var updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' +encodeURIComponent(JSON.stringify(filterValues)) + "&ids=" + ids + "&callingScreen=" + callingScreen + "&search=" + search;
        var filterList = {};
        $('#alertsDetailsTable').DataTable().columns().every( function ()
        {
            if(this.search() !== "") {
                filterList[this.dataSrc()] = this.search();
            }
        });
        var filterListJson = JSON.stringify(filterList);
        window.location.href = updatedExportUrl + "&filterList=" + encodeURIComponent(filterListJson);
        return false
    });

    $("#commentsModal").on('hidden.bs.modal', function () {
        $('#commentNotes').val("");
        $('#commentNotes').val("");
        $(".createdBy").text("");
        $('.addAdhocComment').html("Add");
        $("#commentsModal").find('#commentNotes').on("keyup", function () {
            if ($('#commentNotes').val() =="") {
                $("#commentsModal").find('.addAdhocComment').prop("disabled", true);
            } else {
                $("#commentsModal").find('.addAdhocComment').prop("disabled", false);
            }
        });
        $("#commentsModal").find('.addAdhocComment').prop("disabled", true);
    })
    $(document).on('change', '.copy-select', function(){
        addToSelectedCheckBox($(this))
    });
    $('#alertsFilter').on('change', function() {
        if($('#alertsFilter').val()==null) {
            if(callingScreen === CALLING_SCREEN.DASHBOARD) {
                $("#filterValsForDashboard").val("");
            }else {
                $("#filterVals").val("");
            }
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
        $("#alertsDetailsTable").DataTable().ajax.reload();
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

var bindCommentNotes = function () {
    $(document).on("click", ".comment-icon", function () {
        var $this = this;
        var selectedRowIds = [];
        var selectedRowCount = caseJsonArrayInfo.length;
        var commentModal = $('#commentsModal');
        if (selectedRowCount > 1 && $(this).closest('tr').find(".copy-select").prop("checked")) {
            $(commentModal).find('div.bulkOptionsSection').show();
            $(commentModal).find('div.bulkOptionsSection span.count').html(selectedRowCount);
        } else {
            $(commentModal).find('div.bulkOptionsSection').hide();
        }

        $('div.bulkOptionsSection input[name=bulkOptions]').unbind().on('change', function () {
            switch ($(this).val()) {
                case 'allSelected':
                    selectedRowIds = initiateBulkRowCommentProcess();
                    bindAddCommentNotes(commentModal, selectedRowIds,"bulk");
                    break;
                case 'current':
                    selectedRowIds = [];
                    selectedRowIds.push(initiateSingleRowCommentProcess($this));
                    bindAddCommentNotes(commentModal, selectedRowIds,"row");
                    break;
            }
        });
        if ($(this).data('info') === 'row') {
            selectedRowIds.push(initiateSingleRowCommentProcess($this));
            bindAddCommentNotes(commentModal, selectedRowIds,"row",$this);
        }
        commentModal.modal('show');
    });
};

var initiateSingleRowCommentProcess = function ($this) {
    var selectedRowIndex = ($($this).parents('tr') && $($this).parents('tr').length > 0)?$($this).parents('tr').index():$($this).attr('data-index');
    var curRowdata = table.row(selectedRowIndex).data();
    var adhocAlertId = curRowdata.id
    var updatedComments = $('#commentNotes').val();
    $.ajax({
        type: "POST",
        data: {'adhocAlert.id': adhocAlertId},
        url: fetchCommentUrl,
        success: function (result) {
            var commentModal = $('#commentsModal');
            updatedComments = $('#commentNotes').val();
            if (result.comment) {
                if(updatedComments.length >= result.comment.length){
                    commentModal.find('#commentNotes').val(updatedComments);
                }else{
                    commentModal.find('#commentNotes').val(result.comment);
                }
                commentModal.find(".createdBy").text("Last Modified by " + result.createdBy + " on " + moment.utc(result.dateUpdated).format('DD-MMM-YYYY hh:mm:ss A'));
                commentModal.find('.addAdhocComment').html("Update");
                commentModal.find('#commentNotes').on("keyup", function () {
                    if ($('#commentNotes').val().length>0 && $('#commentNotes').val() == result.comment && updatedComments !=="") {
                        commentModal.find('.addAdhocComment').prop("disabled", true);
                    } else {
                        commentModal.find('.addAdhocComment').prop("disabled", false);
                    }
                });
                commentModal.find('.addAdhocComment').prop("disabled", true);
            }
        }
    });

    return selectedRowIndex
};

var initiateBulkRowCommentProcess = function () {
    var indexSet = new Set();
    var selectedRowIds = [];
    $.each($('table.DTFC_Cloned .copy-select:checked'), function () {
        indexSet.add(($(this).closest('tr').index()));
    });

    indexSet.forEach(function (index) {
        selectedRowIds.push(index);
    });
    return selectedRowIds;
};

var bindAddCommentNotes = function (commentModal, selectedRowIds,dataInfo,currentRow) {
    var selectedAdhocAlertIds = [];
    if(selectedCases.length == 0){
        selectedAdhocAlertIds.push($(currentRow).data("id"));
    }
    else if(selectedCases.length > 0&& dataInfo == "row"){
        selectedAdhocAlertIds.push($(currentRow).data("id"));
    }
    else if(selectedCases.length > 0&& dataInfo == "bulk"){
        selectedAdhocAlertIds = selectedCases.map(i=>Number(i));
    }
    commentModal.find('.addAdhocComment').unbind().on('click', function () {
        $.ajax({
            type: "POST",
            data: {
                'selectedAdhocAlertIds': JSON.stringify(selectedAdhocAlertIds),
                'comment': $("#commentNotes").val()?$("#commentNotes").val():null
            },
            url: saveCommentUrl,
            success: function (result) {
                var commentModal = $('#commentsModal');
                if (result.success) {
                    commentModal.find('#commentNotes').val(result.comment);
                    commentModal.find(".createdBy").text("Last Modified by " + result.createdBy + " on " + moment.utc(result.dateUpdated).format('DD-MMM-YYYY hh:mm:ss A'));
                    commentModal.find('.addAdhocComment').html("Update");
                    if (result.isUpdated) {
                        if($("#commentNotes").val()){
                            $.Notification.notify('success', 'top right', "Success", "Comment updated successfully.", {autoHideDelay: 10000});
                        } else
                        {
                            $.Notification.notify('success', 'top right', "Success", "Comments removed successfully.", {autoHideDelay: 10000});
                        }
                    } else {
                        $.Notification.notify('success', 'top right', "Success", "Comments added successfully.", {autoHideDelay: 10000});
                    }
                    commentModal.find('.addAdhocComment').prop("disabled", true);
                    commentModal.find('#commentNotes').on("keyup", function () {
                        if ($("#commentNotes").val().toString() == result.comment.toString() && $("#commentNotes").val().toString()!=="") {
                            commentModal.find('.addAdhocComment').prop("disabled", true);
                        } else {
                            commentModal.find('.addAdhocComment').prop("disabled", false);
                        }
                    });
                    $('#alertsDetailsTable').DataTable().ajax.reload();
                }

                if (currentRow) {
                    if (result.comment) {
                        showCommentIcon(currentRow);
                    } else {
                        removeCommentIcon(currentRow);
                    }
                } else {
                    commentModal.modal("hide");
                    $('body').removeClass('modal-open');
                    $('.modal-backdrop').remove();
                    var checkboxSelector = 'table.DTFC_Cloned .copy-select:checked';
                    $.each($(checkboxSelector), function () {
                        if (result.comment) {
                            showCommentIcon(this);
                        } else {
                            removeCommentIcon(this)
                        }
                    });
                    caseJsonArrayInfo = [];
                    selectedCases = [];
                    selectedCasesInfo = [];
                    $(".copy-select").prop('checked', false);
                    $(".alert-select-all").prop('checked', false);
                    prev_page = [];
                    if (applicationName != "EVDAS Alert" || applicationName != 'Ad-Hoc Alert'){
                        alertIdSet.clear();
                    }
                }
            }
        });
    });
};

function bindAssignToSelection(searchUserGroupListUrl, assignToGroupUrl, table, allowClear) {
    $(".assignedToSelect").each(function (i) {
        signal.user_group_utils.bind_assign_to($(this), searchUserGroupListUrl, assignedToData[i], allowClear);
    });
    signal.user_group_utils.bind_assignTo_selection(assignToGroupUrl, table, hasReviewerAccess);
}


function populateSelectedCases() {
    $(".copy-select").change(function () {
        if (selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')) {
            selectedCases.push($(this).attr("data-id"));
            selectedCasesInfo.push(populateDispositionDataFromGrid($(this).closest('tr').index()));
        } else if (selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')) {
            selectedCasesInfo.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
            selectedCases.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
        }
    });
}
