function filter() {
    hideGroups();
    $('.selectGroup').val("");
    const vo = $("#selectVo").val();
    if (vo !== "") {
        showGroups();
        $(".groupOption").each(function () {
            const value = $(this).val();
            if (value.startsWith(vo, 0)) {
                $(this).show();
            } else {
                $(this).hide();
            }
        });
    }
}
function showGroups() {
    $(".selectGroup").show();
}
function hideGroups() {
    $(".selectGroup").hide();
}
$(document).ready(function() {
    $("#selectVo").val("");
});
