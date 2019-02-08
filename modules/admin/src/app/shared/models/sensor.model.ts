
import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

import { DictList } from './dictionary.model';

/**
 * @class
 * This represents a snapshot of asensor.
 *
 * This may need to be drastically changed once sensor-querying is implemented better
 */
export class SensorRecord extends IndexedObj {

  sensor_id: string;
  virtue_id: string;
  username: string;
  updated_at: string;
  public_key: string;
  port: number;
  last_sync_at: string;
  kafka_topic: string;
  inserted_at: string;
  has_registered: boolean;
  has_certificates: boolean;
  configuration_id: number;
  component_id: number;
  address: string;
  timestamp: string;

  constructor( sensorObj: any ) {
    super();
    this.sensor_id = sensorObj.sensor_id;
    this.virtue_id = sensorObj.virtue_id;
    this.username = sensorObj.username;
    this.updated_at = sensorObj.updated_at;
    this.public_key = sensorObj.public_key;
    this.port = sensorObj.port;
    this.last_sync_at = sensorObj.last_sync_at;
    this.kafka_topic = sensorObj.kafka_topic;
    this.inserted_at = sensorObj.inserted_at;
    this.has_registered = sensorObj.has_registered;
    this.has_certificates = sensorObj.has_certificates;
    this.configuration_id = sensorObj.configuration_id;
    this.component_id = sensorObj.component_id;
    this.address = sensorObj.address;
    this.timestamp = sensorObj.timestamp;
  }

  getID(): string {
    return this.sensor_id;
  }

  getDatasetName() {
    return DatasetNames.SENSORS;
  }
}
