//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/actions/actions.js
//= require app/pvs/meeting/meeting.js
var notificationConfigTable;
var signalOutcomesToBeDisabled = `${signalOutcomesToBeDisabled}`;

$(document).ready(function () {

    $("#signalSource").select2({
        multiple: true,
        width: '100%'
    });
    $("#triggerVariable").select2();
    $("#signalOutcomes").select2({ multiple: true, width: '100%' });
    $("#actionsTaken").select2({ multiple: true, width: '100%' });

    $(document).on('click', '.edit-signal-memo', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.removeClass('readonly');
        currRow.find('.form-control').removeAttr('disabled');
        currRow.find('.select2').next(".select2-container").show();
        currRow.find('.remove-edit-memo').removeClass('hide');
        currRow.find('.table-row-edit').addClass('hide');
        currRow.find('.table-row-saved').removeClass('hide').addClass('hidden-ic');
        currRow.find('.trigger-value-drop, .email-body-text, .email-subject-text, .delete-signal-memo').addClass('hide');
        currRow.find('.memo-address, .trigger-val-text, .trigger-var-text, .signal-source, .config-name-text').addClass('hide');
        currRow.find('.email-body-input,.comment-on-edit').removeClass('hide');
        currRow.find('.email-subject-input, .email-body-input').removeClass('hide');
        currRow.find('.config-input, .triggerVariable').removeClass('hide');
        currRow.find('.triggerVariable').next(".select2-container").css('display','block');
        currRow.find('.triggerVariable').addClass('edit-trig-val');
        currRow.find('.signalSource').next(".select2-container").css('display','block')
        if(typeof currRow.find('.signal-source').text() != 'undefined' && !currRow.find('.signal-source').text()){
            currRow.find('.signalSource').val(null).trigger("change");
        }
        currRow.find('.assignedToSelect').next(".select2-container").css('display','block')
        if(currRow.find('.triggerVariable').val() == 'Signal Outcome'){
            currRow.find('.signalOutcomes').next(".select2-container").css('display','block')
        } else if(currRow.find('.triggerVariable').val() == 'Action Taken') {
            currRow.find('.actionsTaken').next(".select2-container").css('display','block')
        } else {
            currRow.find('.trigger-val-input').removeClass('hide');
        }
    });

    $(document).on('click', '.remove-edit-memo', function (e) {
        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.removeClass('readonly');
        currRow.find('.form-control').prop('disabled', true);
        currRow.find('.select2').next(".select2-container").hide();
        currRow.find('.remove-edit-memo').addClass('hide');
        currRow.find('.table-row-edit').removeClass('hide');
        currRow.find('.table-row-saved').addClass('hide').removeClass('hidden-ic');
        currRow.find('.trigger-value-drop, .email-body-text, .email-subject-text, .delete-signal-memo').removeClass('hide');
        currRow.find('.memo-address, .trigger-val-text, .trigger-var-text, .signal-source, .config-name-text').removeClass('hide');
        currRow.find('.email-body-input,.comment-on-edit').addClass('hide');
        currRow.find('.trigger-val-input, .email-subject-input, .email-body-input').addClass('hide');
        currRow.find('.config-input, .triggerVariable').addClass('hide');
        currRow.find('.triggerVariable').next(".select2-container").css('display','none');
        currRow.find('.triggerVariable').removeClass('edit-trig-val');
        currRow.find('.signalSource').next(".select2-container").css('display','none');
        currRow.find('.assignedToSelect').next(".select2-container").css('display','none');
        if(currRow.find('.triggerVariable').val() == 'Signal Outcome'){
            currRow.find('.signalOutcomes').next(".select2-container").css('display','none');
        } else if(currRow.find('.triggerVariable').val() == 'Action Taken') {
            currRow.find('.actionsTaken').next(".select2-container").css('display','none');
        } else {
            currRow.find('.trigger-val-input').addClass('hide');
        }
        $('#signalMemoReportTable').DataTable().columns.adjust().draw();
    });

    $('#signalSource').next(".select2-container").css('display','block');
    $('#signalOutcomes').next(".select2-container").css('display','none');
    $('#actionsTaken').next(".select2-container").css('display','none');
    $(document).on('change', '#triggerVariable, .edit-trig-val', function () {
        var currRow = $(this).closest('tr');
        if ($(this).val() == 'Signal Outcome') {
            currRow.find('.signalOutcomes').next(".select2-container").css('display','block');
            currRow.find('.trigger-value').css('display','none');
            currRow.find('.actionsTaken').next(".select2-container").css('display','none');
        } else if ($(this).val() == 'Action Taken') {
            currRow.find('.actionsTaken').next(".select2-container").css('display','block');
            currRow.find('.signalOutcomes').next(".select2-container").css('display','none');
            currRow.find('.trigger-value').hide();
        } else {
            currRow.find('.trigger-value').removeClass('hide').css('display','block');
            currRow.find('.trigger-val-input').removeClass('hide')
            currRow.find('.signalOutcomes').next(".select2-container").css('display','none');
            currRow.find('.actionsTaken').next(".select2-container").css('display','none');
        }
    });

    function checkIfConfigExistsWithConfigName(configName, configId, triggerVariable, callback, $this){
        var id;
        if(typeof configId != 'undefined' && configId){
            id = configId;
        }
        var data = {
            configName: configName,
            triggerVariable: triggerVariable,
            configId: id,
        };
        $.ajax({
            url: configExistsUrl,
            type: "POST",
            data: data,
            success: function (data) {
                callback(data);
                $this.show();
            }
        });
    }

    function isInt(value) {
        return !isNaN(value) && (function(x) { return (x | 0) === x; })(parseFloat(value))
    }

    $(document).on('click', '.save-signal-memo', function (e) {
        e.preventDefault();
        var $this = $(this);
        var currRow = $(this).closest('tr');
        $this.hide();
        var formData = new FormData();
        var configName = currRow.find('.config-name').val();
        var signalSource = currRow.find('.signalSource').val();
        var triggerVariable = currRow.find('.triggerVariable').val();
        var triggerValue;
        if(currRow.find('.triggerVariable').val() == 'Signal Outcome'){
            triggerValue = currRow.find('.signalOutcomes').val();
        } else if(currRow.find('.triggerVariable').val() == 'Action Taken') {
            triggerValue = currRow.find('.actionsTaken').val();
        } else {
            triggerValue = currRow.find('.trigger-value').val();
        }
        var mailAddresses = currRow.find('.assignedToSelect').val();
        var configId;
        var emailSubject = currRow.find('.email-subject').val();
        var emailBody = currRow.find('.email-body').val();
        if (!currRow.parent().is('tfoot')) {
            formData.append("signalMemoId", notificationConfigTable.rows(currRow).data()[0].id)
            configId = notificationConfigTable.rows(currRow).data()[0].id;
        }
        formData.append("configName",configName);
        formData.append("signalSource",signalSource); noSelection="['': 'Select']"
        formData.append("triggerVariable",triggerVariable);
        formData.append("triggerValue",triggerValue);
        formData.append("mailAddresses",mailAddresses);
        formData.append("emailSubject",emailSubject);
        formData.append("emailBody",emailBody);
        var mandatoryFields = false;
        checkIfConfigExistsWithConfigName(configName, configId, triggerVariable, statusOfConfigName, $this);
        function statusOfConfigName(data) {
            if (typeof data != 'undefined' && data.status) {
                mandatoryFields = configName && triggerVariable && triggerValue && mailAddresses;
            } else {
                mandatoryFields = configName && triggerVariable && triggerValue && mailAddresses && emailSubject && emailBody;
            }
             if(!mandatoryFields){
                $.Notification.notify('error', 'top right', "Error", "Please provide information for all the mandatory fields which are marked with an asterisk (*)", {autoHideDelay: 40000});
            }else if(triggerVariable !== "Signal Outcome" &&  triggerVariable !== "Action Taken" && !isInt(triggerValue)){
                $.Notification.notify('warning', 'top right', "Warning", "Trigger value should be in numeric format without any decimals.", {autoHideDelay: 40000});
            }
             else if (data.status && data.data === triggerVariable) {
                $.Notification.notify('warning', 'top right', "Warning", "Same trigger variable should be used only once in a configuration.", {autoHideDelay: 40000});
            } else {
                $.ajax({
                    url: saveSignalMemoConfigUrl,
                    type: "POST",
                    mimeType: "multipart/form-data",
                    processData: false,
                    contentType: false,
                    data: formData,
                    success: function (data) {
                        $response = $.parseJSON(data);
                        if ($response.status) {
                            $.Notification.notify('success', 'top right', "Success", "Record saved successfully", {autoHideDelay: 20000});
                            notificationConfigTable.ajax.reload();
                            if (!notificationConfigTable.data().count()) {
                                $(".dataTables_scrollBody #signalMemoReportTable").toggle();
                            }
                            if (currRow.parent().is('tfoot')) {
                                $(".signal-memo-new-row").hide();
                            }
                        } else {
                            $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 20000});
                        }
                    }
                });
            }
        }
    });

    $(document).on('click', '.delete-signal-memo', function (e) {
        e.preventDefault();
        $(this).hide();
        var currRow = $(this).closest('tr');
        var formData = new FormData();
        formData.append("signalMemoId", notificationConfigTable.rows(currRow).data()[0].id)
        $.ajax({
            url: deleteSignalMemoConfigUrl,
            type: "POST",
            mimeType: "multipart/form-data",
            processData: false,
            contentType: false,
            data: formData,
            success: function (data) {
                $response = $.parseJSON(data);
                if ($response.status) {
                    $.Notification.notify('success', 'top right', "Success", "Record deleted successfully", {autoHideDelay: 20000});
                    notificationConfigTable.ajax.reload();
                    if (!$response.data) {
                        $('#signalMemoReportTable td.dataTables_empty').toggle();
                    }
                    $(".signal-memo-new-row").hide();
                } else {
                    $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 20000});
                }
            }
        })
    });

    if(signalOutcomesToBeDisabled.length > 0){
        console.log(signalOutcomesToBeDisabled)
        for (var i = 0; i < signalOutcomesToBeDisabled.length; i++) {
            var data = $('#signalOutcomes').select2('data')
            if(typeof data !== 'undefined' && data[0].text === signalOutcomesToBeDisabled[i].trim()){
                data[0].disabled = true
            }
            $('#signalOutcomes').find('option[value="' + signalOutcomesToBeDisabled[i].trim() + '"]').prop("disabled", true).trigger('change');
        }
    }

    var initSignalMemoReportTable = function () {
        var columns = create_signal_memo_report_table_columns();
        notificationConfigTable = $('#signalMemoReportTable').DataTable({
            responsive: true,
            processing: true,
            serverSide: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": signalMemoConfigUrl,
                "dataSrc": "aaData",
            },
            "dom": '<"top">frt<"row col-xs-12"<"col-xs-1 pt-8"l><"col-xs-5 dd-content"i><"col-xs-6 pull-right"p>>',
            fnDrawCallback: function (settings) {
                scrollOff();
                colEllipsis();
                webUiPopInit();
                var rowsSignalMemoReport = $('#signalMemoReportTable').DataTable().rows().data();
                pageDictionary($('#signalMemoReportTable_wrapper'), settings.json.recordsFiltered);
                showTotalPage($('#signalMemoReportTable_wrapper'), settings.json.recordsFiltered);
                if(rowsSignalMemoReport.length > 10)
                    $('.dataTables_scrollBody').css("overflow-y","scroll");
                else
                    $('.dataTables_scrollBody').css("overflow-y","none");
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu": "Show _MENU_",
                "sProcessing": '<div class="grid-loading"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>',
                "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
            },
            "rowCallback": function (row, data, index) {
                signal.user_group_utils.bind_assignTo_to_signal_memo_row($(row), searchUserGroupListUrl, data.mailAddresses);
                $(row).find('.signalSource').select2({ multiple: true });
                $(row).find('.signalOutcomes').select2({ multiple: true });
                $(row).find('.actionsTaken').select2({ multiple: true });
                $(row).find('.triggerVariable').select2();
                $(row).find('.assignedToSelect').next(".select2-container").css('display','none');
                $(row).find('.signalSource').next(".select2-container").css('display','none');
                $(row).find('.signalOutcomes').next(".select2-container").css('display','none');
                $(row).find('.actionsTaken').next(".select2-container").css('display','none');
                $(row).find('.triggerVariable').next(".select2-container").css('display','none');
                bindValuesToSelect2($(row).find('.signalSource'), data.signalSource, signalSource);
                bindValuesToSelect2($(row).find('.signalOutcomes'), data.triggerValue, signalOutcomes);
                bindValuesToSelect2($(row).find('.actionsTaken'), data.triggerValue, actionsTaken);
                bindValuesToSelect2($(row).find('.triggerVariable'), data.triggerVariable, triggerVariable);
            },
            "aaSorting": [],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "pagination": true,
            "aoColumns": columns,
            "scrollY":true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });
    };

    var create_signal_memo_report_table_columns = function () {
        var aoColumns = [
            {
                "mData": "configName",
                "className":"col-md-1-half cell-break",
                "mRender": function (data, type, row) {
                    if (!row.configName) {
                        row.configName = '';
                    }
                    var configName =  "<span class='config-name-text'>" + addEllipsisWithEscape(row.configName)+ "</span>";

                    configName +="<div class='textarea-ext config-input hide'><span class='css-truncate expandable'><span class='branch-ref css-truncate-target'><input type='text' value='" + escapeAllHTML(row.configName) + "' title='" + escapeAllHTML(row.configName) + "' class='form-control config-name comment ellipsis' maxlength='255' style='width: 100%' disabled></span></span></div>"
                    return configName
                }
            },
            {
                "mData": "signalSource",
                "className":"col-md-2 signal-source-class cell-break",
                "mRender": function (data, type, row) {
                    let signalSources = ''
                    if(row.signalSource == 'null' || !row.signalSource){
                        signalSources = '';
                    } else {
                        var signalSourceValues = row.signalSource.split(',');
                        _.each(signalSourceValues, function (obj, index) {
                            if (index == signalSourceValues.length - 1) {
                                signalSources += obj;
                            } else {
                                signalSources += obj + ", ";
                            }
                        });
                    }
                    var $select = $("<select></select>", {
                        "value": row.signalSource,
                        "class": "form-control signalSource hide",
                        "disabled": "true"
                    });
                    var signal_source = $select.prop("outerHTML");
                    signal_source += '<div class="signal-source">' + addEllipsis(signalSources) + '</div>'
                    return signal_source;
                }
            },
            {
                "mData":"triggerVariable",
                "className":"col-md-1-half cell-break text-left",
                "mRender": function (data, type, row) {
                    if (!row.triggerVariable) {
                        row.triggerVariable = '';
                    }
                    var $select = $("<select></select>", {
                        "value": row.triggerVariable,
                        "class": "form-control triggerVariable hide",
                        "disabled": "true"
                    });
                    var trigger_variable = $select.prop("outerHTML");
                    trigger_variable +=  "<span class='trigger-var-text'>" + addEllipsis(row.triggerVariable)+ "</span>";
                    return trigger_variable;
                }
            },
            {
                "mData": "triggerValue",
                "className":"col-md-1-half trigger-val-class cell-break",
                "mRender": function (data, type, row) {
                    let triggerValues = '';
                    var trigger_value = '';
                    if (!row.triggerValue || row.triggerValue == 'null') {
                        return triggerValues;
                    }
                    if(row.triggerVariable == 'Signal Outcome' || row.triggerVariable == 'Action Taken'){
                        var triggerDropDownValues = row.triggerValue.split(',');
                        _.each(triggerDropDownValues, function (obj, index) {
                            if (index == triggerDropDownValues.length - 1) {
                                triggerValues += obj;
                            } else {
                                triggerValues += obj + ", ";
                            }
                        });
                    }
                    var $selectOutcome = $("<select></select>", {
                        "value": row.triggerValue,
                        "class": "form-control signalOutcomes hide",
                        "disabled": "true"
                    });
                    var $selectAction = $("<select></select>", {
                        "value": row.triggerValue,
                        "class": "form-control actionsTaken hide",
                        "disabled": "true"
                    });

                    if(row.triggerVariable == 'Signal Outcome' || row.triggerVariable == 'Action Taken'){
                        trigger_value = '<div class="trigger-value-drop">' + addEllipsis(triggerValues) + '</div>';
                    } else {
                        trigger_value = "<span class='trigger-val-text'>" + row.triggerValue + "</span>";
                    }
                    trigger_value +="<div class='textarea-ext trigger-val-input hide'><span class='css-truncate expandable'><span class='branch-ref css-truncate-target'><input type='number' value='" + row.triggerValue + "' title='" + row.triggerValue + "' class='form-control trigger-value comment ellipsis' min=\"0\" step=\"1\" style='width: 100%' disabled></span></span></div>"
                    trigger_value += $selectAction.prop("outerHTML");
                    trigger_value += $selectOutcome.prop("outerHTML");
                    return trigger_value;
                }
            },
            {
                "mData": "mailAddresses",
                "className":"col-md-1-half email-class cell-break",
                "mRender": function (data, type, row) {
                    var assignedTo = signal.list_utils.assigned_to_comp();
                    assignedTo += "<div class='memo-address'>" + addEllipsis(row.addresses) + "</div>";
                    return assignedTo;
                }
            },
            {
                "mData": "emailSubject",
                "className":"col-md-1-half cell-break",
                "mRender": function (data, type, row) {
                    if (!row.emailSubject) {
                        row.emailSubject = '';
                    }
                    var emailSubject =  "<span class='email-subject-text'>" + addEllipsisWithEscape(row.emailSubject)+ "</span>";

                    emailSubject +="<div class='textarea-ext email-subject-input hide'><span class='css-truncate expandable'><span class='branch-ref css-truncate-target'><input type='text' value='" + escapeAllHTML(row.emailSubject) + "' title='" + escapeAllHTML(row.emailSubject) + "' class='form-control email-subject comment ellipsis' maxlength='255' style='width: 100%' disabled></span></span></div>"
                    return emailSubject;
                }
            },
            {
                "mData": "emailBody",
                "className":"col-md-1-half cell-break",
                "mRender": function (data, type, row) {
                    if (!row.emailBody) {
                        row.emailBody = '';
                    }
                    var emailBody = "<span class='email-body-text' style='white-space:pre-wrap'>" + addEllipsisWithEscape(row.emailBody)+ "</span>";

                    emailBody +="<div class='textarea-ext email-body-input hide'><textarea title='" + escapeAllHTML(row.emailBody) + "' rows='1' class='form-control email-body' name='email-body' maxlength='4000' style='width: 100%; min-height: 25px;' disabled>" + escapeAllHTML(row.emailBody) + "</textarea> \
                         <a class='btn-text-ext openStatusComment comment-on-edit hide' href='javascript:void(0);' tabindex='0' title='Open in extended form'> \
                             <i class='mdi mdi-arrow-expand font-20 blue-1'></i></a></div>"
                    return emailBody;
                }
            },
            {
                "mData": "",
                "bSortable": false,
                "mRender": function (data, type, row) {
                    return "<a href='javascript:void(0);' title='Save' class='save-signal-memo table-row-saved hide pv-ic'> " +
                        "<i class='mdi mdi-check' aria-hidden='true'></i> </a>" +
                        "<span class='signalMemoId hide' data-signalMemoId=" + row.id + " ></span>" +
                        "<a href='javascript:void(0);' title='Edit' class='table-row-edit edit-signal-memo pv-ic hidden-ic'>" +
                        "<i class='mdi mdi-pencil' aria-hidden='true'></i>\</a>" +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del delete-signal-memo hidden-ic'> " +
                        "<i class='mdi mdi-close' aria-hidden='true'></i> \</a> " +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del remove-edit-memo hide hidden-ic'> " +
                        "<i class='mdi mdi-close' aria-hidden='true'></i> \</a> "
                },
                "className": 'col-md-1 text-center',
            }
        ];

        return aoColumns
    };

    initSignalMemoReportTable();

    $(".signal-memo-new-row").hide();
    $(document).on('click', "#notificationNewRow", function () {
        if (!notificationConfigTable.data().count()) {
            $('#signalMemoReportTable td.dataTables_empty').toggle();
            $(".dataTables_scrollBody #signalMemoReportTable").toggle();
        }
        $(".signal-memo-new-row").toggle();

        $(".signal-memo-new-row").find('.config-name').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.signalSource').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.signalOutcomes').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.actionsTaken').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.triggerVariable').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.trigger-value').Reset_List_To_Default_Value();
        $('#email-addresses').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.email-subject').Reset_List_To_Default_Value();
        $(".signal-memo-new-row").find('.email-body').Reset_List_To_Default_Value();
    });

    $("#notificationNewRow").hover(function(){
        $(this).attr("title", "Add Notification Configurations");
    });

    $(document).on('click', ".remove-signal-memo", function () {
        if (!notificationConfigTable.data().count()) {
            $('#signalMemoReportTable td.dataTables_empty').toggle();
            $(".dataTables_scrollBody #signalMemoReportTable").toggle();
        }
        $(".signal-memo-new-row").toggle();
    });

    // Resetting all select values to their default values
    $.fn.Reset_List_To_Default_Value = function () {
        $.each($(this), function (index, el) {
            var Founded = false;

            $(this).find('option').each(function (i, opt) {
                if (opt.defaultSelected) {
                    opt.selected = true;
                    Founded = true;
                }
            });
            if (!Founded) {
                if ($(this).attr('multiple')) {
                    $(this).val([]);
                }
                else {
                    $(this).val("");
                }
            }
            $(this).trigger('change');
        });
    }

    var currentRow
    $(document).on('click', '.textarea-ext .openStatusComment', function(evt) {
        evt.preventDefault();
        currentRow = $(this).closest('tr');
        var headerName = $(this).attr('data-name');
        var extTextAreaModal = $("#textarea-ext4");
        triggerChangesOnModalOpening(extTextAreaModal);
        var textArea = currentRow.find('.email-body');
        extTextAreaModal.find('.textAreaValue').val(textArea.val());
        extTextAreaModal.find('.modal-title').text('Email Body');
        extTextAreaModal.modal("show");
    });
    $(document).on('click', '.textarea-ext',function(){
       $(this).find('.countBox').addClass('hide');
    });

    function triggerChangesOnModalOpening(extTextAreaModal) {
        extTextAreaModal.on('shown.bs.modal', function () {
            $('textarea').trigger('keyup');
            //Change button label.
            if(extTextAreaModal.find('.textAreaValue').val()){
                extTextAreaModal.find(".updateTextarea").html($.i18n._('labelUpdate'));
            } else {
                extTextAreaModal.find(".updateTextarea").html($.i18n._('labelAdd'));
            }
        });
    }

    var extTextAreaModal = $("#textarea-ext4");
    $(document).on('click', '#textarea-ext4 .updateTextarea', function(evt) {
        evt.preventDefault();
        currentRow.find(".email-body").val(extTextAreaModal.find('textarea').val());
        extTextAreaModal.modal("hide");
    });

    function isValueInSelect($select, data_value){
        return $($select).children('option').map(function(index, opt){
            return opt.value;
        }).get().includes(data_value);
    }

    function bindValuesToSelect2(selector, data, dataList){
        if(data != 'null' && data){
            var values = data.split(',');
        }
        var $option;
        $.each(dataList, function (k, v) {
            _.each(values, function (obj, index) {
                if (obj === v) {
                    $option = new Option(v, v, true, true);
                    if(!isValueInSelect(selector, v)){
                        selector.append($option).trigger('change');
                    }
                }
            });
            $option = new Option(v, v, false, false);
            if(!isValueInSelect(selector, v)){
                selector.append($option).trigger('change');
            }
            if(signalOutcomesToBeDisabled.length > 0){
                for (var i = 0; i < signalOutcomesToBeDisabled.length; i++) {
                    if($option.value ===  signalOutcomesToBeDisabled[i].trim()){
                        selector.find($option).prop('disabled', true);
                    }
                }
            }
        });
    }
});