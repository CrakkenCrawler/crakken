$(function() {
    $("#pagefetchrequests").dataTable( {
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
});
