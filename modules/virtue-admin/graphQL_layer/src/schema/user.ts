
import rp = require('request-promise');
import { getVirtues, Virtue } from './virtue';

export const typeDef = `
  extend type Query {
    user(username: String!): User
    users: [User]
  }
  extend type Mutation {
    addUser(newUserData: UserInput!): User
    changeUser(username: String!, newUserData: UserInput!): User
  }
  type User {
    username: String!
    authorities: [String]
    virtueTemplateIds: [String]
    virtues: [Virtue]
    enabled: Boolean!
  }
  input UserInput{
    username: String!
    authorities: [String]
    virtueTemplateIds: [String]
    enabled: Boolean
  }
`;
type User = {username: string, virtueTemplateIds: string[], authorities: string[], enabled: boolean};

function addUser(newUserData: User) {
  if (newUserData.authorities === undefined) {
    newUserData.authorities = [];
  }
  if (newUserData.virtueTemplateIds === undefined) {
    newUserData.virtueTemplateIds = [];
  }
  if (newUserData.enabled === undefined) {
    newUserData.enabled = false;
  }
return rp({
    uri: `http://localhost:8080/admin/user/`,
    method: `POST`,
    headers: {'Origin': 'http://localhost:4200'},
    body: newUserData,
    json: true
  });
}

function changeUser(username: string, newUserData: User) {
  return rp({
      uri: `http://localhost:8080/admin/user/` + newUserData.username,
      method: `POST`,
      body: JSON.stringify(newUserData)
    }).then((res: any) => {return res;});
}

function getUserVirtues(u: User) {
  return getVirtues().then((virtues: Virtue[]) => {return virtues.filter(virtue => { return u.virtueTemplateIds.indexOf(virtue.id) !== -1})});
}


function getUser(username: string) {
  return rp({
      uri: `http://localhost:8080/admin/user/` + username,
      method: `GET`
    }).then((res: any) => {return JSON.parse(res);});
}

function getUsers() {
  return rp({
      uri: `http://localhost:8080/admin/user/`,
      method: `GET`
    }).then((res: any) => {return JSON.parse(res);});
}

export const resolvers = {
  Query: {
    users: () => getUsers(),
    user: (_: any, input: {name: string}) => getUser(input.name),
  },
  Mutation: {
    addUser: (_: any, input: {newUserData: User}) => addUser(input.newUserData),
    changeUser: (_: any, input: {username: string, newUserData: User}) => changeUser(input.username, input.newUserData)
  },
  // // this is useful for doing light processing to the requested data, or for generated a simulated attribute
  // // that doesn't directly exist on the object. Note anything here must also be in schema.
  User: {
    // username: (u: User) => u.username.toUpperCase(),
    // initials: (u: User) => u.username[0] + " " + u.id[0]
    virtues: (u: User) => getUserVirtues(u)
  }
};
