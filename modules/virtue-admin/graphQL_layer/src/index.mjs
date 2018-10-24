const { ApolloServer, gql } = require('apollo-server');
const { merge } = require('lodash');

// import { merge } from 'lodash';

import {
  typeDef as User,
  resolvers as userResolvers,
} from './schema/user.js';

import {
  typeDef as Virtue,
  resolvers as bookResolvers,
} from './schema/virtue.js';

// If you had Query fields not associated with a
// specific type you could put them here
const Query = `
  type Query {
    _empty: String
  }
`;
const resolvers = {};
makeExecutableSchema({
  typeDefs: [ Query, User, Virtue ],
  resolvers: merge(resolvers, authorResolvers, bookResolvers),
});

// In the most basic sense, the ApolloServer can be started
// by passing type definitions (typeDefs) and the resolvers
// responsible for fetching the data for those types.
const server = new ApolloServer({ typeDefs, resolvers });

// This `listen` method launches a web-server.  Existing apps
// can utilize middleware options, which we'll discuss later.
server.listen().then(({ url }) => {
  console.log(`🚀  Server ready at ${url}`);
});
