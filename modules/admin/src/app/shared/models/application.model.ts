
export class Application {
  id: string;
  name: string;
  enabled: boolean;

  version: string;
  os: string;
  launchCommand: string;

  constructor() {
    this.enabled = true;
  }
}
