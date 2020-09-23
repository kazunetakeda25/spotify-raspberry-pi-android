const infoFile = './info-local.json';

const { exec } = require('child_process');
const spawn = require('child_process').spawn;
const keypress = require('keypress');
const fs = require('fs');

var volume = 100;

function localPlay(infoFile) {

	exec("amixer -c 0 set 'Line Out' " + volume + "%");

	fs.readFile(infoFile, 'utf8', function(err, data) {
		
		if (err) throw err;

		const obj = JSON.parse(data);
		if (obj.type == 'local') {
			const mplayer = spawn('mplayer', [obj.url]);
			
			mplayer.stdout.on('data', function (data) {
				//console.log(data.toString());
			});
			mplayer.stderr.on('data', function (data) {
				//console.log(data.toString());
				console.log("Playing...");
			});
			mplayer.on('exit', function (code) {
				//console.log('child process exited with code ' + code.toString());
			});
			keypress(process.stdin);
			process.stdin.on('keypress', function (ch, key) {
				if (ch == ' ') {
					mplayer.stdin.write(' ');
					console.log('Pause/Resume');
				} else if (ch == '+') {
					if (volume < 100) {
						volume += 5;
						exec("amixer -c 0 set 'Line Out' " + volume + "%");
						console.log("Volume : %d", volume);
					}
				} else if (ch == '-') {
					if (volume > 0) {
						volume -= 5;
						exec("amixer -c 0 set 'Line Out' " + volume + "%");
						console.log("Volume : %d", volume);
					}
				} else if (ch == 'm') {
					if (volume > 0 && volume <= 100) {
						volume = 0;
						exec("amixer -c 0 set 'Line Out' " + volume + "%");
						console.log("Volume Muted");
					} else {
						volume = 100;
						exec("amixer -c 0 set 'Line Out' 100%");
						console.log("Volume Unmuted");
					}
				} else if (ch == 'q' || (key && key.ctrl && key.name == 'c')) {
					mplayer.stdin.write('q');
					console.log('Exiting...');
					process.exit(0);
				}
			});
			process.stdin.setRawMode(true);
			process.stdin.resume();
		}
	});
}

localPlay(infoFile);
