// user.js


export const typeDef = `
  extend type Query {
    users: [User]
    user(id: Int!): User
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
  console.log(users);
  return users;
}

function getUser(id: number): User {
  console.log(id, users[0].id, id === users[0].id);
  return users.find(x => x.id === id);
}

function addUser(newId: number, newUsername: string): User {
  users.push({id:newId, username: newUsername, virtues: [{id: 0, name: "V0"}]});
  return users[users.length - 1];
}

function changeName(id: number, newUsername: string): User {
  let user = getUser(id);
  if (user !== undefined) {
    user.username = newUsername;
    console.log("changing name to ", newUsername);
    return user;
  }
}

export const resolvers = {
  Query: {
    users: () => getUsers(),
    // users: () => users,
    user: (id: number) => {console.log(id); return getUser(id)},
  },
  Mutation: {
    addUser: (id: number, username: string) => addUser(id, username),
    changeName: (id: number, newUsername: string) => changeName(id, newUsername)
  }//,
  // User: {
  //   name: () => username,
  // }
};
