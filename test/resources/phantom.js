var system = require('system');

if (system.args.length < 2) {
    console.log('Expected a target URL parameter.');
    phantom.exit(1);
}

var page = require('webpage').create();
var url = system.args[1];

page.onConsoleMessage = function (message) {
    console.log(message);
};

console.log("Loading URL: " + url);

var counter = 0;

function checkDone() {
    counter++;

    if (counter>100) {
        console.log("async tests never finished...")
        phantom.exit(140);
    }

    var done = page.evaluate(function(){
        return window["tests-done"];
    });
    if (done) {
        var failures = page.evaluate(function(){
            return window["test-failures"];
        });

        phantom.exit(failures?100:0);
    }
}

page.open(url, function (status) {
    if (status != "success") {
        console.log('Failed to open ' + url);
        phantom.exit(1);
    }

    setInterval(checkDone, 100);
});
