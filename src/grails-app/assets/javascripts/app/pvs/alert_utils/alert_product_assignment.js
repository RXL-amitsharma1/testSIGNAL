$(document).ready(function () {
    $("#productModal").on('hidden.bs.modal', function () {
        const searchIcon = document.getElementsByClassName("iconSearch");
        if (searchIcon && searchIcon.length > 0) {
            searchIcon[0].addEventListener("focusout", (event) => {
                event.target.style.background = "";
            });
        }
        if($("#selectedDatasource").val()) {
            let productAssignment = $("#productSelection").val();
            let productGroupAssignment = $("#productGroupSelection").val() !== "[]" ? $("#productGroupSelection").val() : "";
            let data = {};
            data["productAssignment"] = productAssignment;
            data["productGroupAssignment"] = productGroupAssignment;
            var levelNames = options.product.levelNames.split(',');
            var userAssignmentIndex = levelNames.findIndex(x => x ==="User Assignment") + 1;
            var productGroupJsonSize = getDictionaryObject("product").getValuesDicGroup().length;
            var productJsonSize = Object.values(getDictionaryObject("product").getValues()).map(arr => arr.length);
            var totalProductSelected = productJsonSize.reduce((acc, curr) => acc + curr, 0);
            if ((productAssignment || productGroupAssignment) && productGroupJsonSize <= 1 && totalProductSelected <= 1) {
                let changeValue = true;
                if(productAssignment){
                    let productMap=JSON.parse(productAssignment);
                    if(productMap[userAssignmentIndex] && productMap[userAssignmentIndex].length){
                        var dataNameId = productMap[userAssignmentIndex][0];
                        var selectorAssigned = $('#assignedTo');
                        var selectorShare = $('#sharedWith');
                        var userOrGroupId;
                        if(userIdList.findIndex(x => x === dataNameId.id) === -1) {
                            userOrGroupId = "UserGroup_" + dataNameId.id
                        } else {
                            userOrGroupId = "User_" + dataNameId.id
                        }
                        var option1 = new Option(dataNameId.name, userOrGroupId, true, true);
                        var option2 = new Option(dataNameId.name, userOrGroupId, true, true);
                        selectorAssigned.empty();
                        selectorAssigned.append(option1).trigger('change.select2');
                        selectorShare.empty();
                        selectorShare.append(option2).trigger('change.select2');
                        changeValue = false;
                    }
                }
                $.ajax({
                    url: fetchAssignmentForProductsUrl,
                    type: "POST",
                    data: data,
                    success: function (response) {
                        if (response.status === "success") {
                            let bindData = {"id": "AUTO_ASSIGN", "name": "Auto Assign"}
                            $('#assignedTo').empty()
                            bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl, bindData, false, true);
                            $('#sharedWith').empty()
                            let sharedData = [{"id": "AUTO_ASSIGN", "name": "Auto Assign"}]
                            bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedData, true, true);
                        } else if (response.status === "fail") {
                           //Removed the change value flag as according to latest requirement whenever single product is selected user assignment dropdown will always changes based on product assignment and wil be empty if no product assigned
                            $('#assignedTo').empty();
                            bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl, "", false);
                            $('#sharedWith').empty();
                            bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedWithData, true);
                        }
                    },
                    error: function () {
                        $.Notification.notify('error', 'top right', "Error", "Sorry could not fetch assignment for selected product.", {autoHideDelay: 10000});
                        bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl, "", false);
                        $('#sharedWith').empty()
                        bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedWithData, true);
                    }
                });
            } else if( productGroupJsonSize >= 1 || totalProductSelected >= 1){
                $('#assignedTo').empty()
                bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl, "", false);
                $('#sharedWith').empty()
                bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedWithData, true);
            } else if ((!productAssignment && !productGroupAssignment)) {
                if(typeof productBasedSecurity != "undefined" && productBasedSecurity == true) {
                    $('#assignedTo').empty()
                    bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl, "", false);
                    $('#sharedWith').empty()
                    bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedWithData, true);
                }
            }
        }
    });

    $("#assignedTo").change(function () {
        let selectedVal = $(this).val();
        let elem = $("#sharedWith")
        let selectedElem = elem.val();
        let isIncludes;
        if(selectedElem) {
            isIncludes = selectedElem.includes(selectedVal);
        }
        if(!isIncludes && selectedVal){
            let text = $(this).find("option[value="+selectedVal+"]").text();
            let option = new Option(text,selectedVal,true,true);
            elem.append(option).trigger("change.select2")
        }
    });

    setTimeout(function(){
        if(isAutoAssignedTo){
            let bindData ={"id": "AUTO_ASSIGN", "name": "Auto Assign"}
            $('#assignedTo').empty()
            bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl,bindData,false, true);
        }
        if(isAutoSharedWith){

            let sharedData = [{"id": "AUTO_ASSIGN", "name": "Auto Assign"}]
            _.each(sharedWithData,function (val) {
                sharedData.push(val)
            });
            $('#sharedWith').empty()

            bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedData, true, true);

        }
    }, 2000)

});
