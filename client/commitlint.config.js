module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'references-empty': [2, 'never'],
  },
  parserPreset: {
    parserOpts: {
      issuePrefixes: [
        'DCD-',
        'DVOP-',
        'FUI-',
        'GSA-',
        'PA-',
        'PLAT-',
        'PSD-',
        'PTC-',
        'PTF-',
        'PTL-',
        'TPD-',
        'NOTIFY-',
        'PBC-',
      ],
    },
  },
};
