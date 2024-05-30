const { resolve } = require('node:path');
const { addUIDependency, addServerDependency, runPreRequisiteChecks } = require('@genesislcap/seed-utils');
const checks = require('./checks');
const versions = require('./versions.json');
const packageJson = require('./package.json');

const parseEntityList = (entityArray) => {
  if (!entityArray) {
    return "";
  }
  try {
    if (entityArray.length === 0) {
      return ""
    }
    return `listOf(${entityArray.map(entity => JSON.stringify(entity)).join(", ")})`
  } catch (error) {
    console.error("Error parsing `notificationEntities` parameter as JSON:", error.message);
    return "";
  }
}

module.exports = async (data, utils) => {
  const { editJSONFile } = utils;
  const json = editJSONFile(packageJson);
  data.pbcVersion = json.get('version');
  data.date = Date();
  data.utils = utils;
  data.versions = versions;
  data.notificationEntitiesKotlin = parseEntityList(data.notificationEntities)

  /**
   * Run checks on project
   */
  runPreRequisiteChecks(data, checks);

  addUIDependency(data, '@genesislcap/pbc-notify-ui', versions.dependencies.pbcNotify);

  addServerDependency(data, {name: 'genesis-notify'}, versions.dependencies.serverDepId);
};
