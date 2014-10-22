$(function(){
    $.fn.editable.defaults.mode = 'inline';

    $('.edit-links').on('click', function(e){
	e.preventDefault();
	$('.delete-button').show();
	});

    $('.add-links').on('click', function(e){ addRow(); return false; });

    $('.add-links').on('save', function(e, params) {
	$().editable('destroy');
    });
});

function addRow()
{
    $("#links-table").find("tbody")
	.append($("<tr>")
		.append($("<td>")
			.append($("<a>")
				.attr("href", "#")
				.attr("data-type", "text")
				.attr("data-name", "link")
				.attr("data-original-title", "Enter Amazon URL")
				.attr("class", "new-link-param")
				.attr("id", "link")))
		.append($("<td>")
			.append($("<a>")
				.attr("href", "#")
				.attr("data-type", "text")
				.attr("data-name", "description")
				.attr("data-title", "Enter Description")
				.attr("class", "new-link-param")
				.attr("id", "description")))
		.append($("<button>")
			.text("Save!")
			.attr("type", "submit")
			.attr("class", "btn btn-primary")
			.attr("id", "save-btn")));

   $('#link').editable('option', 'validate', function(v) {
       if(!v) return 'Required field!';});

    $('#description').editable('option', 'validate', function(v) {
       if(!v) return 'Required field!';});

    $(document).ready(function() {
	$('.new-link-param').editable();
    });
    
    $('#save-btn').click(function() {
    	$('.new-link-param').editable('submit', {
    	    url: '/newlink', 
    	    ajaxOptions: {
		type: 'post',
		dataType: 'json'
    	    },
	    // .. not passing if stmt for some reason 
	    // try using json lib
    	    success: function (data, config) {
		console.log(JSON.stringify(data));
		console.log(data.actionid);
		console.log(typeof data);
    		if(data && data.actionid){ // assumes response like {"actionid": 2}
		    $(this).editable('option', 'actionid', data.actionid);
		    $(this).removeClass('editable-unsaved');
		    var msg = "Link saved.";
		    $('#msg').addClass('alert-success').removeClass('hide').removeClass('alert-error').html(msg).show();
		    $(this).off('save.newuser');
		}
		else if(data && data.errors){
		    config.error.call(this, data.errors); 
		}
    	    },
    	    error: function(errors) {
		var msg = "";
		if(errors && errors.responseText){
		    msg = errors.responseText;
		} else {
    		console.log("Error: " + errors.responseText);
    		}
    	    }}).editable('destroy').removeClass().removeAttr('id').parent().parent().children("button").remove();
    });
}

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

