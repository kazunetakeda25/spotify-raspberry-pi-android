const puppeteer = require('puppeteer');

(async () => {
	const browser = await puppeteer.launch({ headless: false, args: ['--no-sandbox']});
	const page = await browser.newPage();
	await page.goto('https://tunein.com/radio/TuneIn-On-Tour-p1152783/?topicId=124710881');
})();