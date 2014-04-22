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
                    return "<a href='/pagefetchrequest/" + full.id + "'>" + data + "</a>";
                }
            },
            {
                "mData": "contentId",
                "bVisible": false
            }
        ]
    } );
});
