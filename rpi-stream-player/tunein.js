const infoFile = './info-tunein.json';

const { exec } = require('child_process');
const spawn = require('child_process').spawn;
const keypress = require('keypress');
const fs = require('fs');

var volume = 100;

function tuneInPlay(infoFile) {

	exec("amixer -c 0 set 'Line Out' " + volume + "%");
	
	fs.readFile(infoFile, 'utf8', function(err, data) {
		
		if (err) throw err;

		const obj = JSON.parse(data);
		if (obj.type == 'tunein') {
			const mplayer = spawn('mplayer', [obj.url]);
			mplayer.stdout.on('data', function (data) {
				//console.log(data.toString());
			});
			mplayer.stderr.on('data', function (data) {
				//console.log(data.toString());
			});
			mplayer.on('exit', function (code) {
				//console.log('child process exited with code ' + code.toString());
			});
			keypress(process.stdin);
			process.stdin.on('keypress', function (ch, key) {
				if (ch == ' ') {
					mplayer.stdin.write(' ');
					console.log('P');
				} else if (ch == '+') {
					if (volume < 100) {
						volume += 5;
						exec("amixer -c 0 set 'Line Out' " + volume + "%");
						console.log("+");
					}
				} else if (ch == '-') {
					if (volume > 0) {
						volume -= 5;
						exec("amixer -c 0 set 'Line Out' " + volume + "%");
						console.log("-");
					}
				} else if (ch == 'm') {
					if (volume > 0 && volume <= 100) {
						volume = 0;
						exec("amixer -c 0 set 'Line Out' " + volume + "%");
						console.log('M');
					} else {
						volume = 100;
						exec("amixer -c 0 set 'Line Out' 100%");
					}
				} else if (ch == 'q' || (key && key.ctrl && key.name == 'c')) {
					mplayer.stdin.write('q');
					console.log('Q');
					process.exit(0);
				}
			});
			process.stdin.setRawMode(true);
			process.stdin.resume();
		}
	});
}

tuneInPlay(infoFile);