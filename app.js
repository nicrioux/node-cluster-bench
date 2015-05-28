 var http = require('http');
var exec = require('child_process').exec;

http.createServer(function(request, response) {
	response.writeHead(200, {
		'Content-Type': 'text/plain'
	});

	var java_child = exec('java -cp . PingGenerator',
		function (error, stdout, stderr) {
	    response.write( stdout );
	    response.end();
	});
}).listen(3000);