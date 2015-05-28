var http = require('http');
var fs = require('fs');

http.createServer(function(request, response) {
	response.writeHead(200, {
		'Content-Type': 'image/png'
	});

	// serve small image
	fs.readFile('./square.png', function (err, data) {
	    if (err) {
	        throw err;
	    }
     	response.end(data, 'binary');
	});

}).listen(3000);