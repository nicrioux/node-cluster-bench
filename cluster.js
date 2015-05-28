var cluster = require('cluster');

if (cluster.isMaster) {
    // How many cpus are available?
    var cpuCount = require('os').cpus().length;
    console.log('Spawning '+ cpuCount +' workers' );

    // Spawn OS process running app.js
    for (var i = 0; i < cpuCount; i += 1) {
        cluster.fork();
    }
}else{ // is used by forked process
    require('./app.js');
}

// handle dying workers
cluster.on('exit', function (worker) {
    cluster.fork();
});