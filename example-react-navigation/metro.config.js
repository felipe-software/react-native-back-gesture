const path = require("node:path");
const { getDefaultConfig } = require("expo/metro-config");

const libraryRoot = path.resolve(__dirname, "..");
const config = getDefaultConfig(__dirname);

config.resolver.extraNodeModules = {
    "react-native-back-gesture": libraryRoot,
};

module.exports = config;
