import { Application } from './application.model';

export class VirtualMachine {
  id: string;
  name: string;
  os: string;
  templatePath: string;
  applications: Application[];
}
