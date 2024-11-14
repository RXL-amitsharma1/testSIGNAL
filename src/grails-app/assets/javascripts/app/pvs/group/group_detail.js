$(document).ready(function () {
    $("#addBulkGroups").on('click', function () {
        $.ajax({
            url: "/signal/group/addGroupsToReports",
            success: function (data) {
                if (data.status === 200) {
                    $.Notification.notify('success', 'top right', "Success", "User Groups added to PVR", {autoHideDelay: 10000});
                } else {
                    $.Notification.notify('error', 'top right', "Failed", "User Groups could not be added to PVR", {autoHideDelay: 10000});
                }
                $('#copyBulkGroupsModal').modal('hide');
            },
            error: function (data) {
                $.Notification.notify('error', 'top right', "Failed", "User Groups could not be added to PVR", {autoHideDelay: 10000});
                $('#copyBulkGroupsModal').modal('hide');
            }
        });
    });

    $('#groupTable').DataTable({
        "sPaginationType": "bootstrap",
        "oLanguage": {
            "url": "/assets/i18n/dataTables_" + userLocale + ".json",
            "sProcessing": '<div class="grid-loading"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>',
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
        },
        "aLengthMenu": [[25, 50, 100, -1], [25, 50, 100, "All"]],
        scrollX: true,
        "aaSorting": [[3, "desc"]],
        "aoColumnDefs": [
            {"bSearchable": false, "aTargets": [6]},
            {"bSortable": false, "aTargets": [6]}
        ]
    });

});