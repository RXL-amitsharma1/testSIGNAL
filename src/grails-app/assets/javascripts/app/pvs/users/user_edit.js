$(document).ready(function () {

    var init_page = function () {
        $("#locale").select2();
        $("#timeZone").select2();

        //Set defaults for switch
        $(":checkbox").not("[id$=_exactSearch], [id$=_exactSearchAssessment], [id$=product_multiIngredient_assessment]").bootstrapSwitch('size', 'small');
        $(":checkbox").not("[id$=_exactSearch], [id$=_exactSearchAssessment], [id$=product_multiIngredient_assessment]").bootstrapSwitch('onText', 'Yes');
        $(":checkbox").not("[id$=_exactSearch], [id$=_exactSearchAssessment], [id$=product_multiIngredient_assessment]").bootstrapSwitch('offText', 'No');
        $(":checkbox").not("[id$=_exactSearch], [id$=_exactSearchAssessment], [id$=product_multiIngredient_assessment]").bootstrapSwitch();
        $(":checkbox").on('switchChange.bootstrapSwitch', function (event, state) {
            if (state == true) {
                $(this).parent().parent().parent().find("input").attr("value", "on")
            } else {
                $(this).parent().parent().parent().find("input").attr("value", "off")
            }
        });

        $("#username").select2({
            placeholder: "Enter a username",
            minimumInputLength: 3,
            multiple: false,
            ajax: {
                quietMillis: 100,
                dataType: "json",
                url: "/signal/user/ajaxLdapSearch",
                data: function (params) {
                    return {
                        term: params.term,
                        max: params.page || 10
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
        }).on('select2:select', function (e) {
            $('#username').val(e.params.data.id);
        });
    }

    var getUrl = function () {

    }

    init_page();

});


