$(function() {
    oTable = $("#pagefetchrequests").dataTable( {
        "bProcessing": true,
        "sAjaxSource": "/pagefetchrequest/listbycrid/" + crId,
        "aoColumns": [
            {
                "mData": "id"
            },
            {
                "mData": "statusCode"
            },
            {
                "mData": "url",
                "mRender": function(data,type,full) {
                    var retval;
                    if (full.statusCode == 200) {
                        retval = "<a href='/pagefetchrequest/" + full.id + "'>" + data + "</a>"
                    } else {
                        retval = data;
                    }
                    return retval;
                }
            },
            {
                "mData": "contentId",
                "bVisible": false
            }
        ]
    } );

    var context = $('<a>', { href: window.location } )[0];
    var connection = new WebSocket("ws://" + context.hostname + ":" + context.port + "/status/crawlrequest/" + crId);
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
