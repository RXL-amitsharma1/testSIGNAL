$(document).ready(function () {

    var testTable = $('#excelDataTable').DataTable({
        scrollY: 300,
        scrollX: true,
        scrollCollapse: true,
        ajax: {
            "url": testCaseUrl,
            "dataSrc": "aaData"
        },
        'columnDefs': [{
            'targets': 0,
            'className': 'dt-body-center',
            'render': function (data, type, full, meta) {
                return '<input type="checkbox" name="id[]" value="'
                    + $('<div/>').text(data).html() + '">';
            }
        }],
        'order': [[2, 'asc']],
        aoColumns: [
            {
                "mData": "id",
                "mRender": function (data, type, full, meta) {
                    return '<input type="checkbox" name="id[]" value="'
                        + $('<div/>').text(data).html() + '">';
                },
            },
            {
                "mData": "alertType",
                "className": 'col-min-150 col-max-500 cell-break',
                "bSortable": true
            },
            {
                "mData": "owner",
                "bSortable": true
            },
            {
                "mData": "dataSource",
                "bSortable": true
            },
            {
                "mData": "products",
                "className": 'col-min-150',
                "bSortable": true
            },
            {
                "mData": "isAdhoc",
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == true) {
                        return '<span>' + "Yes" + '</span>'
                    } else
                        return '<span>' + 'No' + '</span>'
                }
            },
            {
                "mData": "isExcludeFollowUp",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    console.log("Hi come here " + data)
                    if (data == null) {
                        return '<span>' + "-" + '</span>'
                    }
                    else if(data == true) {
                        return '<span>' + "Yes" + '</span>'
                    } else
                        return '<span>' + 'No' + '</span>'
                }
            },
            {
                "mData": "isDataMiningSMQ",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == null) {
                        return '<span>' + "-" + '</span>'
                    }
                    else if(data == true) {
                        return '<span>' + "Yes" + '</span>'
                    } else
                        return '<span>' + 'No' + '</span>'
                }
            },
            {
                "mData": "isExcludeNonValidCases",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == null) {
                        return '<span>' + "-" + '</span>'
                    }
                    else if(data == true) {
                        return '<span>' + "Yes" + '</span>'
                    } else
                        return '<span>' + 'No' + '</span>'
                }
            },
            {
                "mData": "isIncludeMissingCases",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == null) {
                        return '<span>' + "-" + '</span>'
                    }
                    else if(data == true) {
                        return '<span>' + "Yes" + '</span>'
                    } else
                        return '<span>' + 'No' + '</span>'
                }
            },
            {
                "mData": "isApplyAlertStopList",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == null) {
                        return '<span>' + "-" + '</span>'
                    }
                    else if(data == true) {
                        return '<span>' + "Yes" + '</span>'
                    } else
                        return '<span>' + 'No' + '</span>'
                }
            },
            {
                "mData": "isIncludeMedicallyConfirmedCases",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == null) {
                        return '<span>' + "-" + '</span>'
                    }
                    else if(data == true) {
                        return '<span>' + "Yes" + '</span>'
                    } else
                        return '<span>' + 'No' + '</span>'
                }
            },
            {
                "mData": "dateRangeType",
                "bSortable": true,
                "render": function (data, type, row) {
                    return data
                }
            },
            {
                "mData": "dateRange",
                "bSortable": true,
                "render": function (data, type, row) {
                    return data
                }
            },
            {
                "mData": "xForDateRange",
                "bSortable": true
            },
            {
                "mData": "startDate",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == "null") {
                        return '<span>' + "-" + '</span>'
                    } else
                        return '<span>' + data + '</span>'
                }
            },
            {
                "mData": "endDate",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == "null") {
                        return '<span>' + "-" + '</span>'
                    } else
                        return '<span>' + data + '</span>'
                }
            },
            {
                "mData": "evaluateCaseDateOn",
                "bSortable": true
            },
            {
                "mData": "versionAsOfDate",
                "className": 'col-min-150',
                "bSortable": true,
                "mRender": function (data, type, row) {
                    if (data == "null") {
                        return '<span>' + "-" + '</span>'
                    } else
                        return '<span>' + data + '</span>'
                }
            },
            {
                "mData": "drugType",
                "bSortable":true
            },
            {
                "mData": "priority",
                "bSortable": true
            },
            {
                "mData": "assignedTo",
                "bSortable": true
            },
            {
                "mData": "shareWith",
                "className": 'col-min-150',
                "bSortable": true
            },
            {
                "mData": "limitCaseSeries",
                "className": 'col-min-150'
            }
        ]
    });


    $('#excelDataTable tbody').on('change', 'input[type="checkbox"]', function () {

        console.log(testTable.row($(this).closest('tr')).data())

        // If checkbox is not checked
        if (!this.checked) {
            var el = $('#excelDataTable-select-all').get(0);
            // If "Select all" control is checked and has 'indeterminate' property
            if (el && el.checked && ('indeterminate' in el)) {
                // Set visual state of "Select all" control
                // as 'indeterminate'
                el.indeterminate = true;
            }
        }
    });

    $('#send-selected-cases').on('click', function () {

        // Iterate over all checkboxes in the table
        testTable.$('input[type="checkbox"]').each(function () {
            if (this.checked) {
                // Create a hidden element
                //selectedRowData.push({'data':testTable.row($(this).closest('tr')).data()})
                selectedRowData.push(testTable.row($(this).closest('tr')).data())
            }

        })
        var data = {
            selectedRowData: JSON.stringify(selectedRowData)
        }
        console.log(selectedRowData);
        $.ajax({
            type: "POST",
            url: "/signal/TestSignalRest/handleSelectedCases",
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(selectedRowData),
            dataType: "json",
            success: function (data) {

            }
        });
    });

    $('#excelDataTable-select-all').on('click', function () {
        // Check/uncheck all checkboxes in the table
        var rows = testTable.rows({'search': 'applied'}).nodes();
        $('input[type="checkbox"]', rows).prop('checked', this.checked);
    });

    $('.select-check-box').click(function () {

    });

    $(document).on('click', '.file-uploader .browse', function () {
        var fileUploaderElement = $(this).closest('.file-uploader');
        var file = fileUploaderElement.find('.file');
        var fileName = fileUploaderElement.find('.form-control').val();
        file.trigger('click');
    });
    $(document).on('change', '.file-uploader .file', function () {
        var currentElement = $(this);
        var inputBox = currentElement.parent('.file-uploader').find('.form-control');
        if (!_.isEmpty(currentElement.val())) {
            inputBox.val(currentElement.val().match(/[^\\/]*$/)[0]);
        }
        inputBox.trigger('change');
    });

    function uploadFileAjax($this, formData) {
        $(this).find(".upload").attr("disabled", true);
        $.ajax({
                url: uploadFileUrl,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (response) {
                    $(this).find(".upload").attr("disabled", false);
                    $('#importTestFileUploadForm')[0].reset();
                    if (response.status) {
                        $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 10000});
                    } else {
                        $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 10000});
                    }
                    $('#excelDataTable').DataTable().ajax.reload()
                },
                error: function () {
                    $this.find(".upload").attr("disabled", false);
                    $.Notification.notify('error', 'top right', "Error", "Sorry, This File Format Not Accepted", {autoHideDelay: 10000});
                }
            }
        );
    }

    $('#importTestFileUploadForm').submit(function (e) {
        e.preventDefault();
        var formData = new FormData(this);

        var $this = $(this);
        if ($this.find('.file').val()) {
            if (fileExists) {
                bootbox.confirm({
                    message: $.i18n._('fileExists'),
                    buttons: {
                        confirm: {
                            label: 'Yes',
                            className: 'btn-primary'
                        },
                        cancel: {
                            label: 'No',
                            className: 'btn-default'
                        }
                    },
                    callback: function (result) {
                        if (result) {
                            uploadFileAjax(this, formData)
                        }
                    }
                })
            } else {
                uploadFileAjax(this, formData)
            }
        }
    });


});