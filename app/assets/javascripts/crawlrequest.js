$(function() {
    $("#crawlrequests").dataTable( {
        "bProcessing": true,
        "sAjaxSource": "/crawlrequest/list",
        "aoColumns": [
            {
                "mData": "id",
                "mRender": function(data,type,full) {
                    return "<a href='/crawlrequest/" + data + "'>" + data + "</a>";
                }
            },
            {
                "mData": "origin",
                "mRender": function(data,type,full) {
                    return "<a href='" + data + "'>" + data + "</a>";
                }
            },
            {
                "mData": "initialRecursionLevel"
            },
            {
                "mData": "includeExternals",
                "mRender": function(data,type,full) {
                    var icon = (data == "true") ? "ok" : "remove";
                    return "<span class='glyphicon glyphicon-" + icon + "'></span>";
                }
            }
        ]
    } );
});
