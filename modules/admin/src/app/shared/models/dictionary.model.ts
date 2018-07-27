export class DictList<T> {
  private d: Dict<T> = {};
  private l: T[] = [];

  add(key: string, e: T) {
    if (key in this.d) {
      console.log("Key ", key + ": ", e, " already in dict.");
      return;
    }
    this.d[key] = e;
    this.l.push(e);
  }


  get(key: string) {
    return this.d[key];
  }

  getL() {
    return this.l;
  }

  clear() {
    this.d = null;
    this.l = null;
  }
}

export class Dict<T> {
    [key: string]: T;
}
