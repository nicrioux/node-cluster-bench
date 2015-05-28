var cluster = require('cluster');

if (cluster.isMaster) {
    var cpuCount = require('os').cpus().length;
    console.log('Spawning '+ cpuCount +' workers' );

    for (var i = 0; i < cpuCount; i += 1) {
        cluster.fork();
    }
}else{
    require('./app.js');
}

// handle dying workers
cluster.on('exit', function (worker) {
    cluster.fork();
});