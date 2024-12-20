$(document).ready(function () {
    var customDateDDMMMYYYYToOrd = function (date) {
        "use strict";
        var dateParts = date.split(/-/);
        return (dateParts[2] * 10000) + ($.inArray(dateParts[1].toUpperCase(), ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"]) * 100) + (dateParts[0] * 1);
    };

    jQuery.fn.dataTableExt.aTypes.unshift(
        function (sData) {
            "use strict";
            if (/^([0-2]?\d|3[0-1])-(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)-\d{4}/i.test(sData)) {
                return 'date-dd-mmm-yyyy';
            }
            return null;
        }
    );

    jQuery.fn.dataTableExt.oSort['date-dd-mmm-yyyy-asc'] = function (a, b) {
        "use strict";
        var ordA = customDateDDMMMYYYYToOrd(a),
            ordB = customDateDDMMMYYYYToOrd(b);
        return (ordA < ordB) ? -1 : ((ordA > ordB) ? 1 : 0);
    };

    jQuery.fn.dataTableExt.oSort['date-dd-mmm-yyyy-desc'] = function (a, b) {
        "use strict";
        var ordA = customDateDDMMMYYYYToOrd(a),
            ordB = customDateDDMMMYYYYToOrd(b);
        return (ordA < ordB) ? 1 : ((ordA > ordB) ? -1 : 0);
    };

});