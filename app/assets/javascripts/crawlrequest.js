$(function() {
    oTable = $("#crawlrequests").dataTable( {
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

    var context = $('<a>', { href: window.location } )[0];
    var connection = new WebSocket("ws://" + context.hostname + ":" + context.port + "/status/crawlrequest");
    connection.onmessage = function(e) {
        var message = JSON.parse(e.data);
        console.log(message);
        var aoData = oTable.fnGetData();
        var index = -1;
        for (var i = 0; i < aoData.length; i++) {
            if (aoData[i].id == message.id) { index = i; break; }
        }

        if (index >= 0) oTable.fnUpdate(message, index, undefined, false, false);
        else oTable.fnAddData(message, false);
        oTable.fnStandingRedraw();
    }
});
