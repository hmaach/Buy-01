module.exports = function (config) {
  config.set({
    basePath: '',
    browsers: ['ChromeHeadlessNoSandbox'],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: [
          '--no-sandbox',
          '--disable-dev-shm-usage',
          '--disable-gpu'
        ],
      },
    },
    restartOnFileChange: true,
  });
};