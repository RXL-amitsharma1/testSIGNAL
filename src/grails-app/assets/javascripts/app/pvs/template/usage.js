$(document).ready( function () {
    var table = $('#rxCheckTemplateUsage').DataTable({
        "sPaginationType": "bootstrap",
        "aaSorting": [[ 0, "asc" ]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
    });
    loadTableOption('#rxCheckTemplateUsage');
});

function goBack() {
    window.history.back()
}