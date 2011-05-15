// present an alert if an error occurs
var handleError = function(message) {
	alert(message);
};

// Perform an async request for a 
var requestMarkdown = function(file, url, insertHtml) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function(e) {
    	if (req.readyState == 4 /* DONE */) {
    		if (req.status == 200) {
    		    var converter = new Showdown.converter();
    		    var html = converter.makeHtml(req.responseText);    			
    		    insertHtml(html);
        	} else {
        		handleError("Error encountered receiving response from server. Try again later.");
        	}
        }
    };
    
    var request = 'pb';
    if (url) request += '?url=' + encodeURI(url);
    
	req.open(url ? 'GET' : 'POST', request, true);
	req.setRequestHeader("Content-Type", "text/plain; charset=UTF-8");
	req.send(file);
}

var requestMarkdownFromFile = function(file, insertHtml) {
	if (!file) {
		handleError("No file selected");
		return;
	}
	
	requestMarkdown(file, null, insertHtml);
}

var requestMarkdownFromUrl = function(url, insertHtml) {
	requestMarkdown(null, url, insertHtml);
}

var urlParameters = function()
{
    var params = {};
    var href =  window.location.href;
    var index = href.lastIndexOf('?');
    if (index == -1) { index = href.lastIndexOf('/'); }
    var keyValues = href.slice(index + 1).split('&');
    for(var i = 0; i < keyValues.length; i++)
    {
    	keyValueArray = keyValues[i].split('=');
        params[keyValueArray[0]] = decodeURI(keyValueArray[1]);
    }
    return params;
}
