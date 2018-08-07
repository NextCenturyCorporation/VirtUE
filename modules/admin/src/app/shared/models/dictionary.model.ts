export class DictList<T> {
  private d: Dict<T> = {};
  private l: T[] = [];

  length: number = 0;

  add(key: string, e: T) {
    if (key in this.d) {
      console.log("Key ", key + ": ", e, " already in dict.");
      return;
    }
    this.d[key] = e;
    this.l.push(e);
    this.length += 1;
  }


  get(key: string) {
    return this.d[key];
  }

  getL(): T[] {
    return this.l;
  }

  clear() {
    this.d = null;
    this.l = null;
  }
}

class Dict<T> {
    [key: string]: T;
}
