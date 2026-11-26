// Karma setup for local runs and for the Jenkins pipeline.
// The junit reporter writes test-results/karma/*.xml, which Jenkins publishes so a failing spec fails the build.
// ChromeHeadlessCI is needed inside the CI container: Chromium cannot use its sandbox there,
// and /dev/shm is too small for it by default.
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('karma-junit-reporter'),
    ],
    client: {
      clearContext: false,
    },
    reporters: ['progress', 'kjhtml', 'junit', 'coverage'],
    coverageReporter: {
      dir: require('path').join(__dirname, 'coverage'),
      subdir: '.',
      reporters: [{ type: 'lcovonly' }, { type: 'text-summary' }],
    },
    junitReporter: {
      outputDir: 'test-results/karma',
      useBrowserName: false,
      outputFile: 'frontend-tests.xml',
    },
    browsers: ['Chrome'],
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-dev-shm-usage'],
      },
    },
    restartOnFileChange: true,
  });
};
