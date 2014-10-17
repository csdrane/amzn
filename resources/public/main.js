$(function(){
    $('.edit-links').on('click', function(e){
	e.preventDefault();
	$('.delete-button').show();
	});
});

function deleteRow(t)
{
    var row = t.parentNode.parentNode;
    document.getElementById("links-table").deleteRow(row.rowIndex);
    console.log(row);
}
