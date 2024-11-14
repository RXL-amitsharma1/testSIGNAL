$(document).ready(function () {
    var validatedValue = $('#validatedConfirmed').is(":checked");
    var closedValue = $('#closed').is(":checked");
    if(!isAdmin){
        $('#dispositionForm input,select').prop('disabled', true);
    }

    var checkForConfirmationModel = function (checkBoxId, checkBoxSecondId, initialValue){
        if($(checkBoxId).is(":checked")){
            if(initialValue) {
                var id = $(document).find("#instanceId").val();
                $.ajax({
                    url : "/signal/disposition/checkForAlerts",
                    data:{id:id}

                }).success(function(data) {
                    var confirmModel = $("#dispositionConfirmation")
                    confirmModel.modal('show')

                    $(".cancel-change").unbind().click(function () {
                        changeCheckboxValues(checkBoxSecondId, checkBoxId)
                    })

                    $(".confirm-change").unbind().click(function () {
                        changeCheckboxValues(checkBoxId, checkBoxSecondId)
                    })
                }).error(function () {
                    changeCheckboxValues(checkBoxId, checkBoxSecondId)
                });
            }
            if($(checkBoxSecondId).is(":checked")) {
                changeCheckboxValues(checkBoxId, checkBoxSecondId)
            }
        }
    };
    var changeCheckboxValues = function (markChecked, markUnchecked){
        $(markUnchecked).prop('checked',false)
        $(markChecked).prop('checked',true)
    };

    var toggleCheckBoxes = function() {
        if ($('#validatedConfirmed').prop("checked") == true ||
            $('#closed').prop("checked") == true) {
            $('#reviewCompleted').prop('checked', true);
            $('#resetReviewProcess').prop('checked', true);
        } else {
            $('#reviewCompleted').prop('checked', false);
            $('#resetReviewProcess').prop('checked', false);
        }
    }


    $("#closed").click(function () {
        checkForConfirmationModel("#closed","#validatedConfirmed", validatedValue);
        toggleCheckBoxes();
    });
    $("#validatedConfirmed").click(function () {
        checkForConfirmationModel("#validatedConfirmed", "#closed", closedValue);
        toggleCheckBoxes();
    });
    $("#reviewCompleted").click(function () {
        if ($('#reviewCompleted').prop("checked") == true) {
            $('#resetReviewProcess').prop('checked', true);
        } else {
            $('#resetReviewProcess').prop('checked', false);
        }
    });
});