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

  remove(key: string) {
    if (!(key in this.d)) {
      return;
    }
    var index: number = this.l.indexOf(this.d[key], 0);
    if (index > -1) {
       this.l.splice(index, 1);
    }
    this.length -= 1;
    delete this.d[key];
  }

  clear() {
    this.d = null;
    this.l = null;
  }
}

export class Dict<T> {
    [key: string]: T;
}
