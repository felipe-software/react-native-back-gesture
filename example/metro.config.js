const path = require("node:path");
const { getDefaultConfig } = require("expo/metro-config");

const libraryRoot = path.resolve(__dirname, "..");
const config = getDefaultConfig(__dirname);

// A file: dependency would symlink the repo into its own node_modules, a loop Metro cannot map.
config.resolver.extraNodeModules = {
    "react-native-back-gesture": libraryRoot,
};

module.exports = config;
