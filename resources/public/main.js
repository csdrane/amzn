$(function(){
    $('#edit-links').on('click', function(e){
	e.preventDefault();
	$('.delete-button').show();
	$('.description-editable').editable();
    });

    $('#add-links').on('click', function(e){ 
	addRow(); 
	return false; 
    });
});

function addRow()
{
    //generate html
    $("#links-table").find("tbody")
	.append($("<tr>")
		.append($("<td>")
			.append($("<a>")
				.attr("href", "#")
				.attr("data-type", "text")
				.attr("data-name", "link")
				.attr("data-original-title", "Enter Amazon URL")
				.attr("class", "myeditable")
				.attr("id", "link")))
		.append($("<td>")
			.append($("<a>")
				.attr("href", "#")
				.attr("data-type", "text")
				.attr("data-name", "description")
				.attr("data-title", "Enter Description")
				.attr("class", "myeditable")
				.attr("id", "description")))
		.append($("<button>")
			.text("Save!")
			.attr("type", "submit")
			.attr("class", "btn btn-primary")
			.attr("id", "save-btn")));

    $.fn.editable.defaults.mode = 'inline';

   $('#link').editable('option', 'validate', function(v) {
       if(!v) return 'Required field!';
   });

    $('#description').editable('option', 'validate', function(v) {
	if(!v) return 'Required field!';
    });

//    $('.myeditable').editable();
    
    $('.myeditable').on('save.newlink', function(){
	var that = this;
	setTimeout(function() {
            $(that).closest('td').next().find('.myeditable').editable('show');
	}, 200);
    });

    $('#save-btn').click(function() {
    	$('.myeditable').editable('submit', {
    	    url: '/newlink', 
    	    ajaxOptions: {
		type: 'post',
		dataType: 'json'
    	    },
    	    success: function (data, config) {
		console.log(JSON.stringify(data));
		if(data && data.actionid){ // assumes response like {"actionid": 2}
		    $(this).editable('option', 'actionid', data.actionid);
		    $(this).removeClass('editable-unsaved');
		    var msg = "Link saved.";
		    $('#msg').addClass('alert-success').removeClass('hide').removeClass('alert-error').html(msg).show();
		    $(this).off('save.newlink');
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
    	    }}).removeClass().parent().parent().children("button").remove();
	
	$('#link').editable('option', 'disabled', true).removeAttr('id');
	$('#description').editable('option', 'disabled', true).removeAttr('id');
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
	    var msg = "<strong>Successfully deleted link.</strong>";
	    var row = t.parentNode.parentNode;
	    document.getElementById("links-table").deleteRow(row.rowIndex);
	    console.log(row);
	    $('#msg').addClass('alert-success').removeClass('hide').removeClass('alert-error').html(msg).show();
	},
	error: function(jq, status, message) {
	    var msg = "<strong>Error: Link deletion failed! If this message persists, please contact the administrator.</strong>";
	    $('#msg').removeClass('alert-success').removeClass('hide').addClass('alert-warning').html(msg).show();    
	}
    });
}

// via StackOverflow
var functionLogger = {};

functionLogger.log = true;//Set this to false to disable logging 

/**
 * Gets a function that when called will log information about itself if logging is turned on.
 *
 * @param func The function to add logging to.
 * @param name The name of the function.
 *
 * @return A function that will perform logging and then call the function. 
 */
functionLogger.getLoggableFunction = function(func, name) {
    return function() {
        if (functionLogger.log) {
            var logText = name + '(';

            for (var i = 0; i < arguments.length; i++) {
                if (i > 0) {
                    logText += ', ';
                }
                logText += arguments[i];
            }
            logText += ');';

            console.log(logText);
        }

        return func.apply(this, arguments);
    }
};

/**
 * After this is called, all direct children of the provided namespace object that are 
 * functions will log their name as well as the values of the parameters passed in.
 *
 * @param namespaceObject The object whose child functions you'd like to add logging to.
 */
functionLogger.addLoggingToNamespace = function(namespaceObject){
    for(var name in namespaceObject){
        var potentialFunction = namespaceObject[name];

        if(Object.prototype.toString.call(potentialFunction) === '[object Function]'){
            namespaceObject[name] = functionLogger.getLoggableFunction(potentialFunction, name);
        }
    }
};
