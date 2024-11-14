$(document).ready(function () {
    dataSheetOptions();
    var data;
    if (typeof dataSheets === "undefined" ||  dataSheets === "" ) {
        data = {}
    } else {
        data = JSON.parse(dataSheets);
    }
    $("input[name='allSheets']").change(function () {
        $("#selectedDatasheets").val(!$("input[type='checkbox'][name='allSheets']:checked").val());
        $('#dataSheet').select2({placeholder: $.i18n._("selectOne"), allowClear:true});
        bindDatasheet2WithData($("#dataSheet"), dataSheetList, data);
    });
    bindDatasheet2WithData($("#dataSheet"), dataSheetList, data);

    $("#dataSheet").on('scroll', function() {
        if($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight) {
            alert('end reached');
        }
    });
});

function dataSheetOptions() {
    var dataSheetOptions = $(".datasheet-options");
    dataSheetOptions.hide();
    $('#selectedDatasheet').change(function() {
        if(!$(this).is(":checked")) {
            dataSheetOptions.hide();
        }else {
            dataSheetOptions.show();
        }
        $('#dataSheet').data('select2').selection.resizeSearch()
    });
}
$("#allSheets").change(function(){
    if($(this).is(':checked')){
        $(this).val('ALL_SHEET');
    }else{
        $(this).val('CORE_SHEET');
    }
});
function bindDatasheet2WithData(selector, dataSheetList, data) {
    return bindDataSheet2WithData(selector, dataSheetList, data);
}

