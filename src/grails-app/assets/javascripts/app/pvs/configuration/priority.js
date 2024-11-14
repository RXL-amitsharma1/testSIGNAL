var dispositionList = [];

$(document).ready(function () {

    if (!isAdmin) {
        $('#priorityForm input,select').prop('disabled', true);
    }
    addCountBoxToInputField(255, $('#value'));
    addCountBoxToInputField(255, $('#displayName'));
    addCountBoxToInputField(255, $('#description'));

    $("#defaultPriority").click(function() {

        var isChecked = $(this).is(":checked");
        if (isChecked) {
            $("#defaultPriority").attr('value', true);
        } else {
            $("#defaultPriority").attr('value', false);
        }

    });
    showDispositionContainer();
    fetchDispositions();
    checkBeforeSubmit();
    setDispositions();
    setWidthSelect();

});

var showDispositionContainer = function () {
    $("#reviewPeriodButton").click(function () {
        $("#dispostionContainer").show();
        if ($(".dispositionRow").length == 0) {
            var dispositionElement = signal.utils.render('disposition_review_period',
                {
                    dispositions: dispositionList,
                    reviewPeriod: "",
                    dispositionName: ""
                });
            $("#dispostionContainer").append(dispositionElement);
        }
    });
};

var addDispositionPeriod = function (container) {
    container.closest(".addDisposition").remove();
    appendRow("", "");
};

var removeDispositionElement = function (container) {
    if ($(".dispositionRow").length == 1) {
        $(container).closest(".dispositionRow").remove();
        $("#dispostionContainer").hide();
    } else {
        $(container).closest(".dispositionRow").remove();
        if($(".addExpression").length == 0)
            $('<div class="col-lg-1 addDisposition"><div class="pull-right"><span class="glyphicon glyphicon-plus btn btn-primary addExpression" onclick="addDispositionPeriod(this)"></span></div></div>').insertBefore($(".removeDisposition:last"));
    }
};

var fetchDispositions = function () {
    $.ajax({
        type: "GET",
        async: false,
        url: dispositionListUrl,
        success: function (result) {
            dispositionList = result.dispositionList;

        },
    });

};

function comparer(otherArray) {
    return function (current) {
        return otherArray.filter(function (other) {
            return other.id == current.id && other.displayName == current.displayName
        }).length == 0;
    }
};

var checkIfRowEmpty = function () {
    var isRowEmpty = false;
    $('.dispositionRow').each(function () {
        if ($(this).find(".dispositionSelect").val() == "" || $(this).find(".reviewPeriodDisposition").val() == "" || $(this).find(".dispositionSelect").val() == null)
            isRowEmpty = true
    });
    return isRowEmpty;
};

var appendRow = function (dispositionName, reviewPeriod) {
    var usedDisposition = [];
    $('.dispositionSelect option:selected').each(function () {
        usedDisposition.push({displayName: $(this).html()})
    });
    var dispositionsRemaining = dispositionList.filter(comparer(usedDisposition));
    var dispositionElement = signal.utils.render('disposition_review_period',
        {
            dispositions: dispositionsRemaining,
            reviewPeriod: reviewPeriod,
            dispositionName: dispositionName
        });
    $("#dispostionContainer").append(dispositionElement);
};

var checkBeforeSubmit = function () {
    $("#submitButton").click(function (event) {
        if(parseInt($("#reviewPeriod").val()) > 999){
            event.preventDefault();
            $.Notification.autoHideNotify('error', 'top right', "Error", "Number of days in Default Review Period can't be more than 999.", {autoHideDelay: 500});
        }else{
            if (checkIfRowEmpty()) {
                event.preventDefault();
                $.Notification.autoHideNotify('error', 'top right', "Error", "Review period is mandatory for each disposition ", {autoHideDelay: 500});
            } else{
                var dispositionPeriodList = [];
                var order = 1;
                $('.dispositionRow').each(function () {
                    var reviewPeriod = $(this).find('.reviewPeriodDisposition').val();
                    var dispositions = $(this).find('.dispositionSelect').val();
                    $.each(dispositions , function(key , value){
                        var json = {displayName : value , reviewPeriod : reviewPeriod , order : order};
                        dispositionPeriodList.push(json);
                    });
                    order = order + 1;
                });
                var dispositionsJSON = JSON.stringify(dispositionPeriodList);
                $("textarea[name='dispositions']").val(dispositionsJSON);
            }
        }
    });
};

var setDispositions = function () {
    if ($('.dispositionSelect').length > 0) {
        $('<div class="col-lg-1 addDisposition"><div class="pull-right"><span class="glyphicon glyphicon-plus btn btn-primary addExpression" onclick="addDispositionPeriod(this)"></span></div></div>').insertBefore($(".removeDisposition:last"));
        makeDispositionList();
    }
};

var makeDispositionList = function() {
    var usedDisposition = [];
    $('.dispositionSelect option:selected').each(function () {
        usedDisposition.push({displayName: $(this).html()})
    });
    var dispositionsRemaining = dispositionList.filter(comparer(usedDisposition));
    $('.dispositionSelect').each(function () {
        $("#dispostionContainer").show();
        var selectContainer = $(this);
        selectContainer.find('option').not(':selected').remove();
        selectContainer.select2({placeholder:"Select Disposition"}).data('select2').$dropdown.addClass('cell-break');
        $.each(dispositionsRemaining, function (key, value) {
            selectContainer.append(new Option(value.displayName, value.displayName));
        });
        if ($(this).val() != "" && $(this).val()!= null) {
            $(this).next().find(".select2-search__field").css({"min-width": "30px"});
        } else {
            $(this).next().find(".select2-search__field").css({"min-width": "130px"})
        }
    });
};

var setWidthSelect = function(container){
    $('.dispositionSelect').on('change', function (e) {
        makeDispositionList();
    });
}