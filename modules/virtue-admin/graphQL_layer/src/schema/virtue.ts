
import rp = require('request-promise');

export const typeDef = `
  extend type Query {
    virtues: [Virtue]
    virtue(id: String!): Virtue
  }
  type Virtue {
    id: String!
    name: String
  }
`;
export type Virtue = {id: string, name: string};

export function getVirtue(id: string) {
  return rp({
      uri: `http://localhost:8080/admin/virtue/template/` + id,
      json: true
    }).then((res: any) => res);
}

export function getVirtues() {
  return rp({
      uri: `http://localhost:8080/admin/virtue/template/`,
      json: true
    }).then((res: any) => res);
}

export const resolvers = {
  Query: {
    virtues: () => getVirtues,
    virtue: (id: string) => getVirtue(id),
  }//,
  // Virtue: {
  //   title: () => { ... },
  // }
};
