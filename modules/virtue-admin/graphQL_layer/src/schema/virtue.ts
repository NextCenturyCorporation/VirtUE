// virtue.js
export const typeDef = `
  extend type Query {
    virtues: [Virtue]
    virtue(id: Int!): Virtue
  }
  type Virtue {
    id: Int!
    name: String
  }
`;

let virtues = [{id: 1, name: "V1"}];

export const resolvers = {
  Query: {
    virtues: () => virtues,
    virtue: (id: number) => virtues[0],
  }//,
  // Virtue: {
  //   title: () => { ... },
  // }
};
