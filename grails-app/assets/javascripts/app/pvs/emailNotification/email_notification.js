$(document).ready(function () {
    var init_page = function () {

        //Set defaults for switch
        $(":checkbox").not("[id$=_exactSearch]").bootstrapSwitch('size', 'small');
        $(":checkbox").not("[id$=_exactSearch]").bootstrapSwitch('onText', 'Yes');
        $(":checkbox").not("[id$=_exactSearch]").bootstrapSwitch('offText', 'No');

        $(":checkbox").not("[id$=_exactSearch]").bootstrapSwitch();
    };
    init_page();
});


