const { readJsonSync, writeJsonSync } = require('fs-extra');
const { join } = require('path');

const [package, dependency] = process.argv.slice(2);

const pkgPath = join(__dirname, '../', package, 'package.json');
const packageJson = readJsonSync(pkgPath);
packageJson.dependencies[dependency] = '*';

writeJsonSync(pkgPath, packageJson, { spaces: 2 });
