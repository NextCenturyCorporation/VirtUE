// user.js


export const typeDef = `
  extend type Query {
    user(id: Int!): User
    users: [User]
  }
  extend type Mutation {
    addUser(id: Int!, username: String!): User
    changeName(id: Int!, newName: String!): User
  }
  type User {
    id: Int!
    username: String
    virtues: [Virtue]
  }
`;
type Virtue = {id: number, name: string};
type User = {id: number, username: string, virtues: Virtue[]};

let users: User[] = [{id: 34, username: "Bob", virtues: [{id: 1, name: "V1"}, {id: 2, name: "V2"}]}];

function getUsers(): User[] {
  return users;
}

function getUser(id: number): User {
  return users.find(x => x.id === id);
}

function addUser(newId: number, username: string): User {
  users.push({id:newId, username: username, virtues: [{id: 0, name: "V0"}]});
  return users[users.length - 1];
}

function changeName(id: number, newName: string): User {
  let user = getUser(id);
  if (user !== undefined) {
    user.username = newName;
    console.log("changing name to ", user.username);
    return user;
  }
}

export const resolvers = {
  Query: {
    users: () => getUsers(),
    user: (_: any, input: {id: number}) => getUser(input.id),
  },
  Mutation: {
    addUser: (_: any, input: {id: number, username: string}) => addUser(input.id, input.username),
    changeName: (_: any, input: {id: number, newName: string}) => changeName(input.id, input.newName)
  },
  User: {
    name: () => username,
  }
};
