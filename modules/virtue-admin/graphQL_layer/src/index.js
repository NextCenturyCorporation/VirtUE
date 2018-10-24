"use strict";
// const { ApolloServer, gql } = require('apollo-server');
// const { merge } = require('lodash');
exports.__esModule = true;
var apollo_server_1 = require("apollo-server");
var lodash_1 = require("lodash");
// const { typeDef as User, resolvers as userResolvers } = require('./schema/user.js');
//
// const { typeDef as Virtue, resolvers as virtueResolvers } = require('./schema/virtue.js');
var user_js_1 = require("./schema/user.js");
var virtue_js_1 = require("./schema/virtue.js");
// If you had Query fields not associated with a
// specific type you could put them here
var Query = "\n  type Query {\n    _empty: String\n  }\n";
var resolvers = {};
makeExecutableSchema({
    typeDefs: [Query, user_js_1.typeDef, virtue_js_1.typeDef],
    resolvers: lodash_1.merge(resolvers, user_js_1.resolvers, virtue_js_1.resolvers)
});
// In the most basic sense, the ApolloServer can be started
// by passing type definitions (typeDefs) and the resolvers
// responsible for fetching the data for those types.
var server = new apollo_server_1.ApolloServer({ typeDefs: typeDefs, resolvers: resolvers });
// This `listen` method launches a web-server.  Existing apps
// can utilize middleware options, which we'll discuss later.
server.listen().then(function (_a) {
    var url = _a.url;
    console.log("\uD83D\uDE80  Server ready at " + url);
});
