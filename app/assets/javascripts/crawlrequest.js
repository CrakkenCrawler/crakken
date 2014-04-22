$(function() {
    $("#crawlrequests").dataTable( {
        "bProcessing": true,
        "sAjaxSource": "/crawlrequest/list",
        "aoColumns": [
            { "mData": "id" },
            { "mData": "origin" },
            { "mData": "initialRecursionLevel" },
            { "mData": "includeExternals" }
        ]
    } );
});
