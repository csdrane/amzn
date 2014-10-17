$(function(){
    $('.edit-links').on('click', function(e){
	e.preventDefault();
	$('.delete-button').show();
	});
});

function deleteRow(t)
{
    var actionid = $(t).parent().parent().prop("id");
    $.ajax({
	type: 'post',
	url: '/delete-link',
	data: { actionid : actionid },
	success: function()
	{
	    var row = t.parentNode.parentNode;
	    document.getElementById("links-table").deleteRow(row.rowIndex);
	    console.log(row);
	},
	error: function(jq, status, message) {
	    // show result of failed request
	}
    });
}

