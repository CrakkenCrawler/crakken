$(function() {
    $("#pagefetchrequests").dataTable( {
        "bProcessing": true,
        "sAjaxSource": "/pagefetchrequest/listbycrid/" + crId,
        "aoColumns": [
            { "mData": "id" },
            { "mData": "statusCode" },
            { "mData": "url" }
        ]
    } );
});
