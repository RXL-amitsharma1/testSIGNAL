$(document).ready( function () {
    var create = " ";
    var counter = $('#selectedCount');
    $("#selectedColumns").change(function() {
        counter.text($("#selectedColumns :selected").length);
    });
    var edit = ($('#edit').val() === 'true');
    $("#category").select2({
        placeholder: "Search for a category",
        allowClear: true,
        width: "100%",
        formatNoMatches: function(term) {
            if (edit) create = "<a id='addNew' class='btn btn-success'>Create</a>";
            else create = editMessage;
            return "<input readonly='readonly' class='form-control' id='newCategory' value='" + term + "'>" + create;
        }
    }).parent().find(".select2-drop").on("click","#addNew",function(){
        var newTerm = $("#newCategory").val();
        if ($.trim(newTerm) != " " && edit) {
            $("<option>" + newTerm + "</option>").appendTo("#category");
            $("#category").select2("val", newTerm);
            $("#category").select2("close");
        } else {
            $("#newCategory").val("");
            $("#category").select2("close");
        }
    });
    $("#s2id_autogen1").keyup(function(event){
        if(event.keyCode == 13){
            $("#addNew").click();
        }
    });
});
