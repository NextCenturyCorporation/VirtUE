// const { ApolloServer, gql } = require('apollo-server');
// const { merge } = require('lodash');

import { ApolloServer, gql, makeExecutableSchema } from 'apollo-server';
import { merge } from 'lodash';

// const { typeDef as User, resolvers as userResolvers } = require('./schema/user.js');
//
// const { typeDef as Virtue, resolvers as virtueResolvers } = require('./schema/virtue.js');

import {
  typeDef as User,
  resolvers as userResolvers,
} from './schema/user';

import {
  typeDef as Virtue,
  resolvers as virtueResolvers,
} from './schema/virtue';

// If you had Query fields not associated with a
// specific type you could put them here
const Query = `
  type Query {
    _empty: String
  }
`;

const Mutation = `
  type Mutation {
    _empty: String
  }
`;
const generalResolvers = {};

const typeDefs = [ Query, Mutation, User, Virtue ];
// const typeDefs = [ User, Virtue ];
const resolvers = merge(generalResolvers, userResolvers, virtueResolvers)

// In the most basic sense, the ApolloServer can be started
// by passing type definitions (typeDefs) and the resolvers
// responsible for fetching the data for those types.
const server = new ApolloServer({ typeDefs, resolvers });

// This `listen` method launches a web-server.  Existing apps
// can utilize middleware options, which we'll discuss later.
server.listen().then(({ url }) => {
  console.log(`🚀  Server ready at ${url}`);
});
