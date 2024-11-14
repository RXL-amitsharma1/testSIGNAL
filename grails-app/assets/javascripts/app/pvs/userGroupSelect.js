function bindShareWith(selector, sharedWithListUrl, sharedWithValuesUrl, data, dynamicSelection, autoAssign, allowClear) {
    selector.select2({
        tags: dynamicSelection,
        minimumInputLength: 0,
        multiple: false,
        placeholder: 'Select Assigned To',
        allowClear: allowClear ?? true,
        width: "100%",
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: sharedWithListUrl,
            data: function (params) {
                if(autoAssign){
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale,
                        autoAssign: true
                    }
                } else {
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale
                    };
                }
            },
            processResults: function (data, params) {
                params.page = params.page || 10;
                return {
                    results: data,
                    pagination: {
                        more: (params.page * 10) < data.length
                    }
                };
            }
        }
    });
    if (data) {
        var option = new Option(encodeToHTML(data.name), data.id, true, true);
        selector.append(option).trigger('change.select2');
    }
}

function bindShareWithForSignalMemo(selector, sharedWithListUrl, sharedWithData, dynamicSelection, autoAssign) {
    selector.select2({
        tags: dynamicSelection,
        minimumInputLength: 0,
        multiple: true,
        placeholder: 'Enter Email',
        allowClear: false,
        width: '100%',
        separator: ";",
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: sharedWithListUrl,
            data: function (params) {
                if(autoAssign){
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale,
                        autoAssign: true
                    };
                } else {
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale,
                    };
                }
            },
            processResults: function (data, params) {
                params.page = params.page || 10;
                return {
                    results: data,
                    pagination: {
                        more: (params.page * 10) < data.length
                    }
                };
            }
        }
    });
    selector.next(".select2-container").css('display', 'block');
    if (sharedWithData) {
        $.each(sharedWithData, function(i, data){
            var option = new Option(encodeToHTML(data.name), data.id, true, true);
            selector.append(option).trigger('change.select2');
        });
    }
}

function bindShareWith2WithData(selector, sharedWithListUrl, sharedWithData, isWorkflowEnabled, autoAssign, alertType) {
    selector.select2({
        minimumInputLength: 0,
        multiple: true,
        placeholder: alertType ? '' :'Select Share With',
        width: "100%",
        separator: ";",
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: sharedWithListUrl,
            data: function (params) {
                if(autoAssign){
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale,
                        isWorkflowEnabled: isWorkflowEnabled,
                        alertType: alertType,
                        autoAssign: true
                    };
                } else {
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale,
                        isWorkflowEnabled: isWorkflowEnabled,
                        alertType: alertType
                    };
                }
            },
            processResults: function (data, params) {
                params.page = params.page || 10;
                return {
                    results: data,
                    pagination: {
                        more: (params.page * 10) < data.length
                    }
                };
            }
        }
    });
    if (sharedWithData) {
        $.each(sharedWithData, function(i, data){
            if(!isValueInSelect(selector, data.id)){
                var option = new Option(encodeToHTML(data.name), data.id, true, true);
                selector.append(option).trigger('change.select2');
            }
        });
    }
}
function isValueInSelect($select, data_value){
    return $($select).children('option').map(function(index, opt){
        return opt.value;
    }).get().includes(data_value);
}


function setDataInSelect2(data) {
    var option = new Option(data.value, data.key, true, true);
    selector.append(option).trigger('change');
}

var signal = signal || {};

signal.user_group_utils = (function () {

    var bind_assign_to = function (selector, url, data, allowClear) {
        selector.select2({
            minimumInputLength: 0,
            multiple: false,
            placeholder: 'Select Assigned To',
            allowClear: allowClear ?? true,
            width: "100%",
            ajax: {
                quietMillis: 250,
                dataType: "json",
                url: url,
                data: function (params) {
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale
                    };
                },
                processResults: function (data, params) {
                    params.page = params.page || 10;
                    return {
                        results: data,
                        pagination: {
                            more: (params.page * 10) < data.length
                        }
                    };
                }
            }
        });
        if (data) {
            var option = new Option(encodeToHTML(data.name), data.id, true, true);
            selector.append(option).trigger('change.select2');
        }
    };

    var bind_assignTo_to_grid_row = function ($row, url, data) {
        //Bind AssignedTo Select Box
        bind_assign_to($row.find('.assignedToSelect'), url, data);
    };

    var bind_assignTo_to_rmm_row = function ($row, url, data) {
        //Bind AssignedTo Select Box
        bind_assign_to_tags($row.find('.assignedToSelect'), url, data);
    };

    var bind_assignTo_to_signal_memo_row = function ($row, url, data) {
        //Bind AssignedTo Select Box
        bind_assign_to_tags_multiple_select($row.find('.assignedToSelect'), url, data);
    };


    var bind_assign_to_tags = function (selector, url, data) {
        selector.select2({
            tags: true,
            minimumInputLength: 0,
            multiple: false,
            placeholder: 'Select Assigned To',
            allowClear: true,
            width: "100%",
            ajax: {
                quietMillis: 250,
                dataType: "json",
                url: url,
                data: function (params) {
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale
                    };
                },
                processResults: function (data, params) {
                    params.page = params.page || 10;
                    return {
                        results: data,
                        pagination: {
                            more: (params.page * 10) < data.length
                        }
                    };
                }
            }
        });
        if (data) {
            var option = new Option(encodeToHTML(data.name), data.id, true, true);
            selector.append(option).trigger('change.select2');
        }
    };

    var bind_assign_to_tags_multiple_select = function (selector, url, sharedWithData) {
        selector.select2({
            tags: true,
            minimumInputLength: 0,
            multiple: true,
            placeholder: 'Select Assigned To',
            allowClear: false,
            width: "100%",
            ajax: {
                quietMillis: 250,
                dataType: "json",
                url: url,
                data: function (params) {
                    return {
                        term: params.term,
                        max: params.page || 10,
                        lang: userLocale
                    };
                },
                processResults: function (data, params) {
                    params.page = params.page || 10;
                    return {
                        results: data,
                        pagination: {
                            more: (params.page * 10) < data.length
                        }
                    };
                }
            }
        });
        if (sharedWithData) {
            $.each(sharedWithData, function (i, data) {
                if(data.name != 'null' && data.id != 'null'){
                    if(!isValueInSelect(selector, data.id)){
                        var option = new Option(encodeToHTML(data.name), data.id, true, true);
                        selector.append(option).trigger('change');
                    }
                }
            });
        }
    };

    function isValueInSelect($select, data_value){
        return $($select).children('option').map(function(index, opt){
            return opt.value;
        }).get().includes(data_value);
    }

    var triggeredElementData;
    var selectedRowIndex;
    var clonedElement;
    var clonedElementData;
    var bind_assignTo_selection = function (assignToGroupUrl, table, changeAssigneIsAllowed) {
        $(document).on("select2:opening", '.assignedToSelect', function (e) {
            if(typeof changeAssigneIsAllowed !== "undefined" && !changeAssigneIsAllowed){
                e.preventDefault();
                $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
            } else {
                clonedElement = $(e.target).clone();
                clonedElementData = $(this).select2('data');
            }
        });

        $(document).on("select2:selecting", '.assignedToSelect', function (e) {
            var selectedRowCount;
            var selectedRowCheck = false;
            selectedRowIndex = $(this).closest('tr').index();
            if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                selectedRowCheck =  $(this).closest('tr').find('.copy-select').prop('checked');
            } else {
                selectedRowCheck =  $("table.DTFC_Cloned tbody tr:nth(" + selectedRowIndex + ") .copy-select").prop("checked");
            }

            selectedRowCount = selectedCases.length;
            triggeredElementData = e.params.args.data;
            if (selectedRowCount > 1 && selectedRowCheck ) {
                assignedToBulkUpdate(selectedRowCount, selectedRowIndex)
            } else {
                $(this).siblings('i.assignToProcessing').show();
                var rowData = table.row($(this).closest('tr')).data();
                changeAssignedTo(JSON.stringify([rowData.id]), false)
            }
        });
    };

    var changeAssignedTo = function (selectedId, isBulkUpdate) {
        var selectedRowIndexes = new Set();
        var checkboxSelector;
        if(isBulkUpdate) {
            if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                checkboxSelector = 'table#alertsDetailsTable .copy-select:checked';
            } else {
                checkboxSelector = 'table.DTFC_Cloned .copy-select:checked';
            }
        }
        $.ajax({
            url: assignToGroupUrl,
            dataType: 'json',
            data: {
                assignedToValue: triggeredElementData.id,
                selectedId: selectedId,
                isArchived: isArchived
            },
            beforeSend: function () {
                if(isBulkUpdate) {
                    $.each($(checkboxSelector), function () {
                        $("table#alertsDetailsTable tr:nth-child(" + ($(this).closest('tr').index() + 1) + ") select.assignedToSelect").siblings('i.assignToProcessing').show();
                    });
                }
            },
            success: function (payload) {
                if (payload.status) {
                    $.Notification.notify('success', 'top right', "Success", payload.message, {autoHideDelay: 2000});
                    if(isBulkUpdate) {
                        $.each($(checkboxSelector), function () {
                            var option = new Option(encodeToHTML(triggeredElementData.text), triggeredElementData.id.split('_')[1], true, true);
                            $("table#alertsDetailsTable tr:nth-child(" + ($(this).closest('tr').index() + 1) + ") select.assignedToSelect").append(option).trigger('change');
                        });
                    }
                    $('i.assignToProcessing').hide();
                    caseJsonArrayInfo = [];
                    selectedCases = [];
                    selectedCasesInfo = [];
                    selectedCasesInfoSpotfire = [];
                    $(".copy-select").prop('checked', false);
                    $(".select-all-check input#select-all").prop('checked', false);
                    prev_page = [];
                    if (applicationName != "EVDAS Alert" || applicationName != 'Ad-Hoc Alert'){
                        alertIdSet.clear();
                    }
                } else {
                    $.Notification.notify('error', 'top right', "Error", payload.message, {autoHideDelay: 2000});
                }
                if(assignToGroupUrl.includes("adHocAlert")) {
                    table.draw();
                }
            },
            error: function () {
                $.Notification.notify('error', 'top right', "Failed", 'Something Went Wrong, Please Contact Your Administrator.', {autoHideDelay: 10000});
            }
        });
    };

    var assignedToBulkUpdate = function (totalSelectedRecords, selectedRowIndex) {
        var textToDisplay;
        switch (applicationName) {
            case 'Single Case Alert':
                textToDisplay = 'Case';
                break;
            case 'Aggregate Case Alert':
                textToDisplay = 'PEC';
                break;
            case 'EVDAS Alert':
                textToDisplay = 'PEC';
                break;
            case 'Ad-Hoc Alert':
                textToDisplay = 'Observation';
                break;
            case 'Literature Search Alert':
                textToDisplay = 'Article';
                break;
        }

        bootbox.dialog({
            title: 'Apply To All',
            message: signal.utils.render('bulk_operation_options', {
                totalSelectedRecords: totalSelectedRecords,
                alertType: textToDisplay
            }),
            buttons: {
                ok: {
                    label: "Ok",
                    className: 'btn btn-primary',
                    callback: function () {
                        switch ($('input[name=bulkOptions]:checked').val()) {
                            case 'current':
                                var rowData = table.rows(selectedRowIndex).data()[0];
                                $("table#alertsDetailsTable tbody tr:nth(" + selectedRowIndex + ") select.assignedToSelect").siblings('i.assignToProcessing').show();
                                changeAssignedTo(JSON.stringify([rowData.id]), false);
                                break;
                            case 'allSelected':
                                changeAssignedTo(JSON.stringify(selectedCases), true);
                                break;
                        }
                    }
                },
                cancel: {
                    label: "Cancel",
                    className: 'btn btn-default',
                    callback: function () {
                        $("table#alertsDetailsTable tbody tr:nth(" + selectedRowIndex + ") select.assignedToSelect").next().remove();
                        $("table#alertsDetailsTable tbody tr:nth(" + selectedRowIndex + ") select.assignedToSelect").replaceWith(clonedElement);
                        bind_assign_to($("table#alertsDetailsTable tbody tr:nth(" + selectedRowIndex + ") select.assignedToSelect"), searchUserGroupListUrl, {
                            name: typeof clonedElementData[0].text != "undefined" ? clonedElementData[0].text : "",
                            id: typeof clonedElementData[0].id  != "undefined"   ? clonedElementData[0].id   : ""
                        });
                    }
                }
            }
        });
    };



    return {
        bind_assign_to: bind_assign_to,
        bind_assignTo_to_grid_row: bind_assignTo_to_grid_row,
        bind_assignTo_to_rmm_row: bind_assignTo_to_rmm_row,
        bind_assignTo_to_signal_memo_row: bind_assignTo_to_signal_memo_row,
        bind_assignTo_selection: bind_assignTo_selection
    }
})();